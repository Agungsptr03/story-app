package com.dicoding.storyapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.data.UploadResponse
import com.dicoding.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.storyapp.model.ViewModelFactory
import com.dicoding.storyapp.model.ViewModelMain
import com.dicoding.storyapp.repo.Output
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel: ViewModelMain by viewModels { ViewModelFactory.getInstance(this) }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            binding.addStoryImage.setImageURI(it)
            viewModel.setCurrentImageUri(it)
        } ?: run {
            Toast.makeText(this, getString(R.string.gallery_error), Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            binding.addStoryImage.setImageURI(viewModel.currentImageUri.value)
        } else {
            viewModel.setCurrentImageUri(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.actionbar_upload_story)

        viewModel.currentImageUri.observe(this) { uri ->
            binding.addStoryImage.setImageURI(uri)
        }

        binding.cameraButton.setOnClickListener { openCamera() }
        binding.galleryButton.setOnClickListener { openGallery() }
        binding.uploadButton.setOnClickListener { uploadStory() }
    }

    private fun openCamera() {
        val imageUri = getImageUri(this)
        viewModel.setCurrentImageUri(imageUri)
        cameraLauncher.launch(imageUri)
    }

    private fun openGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun uploadStory() {
        viewModel.currentImageUri.value?.let { uri ->
            lifecycleScope.launch {
                binding.progressBar.visibility = View.VISIBLE

                val imageFile = withContext(Dispatchers.IO) {
                    uriToFile(uri, this@AddStoryActivity).reduceFileImage()
                }

                val description = binding.edAddDescription.text.toString()

                viewModel.uploadStory(imageFile, description).observe(this@AddStoryActivity) { result ->
                    handleUploadResult(result)
                }
            }
        } ?: Toast.makeText(this, getString(R.string.upload_error), Toast.LENGTH_SHORT).show()
    }

    private fun handleUploadResult(result: Output<UploadResponse>) {
        when (result) {
            is Output.Error -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
            }
            is Output.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            is Output.Success -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, result.data.message, Toast.LENGTH_SHORT).show()
                if (!result.data.error) {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
            }
        }
    }

    private fun getImageUri(context: Context): Uri {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val file = createCustomTempFile(context)
        context.contentResolver.openInputStream(uri).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        }
        return file
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${System.currentTimeMillis()}", ".jpg", storageDir)
    }

    private fun File.reduceFileImage(): File {
        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(this.path, bitmapOptions)

        val inSampleSize = calculateInSampleSize(bitmapOptions)

        bitmapOptions.inSampleSize = inSampleSize
        bitmapOptions.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(this.path, bitmapOptions)

        val reducedFile = File.createTempFile("REDUCED_${System.currentTimeMillis()}", ".jpg", this.parentFile)

        FileOutputStream(reducedFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        }
        return reducedFile
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        val targetHeight = 800
        val targetWidth = 800

        if (height > targetHeight || width > targetWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= targetHeight && (halfWidth / inSampleSize) >= targetWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
