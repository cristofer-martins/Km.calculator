package com.km.kmcalculator

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Esta variável agora controla o estado do botão
    private var isServiceActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputIdeal = findViewById<EditText>(R.id.inputIdeal)
        val inputMin = findViewById<EditText>(R.id.inputMin)
        val btnToggle = findViewById<Button>(R.id.btnToggle)

        btnToggle.setOnClickListener {
            if (isServiceActive) {
                // Para o serviço e volta o botão para o estado original (Verde)
                stopService(Intent(this, OverlayService::class.java))
                btnToggle.text = "LIGAR CALCULADORA"
                btnToggle.setBackgroundColor(Color.parseColor("#4CAF50")) 
                isServiceActive = false
                Toast.makeText(this, "Calculadora Desativada", Toast.LENGTH_SHORT).show()
            } else {
                // Verifica as permissões necessárias
                if (!Settings.canDrawOverlays(this)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intent)
                } else if (!isNotificationServiceEnabled()) {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                } else {
                    // Envia os valores e muda o botão para o estado ativo (Vermelho)
                    val intent = Intent(this, OverlayService::class.java)
                    intent.putExtra("ideal", inputIdeal.text.toString().toFloatOrNull() ?: 0f)
                    intent.putExtra("min", inputMin.text.toString().toFloatOrNull() ?: 0f)
                    startService(intent)

                    btnToggle.text = "DESATIVAR CALCULADORA"
                    btnToggle.setBackgroundColor(Color.parseColor("#F44336")) 
                    isServiceActive = true
                }
            }
        }
    }

    // Função simplificada para verificar permissão
    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }
}