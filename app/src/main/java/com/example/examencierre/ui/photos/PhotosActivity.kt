package com.example.examencierre.ui.photos

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.examencierre.R
import com.example.examencierre.databinding.ActivityPhotosBinding
import com.example.examencierre.ui.detail.PhotoDetailActivity
import com.example.examencierre.util.applySystemBarInsets
import kotlinx.coroutines.launch

class PhotosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotosBinding
    private val viewModel: PhotosViewModel by viewModels { PhotosViewModel.Factory }

    private val photosAdapter = PhotosAdapter { photo ->
        startActivity(
            Intent(this, PhotoDetailActivity::class.java)
                .putExtra(PhotoDetailActivity.EXTRA_PHOTO_ID, photo.id)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.photos_title)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = GridLayoutManager(this, GRID_COLUMNS)
        binding.recyclerView.adapter = photosAdapter
        binding.recyclerView.setHasFixedSize(true)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.photos.collect { photos ->
                    photosAdapter.submitList(photos)
                    val isEmpty = photos.isEmpty()
                    binding.groupEmpty.isVisible = isEmpty
                    binding.recyclerView.isVisible = !isEmpty
                }
            }
        }
    }

    private companion object {
        const val GRID_COLUMNS = 2
    }
}
