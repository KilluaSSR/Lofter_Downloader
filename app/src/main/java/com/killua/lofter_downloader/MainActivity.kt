package com.killua.lofter_downloader
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.killua.lofter_downloader.ui.theme.Lofter_DownloaderTheme
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val buildVersion = Build.VERSION.SDK_INT //获取系统版本
            val viewModel: MainViewModel by viewModels()
            val imagePermissionResultLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    viewModel.onPermissionResult(
                        permission =when(buildVersion >= 33) { true->READ_MEDIA_IMAGES  false->READ_EXTERNAL_STORAGE},
                        isGranted = isGranted
                    )
                }
            )
          //  var imagePermissionsGranted by remember { mutableStateOf(isImagePermissionGranted()) }

//            var shouldShowPermissionRationale by remember {
//                mutableStateOf(
//                    shouldShowRequestPermissionRationale(when(buildVersion>=33){true-> READ_MEDIA_IMAGES false-> READ_EXTERNAL_STORAGE})
//                )
//            }
//            var shouldDirectUserToApplicationSettings by remember {
//                mutableStateOf(false)
//            }
//
//            var currentPermissionsStatus by remember {
//                mutableStateOf(decideCurrentPermissionStatus(imagePermissionsGranted, isImagePermissionGranted()))
//            }
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(key1 = lifecycleOwner, effect = {
                val observer = LifecycleEventObserver{_,event->
                    if(event == Lifecycle.Event.ON_START && !isImagePermissionGranted() ){
                        imagePermissionResultLauncher.launch(when(buildVersion>=33){true-> READ_MEDIA_IMAGES false-> READ_EXTERNAL_STORAGE})
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            })

            Lofter_DownloaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                        MyComposableScreen()
                }
            }
        }
    }
    @Composable
    fun MyComposableScreen(
        viewModel: MainViewModel = MainViewModel(application)
    ) {
        var url by remember { mutableStateOf("") }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Lofter Downloader",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(Color.Red, Color.Blue)),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp
                ),
                modifier = Modifier
                    .padding(top = 15.dp, bottom = 25.dp)
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("输入网址") },
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Button(
                onClick = {
                    if (url.isBlank()) {
                        Toast.makeText(this@MainActivity, "请输入内容", Toast.LENGTH_SHORT).show()
                    } else if (!url.contains(" lofter.com/") && !url.contains("Lofter.com/")) {
                        Toast.makeText(this@MainActivity, "请输入Lofter链接", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        viewModel.lofterLoadData(url)
                    }
                    url = ""
                },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text("Go!")
            }
        }
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(bottom = 10.dp)
        ) {
            Text(
                text = "by KilluaDev",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                modifier = Modifier
            )
        }
    }

    private fun isImagePermissionGranted():Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            when(Build.VERSION.SDK_INT>=33){true-> READ_MEDIA_IMAGES false-> READ_EXTERNAL_STORAGE}
        ) == PackageManager.PERMISSION_GRANTED
    }
//    fun openApplicationSettings(){
//        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS, Uri.fromParts("package",packageName,null)).also { startActivity(it) }
//    }
//    fun decideCurrentPermissionStatus(imagePermissionGranted:Boolean,shouldShowPermissionRationale: Boolean):String{
//        return if (imagePermissionGranted) "Granted"
//        else if (shouldShowPermissionRationale) "Rejected"
//        else "Denied"
//    }
}
