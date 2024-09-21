package com.aplicandointent.intentapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aplicandointent.intentapp.databinding.ActivitySecondBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Solicitar permiso para realizar llamadas
    private val callPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                makePhoneCall()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
                    Toast.makeText(this, "El permiso es necesario para hacer llamadas.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso denegado permanentemente. Habilítalo en la configuración.", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
            }
        }

    @SuppressLint("SetTextI18n", "QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Inicializa el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Ajuste de insets para pantallas edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Navegación entre Actividades
        val message = intent.getStringExtra("EXTRA_MESSAGE")
        binding.welcomeText.text = message ?: "¡Bienvenido a la Segunda Actividad!"

        // 4. Realizar una llamada telefónica
        binding.btnCall.setOnClickListener {
            val phoneNumber = binding.phoneNumberEditText.text.toString()
            if (phoneNumber.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall() // Ya tiene el permiso, realizar la llamada
                } else {
                    callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese un número de teléfono.", Toast.LENGTH_SHORT).show()
            }
        }


        // 5. Abrir una página web con URL
        binding.openWebButton.setOnClickListener {
            val url = binding.urlEditText.text.toString()
            if (url.isNotEmpty()) {
                val webIntent = Intent(Intent.ACTION_VIEW)
                webIntent.data = Uri.parse(if (url.startsWith("http://") || url.startsWith("https://")) url else "http://$url")
                startActivity(webIntent)
            } else {
                Toast.makeText(this, "Por favor, ingrese una URL válida.", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Enviar un correo electrónico
        binding.btnSendEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822" // MIME type para manejar correos electrónicos
                putExtra(Intent.EXTRA_EMAIL, arrayOf("destinatario@ejemplo.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Asunto del correo")
                putExtra(Intent.EXTRA_TEXT, "Cuerpo del correo")
            }

            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, "Elige una app de correo"))
            } else {
                Toast.makeText(this, "No hay aplicaciones de correo instaladas.", Toast.LENGTH_SHORT).show()
            }
        }

        // 7. Compartir imagen desde un ImageView
        binding.btnShareImage.setOnClickListener {
            val drawable = binding.imageView.drawable as? BitmapDrawable
            if (drawable != null) {
                val bitmap = drawable.bitmap

                val imageUri = saveImageToCache(bitmap)

                if (imageUri != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(shareIntent, "Compartir Imagen"))
                } else {
                    Toast.makeText(this, "Error al compartir la imagen.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No hay imagen para compartir.", Toast.LENGTH_SHORT).show()
            }
        }



        // 8. Obtener la ubicación del dispositivo usando Google Maps
        binding.btnGetLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val geoUri = "geo:$latitude,$longitude?q=$latitude,$longitude"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                        intent.setPackage("com.google.android.apps.maps") // Abrir Google Maps
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
        }

        // 9. Abrir la configuración del dispositivo
        binding.btnOpenSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }

        // 10. Reproducir un video de YouTube atraves del id
        binding.btnPlayYouTube.setOnClickListener {
            val videoId = binding.etYouTubeId.text.toString()
            if (videoId.isNotEmpty()) {
                // Crear la URL completa utilizando solo el ID
                val youtubeUrl = "https://www.youtube.com/watch?v=$videoId"
                val youtubeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
                startActivity(youtubeIntent)
            } else {
                Toast.makeText(this, "Por favor, ingrese un ID de video de YouTube.", Toast.LENGTH_SHORT).show()
            }
        }


    }

    // Función para realizar la llamada telefónica
    private fun makePhoneCall() {
        val phoneNumber = binding.phoneNumberEditText.text.toString()
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
    }

    private fun saveImageToCache(bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(applicationContext.cacheDir, "images")
            cachePath.mkdirs() // Crear la carpeta si no existe
            val file = File(cachePath, "image_to_share.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            // Obtener el URI de la imagen usando FileProvider
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.btnGetLocation.performClick()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
}
