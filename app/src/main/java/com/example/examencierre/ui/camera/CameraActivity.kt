package com.example.examencierre.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.examencierre.R
import com.example.examencierre.data.session.SessionManager
import com.example.examencierre.data.storage.PhotoStorage
import com.example.examencierre.databinding.ActivityCameraBinding
import com.example.examencierre.databinding.DialogPhotoCommentBinding
import com.example.examencierre.util.LocationProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var photoStorage: PhotoStorage
    private lateinit var sessionManager: SessionManager
    private lateinit var locationProvider: LocationProvider

    private val viewModel: CameraViewModel by viewModels { CameraViewModel.Factory }

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var capturedFile: File? = null
    private var capturedLocation: Location? = null
    private var photoSaved = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val cameraGranted = result[Manifest.permission.CAMERA] == true
            val locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (cameraGranted) {
                startCamera()
            } else {
                showCameraPermissionDenied()
            }

            if (!locationGranted) {
                Snackbar.make(
                    binding.root,
                    R.string.permission_location_required,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoStorage = PhotoStorage(applicationContext.filesDir)
        sessionManager = SessionManager(this)
        locationProvider = LocationProvider(this)

        setupListeners()
        observeViewModel()
        checkPermissions()
    }

    private fun setupListeners() {
        binding.buttonCapture.setOnClickListener { capturePhoto() }
        binding.buttonSwitchCamera.setOnClickListener { switchCamera() }
        binding.buttonClose.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isSaving.collect { saving ->
                        binding.progressOverlay.isVisible = saving
                        binding.buttonCapture.isEnabled = !saving
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is CameraEvent.PhotoSaved -> {
                                photoSaved = true
                                setResult(RESULT_OK, Intent().putExtra(EXTRA_PHOTO_ID, event.photoId))
                                finish()
                            }

                            CameraEvent.SaveError -> {
                                Snackbar.make(
                                    binding.root,
                                    R.string.error_database,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val missingPermissions = buildList {
            if (!isPermissionGranted(Manifest.permission.CAMERA)) {
                add(Manifest.permission.CAMERA)
            }
            if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
                !isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }

        if (missingPermissions.isEmpty()) {
            startCamera()
        } else {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val hasFront = runCatching {
                provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            }.getOrDefault(false)
            val hasBack = runCatching {
                provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            }.getOrDefault(false)
            binding.buttonSwitchCamera.isVisible = hasFront && hasBack

            bindCameraProvider(provider, preview)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraProvider(
        provider: ProcessCameraProvider,
        preview: Preview
    ) {
        try {
            provider.unbindAll()
            provider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch (e: IllegalArgumentException) {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            provider.unbindAll()
            provider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch (e: Exception) {
            Snackbar.make(binding.root, R.string.capture_error, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        val provider = cameraProvider ?: return
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.previewView.surfaceProvider)
        }
        bindCameraProvider(provider, preview)
    }

    private fun capturePhoto() {
        val capture = imageCapture ?: return
        val photoFile = photoStorage.createPhotoFile()
        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        binding.buttonCapture.isEnabled = false
        capture.takePicture(
            options,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    capturedFile = photoFile
                    resolveLocationAndAskComment()
                }

                override fun onError(exception: ImageCaptureException) {
                    binding.buttonCapture.isEnabled = true
                    Snackbar.make(binding.root, R.string.capture_error, Snackbar.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun resolveLocationAndAskComment() {
        binding.progressOverlay.isVisible = true
        binding.progressText.setText(R.string.location_waiting)
        lifecycleScope.launch {
            capturedLocation = locationProvider.getCurrentLocation()
            binding.progressOverlay.isVisible = false
            showCommentDialog()
        }
    }

    private fun showCommentDialog() {
        val file = capturedFile ?: return
        val dialogBinding = DialogPhotoCommentBinding.inflate(layoutInflater)

        val location = capturedLocation
        dialogBinding.textViewGpsInfo.text = if (location != null) {
            getString(
                R.string.gps_registered,
                getString(
                    R.string.gps_coordinates_format,
                    location.latitude,
                    location.longitude
                )
            )
        } else {
            getString(R.string.location_unavailable)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.comment_title)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.action_discard) { _, _ -> discardPhoto(file) }
            .setPositiveButton(R.string.action_save) { _, _ ->
                val comment = dialogBinding.editTextComment.text?.toString()?.trim()
                viewModel.savePhoto(
                    userId = sessionManager.userId,
                    file = file,
                    comentario = comment,
                    location = capturedLocation
                )
            }
            .setOnCancelListener { discardPhoto(file) }
            .show()
    }

    private fun discardPhoto(file: File) {
        photoStorage.delete(file)
        capturedFile = null
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        // Si el usuario abandona la pantalla antes de guardar, la foto no debe quedar huérfana.
        if (isFinishing && !photoSaved) {
            capturedFile?.let { photoStorage.delete(it) }
        }
        super.onDestroy()
    }

    private fun showCameraPermissionDenied() {
        Snackbar.make(binding.root, R.string.permission_camera_required, Snackbar.LENGTH_LONG)
            .setAction(R.string.permission_settings) {
                val intent = Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.fromParts("package", packageName, null)
                )
                startActivity(intent)
            }
            .show()
        binding.buttonCapture.isEnabled = false
        binding.previewView.visibility = View.INVISIBLE
    }

    companion object {
        const val EXTRA_PHOTO_ID = "extra_photo_id"
    }
}
