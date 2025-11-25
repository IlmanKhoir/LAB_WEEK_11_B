package com.example.lab_week_11_b

import android.content.Context
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class FileHelper(private val context: Context) {

    // Generate a URI for a file using FileProvider
    fun getUriFromFile(file: File) =
        FileProvider.getUriForFile(context, "com.example.lab_week_11_b.camera", file)

    // Folder name for Pictures as used in file_provider_paths.xml and as relative path in MediaStore
    fun getPicturesFolder(): String = Environment.DIRECTORY_PICTURES

    // Folder name for Movies/Videos
    fun getVideosFolder(): String = Environment.DIRECTORY_MOVIES
}
