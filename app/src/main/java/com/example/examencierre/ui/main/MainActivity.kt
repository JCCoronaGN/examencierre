package com.example.examencierre.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.examencierre.R
import com.example.examencierre.data.session.SessionManager
import com.example.examencierre.databinding.ActivityMainBinding
import com.example.examencierre.ui.camera.CameraActivity
import com.example.examencierre.ui.login.LoginActivity
import com.example.examencierre.ui.photos.PhotosActivity
import com.example.examencierre.util.applySystemBarInsets
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Snackbar.make(binding.root, R.string.photo_saved, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        if (!sessionManager.isLoggedIn) {
            goToLogin()
            return
        }

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.main_title)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                confirmLogout()
                true
            } else {
                false
            }
        }
    }

    private fun setupListeners() {
        binding.buttonTakePhoto.setOnClickListener {
            takePhotoLauncher.launch(Intent(this, CameraActivity::class.java))
        }
        binding.buttonViewPhotos.setOnClickListener {
            startActivity(Intent(this, PhotosActivity::class.java))
        }
    }

    private fun observeViewModel() {
        binding.textViewWelcome.text = getString(
            R.string.welcome_user,
            sessionManager.userName,
            sessionManager.userLastName
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.lastPhoto.collect { photo ->
                        binding.imageViewLastPhoto.isVisible = photo != null
                        binding.textViewNoPhotos.isVisible = photo == null
                        if (photo != null) {
                            binding.imageViewLastPhoto.load(photo.ruta) {
                                crossfade(true)
                                placeholder(R.drawable.ic_photo_placeholder)
                                error(R.drawable.ic_photo_placeholder)
                            }
                        }
                    }
                }
                launch {
                    viewModel.photoCount.collect { count ->
                        binding.textViewPhotoCount.text = resources.getQuantityString(
                            R.plurals.photo_count,
                            count,
                            count
                        )
                    }
                }
            }
        }
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_confirm_title)
            .setMessage(R.string.logout_confirm_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.logout) { _, _ -> logout() }
            .show()
    }

    private fun logout() {
        sessionManager.clearSession()
        goToLogin()
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
