package com.example.prueba_app.ui.theme.camara

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@Composable
fun PantallaCamara(onExit: () -> Unit) { // Recibimos la función de salida
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado para saber si tenemos permiso
    var tienePermisoCamara by remember { mutableStateOf(false) }

    // Lanzador para solicitar permiso
    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { esConcedido ->
            tienePermisoCamara = esConcedido
            if (!esConcedido) {
                Toast.makeText(context, "Se requiere permiso de cámara", Toast.LENGTH_LONG).show()
            }
        }
    )

    // Verificar permiso al iniciar
    LaunchedEffect(Unit) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheckResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            tienePermisoCamara = true
        } else {
            lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    if (tienePermisoCamara) {
        VistaCamara(context = context, lifecycleOwner = lifecycleOwner, onExit = onExit)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Esperando permiso de cámara...")
            // Opción de salir también si no hay permiso
            Button(onClick = onExit, modifier = Modifier.padding(top = 16.dp)) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
fun VistaCamara(context: Context, lifecycleOwner: LifecycleOwner, onExit: () -> Unit) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Casos de uso de CameraX
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    val executor = ContextCompat.getMainExecutor(context)

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. VISTA PREVIA (AndroidView)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val provider = cameraProviderFuture.get()

                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                previewView
            },
            update = { previewView ->
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                try {
                    val provider = cameraProviderFuture.get()
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    // Ignorar errores de binding rápido
                }
            }
        )

        // 2. BOTÓN SALIR
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd) // Esquina superior derecha
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape) // Fondo semitransparente
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar cámara",
                tint = Color.White
            )
        }

        // 3. CONTROLES (Botón captura y cambio de cámara)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Cambiar Cámara
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                            CameraSelector.LENS_FACING_FRONT
                        else
                            CameraSelector.LENS_FACING_BACK
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Cameraswitch, contentDescription = "Cambiar cámara", tint = Color.White)
                }

                // Botón Capturar
                IconButton(
                    onClick = {
                        capturarFoto(context, imageCapture, executor)
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape)
                        .border(4.dp, Color.Gray, CircleShape)
                ) {
                    Icon(Icons.Default.Camera, contentDescription = "Tomar foto", tint = Color.Black, modifier = Modifier.size(40.dp))
                }

                // Espaciador para equilibrar
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

// Función auxiliar para capturar y guardar
private fun capturarFoto(context: Context, imageCapture: ImageCapture?, executor: Executor) {
    if (imageCapture == null) return

    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ComidaPeruanaApp")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Error al guardar foto: ${exc.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Toast.makeText(context, "Foto guardada en Galería", Toast.LENGTH_SHORT).show()
            }
        }
    )
}