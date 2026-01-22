package com.km.kmcalculator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat

class OverlayService : NotificationListenerService() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private lateinit var params: WindowManager.LayoutParams
    private var idealLimit: Float = 0f
    private var minLimit: Float = 0f

    companion object {
        var isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Pega os limites definidos na MainActivity
        idealLimit = intent?.getFloatExtra("ideal", 0f) ?: 0f
        minLimit = intent?.getFloatExtra("min", 0f) ?: 0f

        // Se o valor vier do KMAccessibilityService, atualiza o texto direto
        val valorVindoDaAcessibilidade = intent?.getFloatExtra("valor_km_acess", -1f) ?: -1f
        if (valorVindoDaAcessibilidade > 0) {
            processarResultado(valorVindoDaAcessibilidade)
        }

        if (!isRunning) {
            isRunning = true
            startInForeground()
            showOverlay()
        }

        return START_STICKY
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return

        // Filtra os apps de mobilidade
        if (packageName.contains("ubercab") || packageName.contains("taxis.99") || packageName.contains("indriver")) {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val fullContent = "$title $text"

            val valor = extrairValor(fullContent)
            val km = extrairKm(fullContent)

            if (valor != null && km != null && km > 0) {
                processarResultado(valor / km)
            }
        }
    }

    private fun processarResultado(resultadoKm: Float) {
        val corHex = when {
            resultadoKm >= idealLimit -> "#4CAF50" // Verde
            resultadoKm >= minLimit -> "#FFC107"   // Amarelo
            else -> "#F44336"                      // Vermelho
        }
        atualizarTextoOverlay(String.format("R$ %.2f/km", resultadoKm), corHex)
    }

    private fun extrairValor(texto: String): Float? {
        val regexMoney = Regex("""(?:R\$\s?|S\$\s?|[\$])?\s?(\d+(?:[.,]\d{1,2})?)""")
        val matches = regexMoney.findAll(texto)
        return matches.map { it.groupValues[1].replace(",", ".").toFloatOrNull() }
            .filterNotNull()
            .firstOrNull()
    }

    private fun extrairKm(texto: String): Float? {
        // Tenta buscar KM
        val regexKm = Regex("""(\d+(?:[.,]\d{1,2})?)\s?km""", RegexOption.IGNORE_CASE)
        val matchKm = regexKm.find(texto)
        if (matchKm != null) {
            return matchKm.groupValues[1].replace(",", ".").toFloatOrNull()
        }

        // Tenta buscar Metros (m) e converte para KM
        val regexMeters = Regex("""(\d+)\s?m\b""", RegexOption.IGNORE_CASE)
        val matchMeters = regexMeters.find(texto)
        if (matchMeters != null) {
            val metros = matchMeters.groupValues[1].toFloatOrNull() ?: 0f
            return metros / 1000f
        }
        return null
    }

    private fun atualizarTextoOverlay(texto: String, corHex: String) {
        Handler(Looper.getMainLooper()).post {
            try {
                overlayView?.findViewById<TextView>(R.id.txtIdealValue)?.apply {
                    this.text = texto
                    this.setTextColor(Color.parseColor(corHex))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_layout, null)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 200

        overlayView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(overlayView, params)
                        return true
                    }
                }
                return false
            }
        })

        // Clique longo para fechar rápido
        overlayView?.setOnLongClickListener {
            Toast.makeText(this, "Encerrando monitor...", Toast.LENGTH_SHORT).show()
            stopSelf()
            true
        }

        windowManager.addView(overlayView, params)
    }

    private fun startInForeground() {
        val channelId = "overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Monitor KM", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("KM Calculator Ativo")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
        isRunning = false
    }
}