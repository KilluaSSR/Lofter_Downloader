package com.killua.lofter_downloader

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern
import kotlin.concurrent.thread

class MainViewModel(application: Application) :AndroidViewModel(application) {
    private val applicationContext = application.applicationContext
            fun lofterLoadData(URL:String){
                val resultSet = HashSet<String>()
                thread {
                    val url = URL(URL)
                    val connection = url.openConnection() as HttpURLConnection
                    try {
                        val response = StringBuilder()
                        connection.apply {
                            setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
                            setRequestProperty("Referer",URL)
                            requestMethod = "GET"
                            connectTimeout = 8000
                            readTimeout = 8000
                        }
                        val input = connection.inputStream
                        val reader = BufferedReader(InputStreamReader(input))
                        reader.apply {
                            forEachLine {
                                response.append(it)
                            }
                        }
                        val pattern = Pattern.compile("""bigimgsrc="(.*?)(?=\?imageView)""")
                        val matcher = pattern.matcher(response.toString())
                        while (matcher.find()) {
                            matcher.group(1)?.let { resultSet.add(it) }
                        }
                        for(items in resultSet){
                            lofterDownload(items)
                        }

                    }catch (e:Exception){
                        e.printStackTrace()
                    }finally {
                        connection.disconnect()
                    }
                }
            }
            @SuppressWarnings
            fun lofterDownload(imageURL:String) {
                val url = URL(imageURL)
                thread {
                    val connection = url.openConnection() as HttpURLConnection
                    try {
                        connection.apply {
                            setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
                            setRequestProperty("Referer",imageURL)
                            setRequestProperty("Origin", imageURL)
                            requestMethod = "GET"
                            connectTimeout = 8000
                            readTimeout = 8000
                        }
                        val inputStream: InputStream = connection.inputStream
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            val result = Regex(pattern = "/([^/]+)\$")
                            "${result.find(imageURL)?.value}"
                            saveImageToGallery(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        connection.disconnect()
                    }
                }
            }
            private fun saveImageToGallery(bitmap: Bitmap) {
                thread {
                    val displayName = "Image_${System.currentTimeMillis()}.jpg"
                    val mimeType = "image/jpeg"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                        }
                    }
                     val resolver = applicationContext.contentResolver
                    var stream: OutputStream? = null
                    val uri: Uri?

                    try {
                        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        uri = resolver.insert(contentUri, contentValues)
                        if (uri != null) {
                               stream = resolver.openOutputStream(uri)
                            if (stream != null) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                                stream.flush()
                            }
                        }
                    } catch (e: Exception) {
                        // 处理异常
                    } finally {
                        stream?.close()
                        Looper.prepare()
                        Toast.makeText(applicationContext,"下载完成",Toast.LENGTH_SHORT).show()
                        Looper.loop()
                    }
                }
            }
    private val visiblePermissionDialogQueue = mutableStateListOf<String>()

//    fun dismissDialog(){
//        visiblePermissionDialogQueue.removeLast()
//    }
    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted) {
            visiblePermissionDialogQueue.add(0, permission)
        }
    }
        }


