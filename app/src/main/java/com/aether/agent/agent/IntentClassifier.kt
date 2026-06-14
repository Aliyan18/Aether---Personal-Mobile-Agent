package com.aether.agent.agent

class IntentClassifier {
    data class IntentResult(val action: String, val confidence: Float, val params: Map<String, String>)

    fun classify(text: String): IntentResult {
        val lower = text.lowercase()
        return when {
            lower.contains("meeting") && (lower.contains("start") || lower.contains("in")) ->
                IntentResult("mute_phone", 0.95f, emptyMap())
            lower.contains("end") && lower.contains("meeting") ->
                IntentResult("unmute_phone", 0.90f, emptyMap())
            else -> IntentResult("ignore", 0.0f, emptyMap())
        }
    }
}
