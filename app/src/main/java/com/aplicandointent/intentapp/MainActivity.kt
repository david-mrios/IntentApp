package com.aplicandointent.intentapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aplicandointent.intentapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Mostrar un Mensaje en un TextView
        binding.intentText.text = "¡Este es un mensaje dinámico desde Kotlin!"

        // 2. Navegación entre Actividades
        binding.buttonOpenSecondActivity.setOnClickListener {
            Toast.makeText(this, "Permiso denegado para hacer llamadas.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
        // 3. Compartir Contenido
        binding.buttonShare.setOnClickListener {
            val message = binding.textViewMessage.text.toString()

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }

            val chooser = Intent.createChooser(shareIntent, "Compartir usando:")
            startActivity(chooser)
        }
    }
}