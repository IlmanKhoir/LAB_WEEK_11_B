package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.util.concurrent.Executor

class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {

    fun generatePhotoUri(time: Long): FileInfo {
        val name = "img_time.jpg".replace("\u007f", "$time")
        val file = File(context.getExternalFilesDir(fileHelper.getPicturesFolder()), name)
        return FileInfo(
            uri = fileHelper.getUriFromFile(file),
            file = file,
            name = name,
            relativePath = fileHelper.getPicturesFolder(),
            mimeType = "image/jpeg"
        )
    }

    fun generateVideoUri(time: Long): FileInfo {
        val name = "video_time.mp4".replace("\u007f", "$time")
        val file = File(context.getExternalFilesDir(fileHelper.getVideosFolder()), name)
        return FileInfo(
            uri = fileHelper.getUriFromFile(file),
            file = file,
            name = name,
            relativePath = fileHelper.getVideosFolder(),
            mimeType = "video/mp4"
        )
    }

    fun insertImageToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(it, mediaContentHelper.getImageContentUri(), mediaContentHelper.generateImageContentValues(it))
        }
    }

    fun insertVideoToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(it, mediaContentHelper.getVideoContentUri(), mediaContentHelper.generateVideoContentValues(it))
        }
    }

    private fun insertToStore(fileInfo: FileInfo, contentUri: Uri, contentValues: ContentValues) {
        executor.execute {
            try {
                val insertedUri = contentResolver.insert(contentUri, contentValues)
                if (insertedUri != null) {
                    contentResolver.openInputStream(fileInfo.uri)?.use { inputStream ->
                        contentResolver.openOutputStream(insertedUri)?.use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.e("ProviderFileManager", "failed to insert to MediaStore", t)
            }
        }
    }
}
