package com.km.kmcalculator

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class KMAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!OverlayService.isRunning) return

        val rootNode = rootInActiveWindow ?: return
        val textosEncontrados = mutableListOf<String>()
        
        extrairTextosDaTela(rootNode, textosEncontrados)
        val textoCompleto = textosEncontrados.joinToString(" ")

        val valor = extrairValor(textoCompleto)
        // Agora buscamos todas as distâncias e somamos
        val listaDistancias = extrairTodasDistancias(textoCompleto)

        if (valor != null && listaDistancias.isNotEmpty()) {
            val distanciaTotal = listaDistancias.sum()
            if (distanciaTotal > 0) {
                val resultado = valor / distanciaTotal
                
                val intent = Intent(this, OverlayService::class.java)
                intent.putExtra("valor_km_acess", resultado)
                startService(intent)
            }
        }
    }

    private fun extrairTextosDaTela(node: AccessibilityNodeInfo?, lista: MutableList<String>) {
        if (node == null) return
        node.text?.let { if (it.isNotBlank()) lista.add(it.toString()) }
        for (i in 0 until node.childCount) {
            extrairTextosDaTela(node.getChild(i), lista)
        }
    }

    private fun extrairValor(texto: String): Float? {
        val regexMoney = Regex("""(?:R\$\s?|S\$\s?|[\$])?\s?(\d+(?:[.,]\d{1,2})?)""")
        return regexMoney.findAll(texto)
            .map { it.groupValues[1].replace(",", ".").toFloatOrNull() }
            .filterNotNull()
            .firstOrNull()
    }

    // Função aprimorada para somar (688m + 4.2km)
    private fun extrairTodasDistancias(texto: String): List<Float> {
        val distancias = mutableListOf<Float>()
        
        // Pega todos os KMs
        val regexKm = Regex("""(\d+(?:[.,]\d{1,2})?)\s?km""", RegexOption.IGNORE_CASE)
        regexKm.findAll(texto).forEach {
            it.groupValues[1].replace(",", ".").toFloatOrNull()?.let { km -> distancias.add(km) }
        }

        // Pega todos os Metros e converte
        val regexMeters = Regex("""(\d+)\s?m\b""", RegexOption.IGNORE_CASE)
        regexMeters.findAll(texto).forEach {
            it.groupValues[1].toFloatOrNull()?.let { m -> distancias.add(m / 1000f) }
        }
        
        return distancias
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
    }
} // Fechamento correto da classe aqui