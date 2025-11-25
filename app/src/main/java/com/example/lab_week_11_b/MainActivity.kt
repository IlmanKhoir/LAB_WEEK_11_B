package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    // helpers and managers
    private lateinit var providerFileManager: ProviderFileManager

    // Data model holders for the file to be created
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null

    private var isCapturingVideo = false

    private lateinit var takePictureLauncher: ActivityResultLauncher<android.net.Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<android.net.Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize provider file manager and helpers
        providerFileManager = ProviderFileManager(
            applicationContext,
            FileHelper(applicationContext),
            contentResolver,
            Executors.newSingleThreadExecutor(),
            MediaContentHelper()
        )

        // Register activity result launchers
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                providerFileManager.insertImageToStore(photoInfo)
            }
        }

        takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { _ ->
            // Copy file into MediaStore
            providerFileManager.insertVideoToStore(videoInfo)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<android.widget.Button>(R.id.photo_button).setOnClickListener {
            isCapturingVideo = false
            checkStoragePermission { openImageCapture() }
        }

        findViewById<android.widget.Button>(R.id.video_button).setOnClickListener {
            isCapturingVideo = true
            checkStoragePermission { openVideoCapture() }
        }
    }

    // Create file info and launch the camera app to take a picture
    private fun openImageCapture() {
        photoInfo = providerFileManager.generatePhotoUri(System.currentTimeMillis())
        photoInfo?.let { takePictureLauncher.launch(it.uri) }
    }

    // Create file info and launch the camera app to record a video
    private fun openVideoCapture() {
        videoInfo = providerFileManager.generateVideoUri(System.currentTimeMillis())
        videoInfo?.let { takeVideoLauncher.launch(it.uri) }
    }

    // Request storage permission at runtime for older Android versions
    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ doesn't need WRITE_EXTERNAL_STORAGE for MediaStore use
            onPermissionGranted()
            return
        }

        when (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PackageManager.PERMISSION_GRANTED -> onPermissionGranted()
            else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
        }
    }

    // Handle pre-Android 10 permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isCapturingVideo) openVideoCapture() else openImageCapture()
                }
            }
            else -> {}
        }
    }
}
