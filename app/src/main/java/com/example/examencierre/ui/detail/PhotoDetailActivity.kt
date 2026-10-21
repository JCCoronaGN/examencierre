package com.example.examencierre.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.examencierre.R
import com.example.examencierre.data.local.entity.PhotoEntity
import com.example.examencierre.databinding.ActivityPhotoDetailBinding
import com.example.examencierre.util.applySystemBarInsets
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoDetailBinding
    private val viewModel: PhotoDetailViewModel by viewModels { PhotoDetailViewModel.Factory }
    private var currentPhoto: PhotoEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonDelete.setOnClickListener { confirmDelete() }
        binding.buttonOpenMap.setOnClickListener { openInMap() }

        observeViewModel()
        viewModel.load(intent.getLongExtra(EXTRA_PHOTO_ID, -1L))
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        when (state) {
                            PhotoDetailUiState.Loading -> binding.progressBar.isVisible = true
                            PhotoDetailUiState.NotFound -> finish()
                            is PhotoDetailUiState.Found -> {
                                binding.progressBar.isVisible = false
                                currentPhoto = state.photo
                                bindPhoto(state.photo)
                            }
                        }
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            PhotoDetailEvent.Deleted -> {
                                setResult(RESULT_OK)
                                finish()
                            }

                            PhotoDetailEvent.DeleteError -> Snackbar.make(
                                binding.root,
                                R.string.error_generic,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun bindPhoto(photo: PhotoEntity) {
        binding.imageViewPhoto.load(photo.ruta) {
            crossfade(true)
            placeholder(R.drawable.ic_photo_placeholder)
            error(R.drawable.ic_photo_placeholder)
        }

        binding.textViewComment.text = photo.comentario
            ?.takeIf { it.isNotBlank() }
            ?: getString(R.string.detail_no_comment)

        val hasLocation = photo.latitud != null && photo.longitud != null
        binding.buttonOpenMap.isVisible = hasLocation
        binding.textViewNoGps.isVisible = !hasLocation
        binding.groupCoordinates.isVisible = hasLocation

        if (hasLocation) {
            binding.textViewLatitude.text = getString(
                R.string.gps_decimal_format,
                photo.latitud
            )
            binding.textViewLongitude.text = getString(
                R.string.gps_decimal_format,
                photo.longitud
            )
            binding.textViewAltitude.text = photo.altitud?.let {
                getString(R.string.gps_altitude_format, it)
            } ?: getString(R.string.detail_no_gps)
            binding.textViewAccuracy.text = photo.precisionMetros?.let {
                getString(R.string.gps_accuracy_format, it)
            } ?: getString(R.string.detail_no_gps)
        }

        binding.textViewDate.text = SimpleDateFormat(
            getString(R.string.date_format),
            Locale.getDefault()
        ).format(Date(photo.fechaHora))
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                currentPhoto?.let(viewModel::delete)
            }
            .show()
    }

    private fun openInMap() {
        val photo = currentPhoto ?: return
        val lat = photo.latitud ?: return
        val lon = photo.longitud ?: return
        val uri = "geo:$lat,$lon?q=$lat,$lon".toUri()
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, R.string.no_map_app, Snackbar.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_PHOTO_ID = "extra_photo_id"
    }
}
