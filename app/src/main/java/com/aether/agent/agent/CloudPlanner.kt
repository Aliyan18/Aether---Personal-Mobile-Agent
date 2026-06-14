package com.aether.agent.agent

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class CloudPlanner {
    private val client = OkHttpClient()
    private val workerUrl = "https://aether-planner.rajaaliyanahmed.workers.dev"
    private val tag = "CloudPlanner"

    interface PlanCallback {
        fun onSuccess(plan: Plan)
        fun onError(error: String)
    }

    data class Step(val action: String, val params: Map<String, String>)
    data class Plan(val steps: List<Step>)

    fun getPlan(triggerText: String, callback: PlanCallback) {
        val json = JSONObject().apply {
            put("triggerText", triggerText)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(workerUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(tag, "Network error: ${e.message}")
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) {
                    callback.onError("HTTP ${response.code}")
                    return
                }

                try {
                    val steps = parsePlan(responseBody)
                    callback.onSuccess(Plan(steps))
                } catch (e: Exception) {
                    callback.onError("Parse error: ${e.message}")
                }
            }
        })
    }

    private fun parsePlan(jsonString: String): List<Step> {
        val json = JSONObject(jsonString)
        val stepsArray = json.getJSONArray("steps")
        val steps = mutableListOf<Step>()

        for (i in 0 until stepsArray.length()) {
            val stepObj = stepsArray.getJSONObject(i)
            val action = stepObj.getString("action")
            val paramsObj = stepObj.optJSONObject("params")
            val params = mutableMapOf<String, String>()
            paramsObj?.keys()?.forEach { key ->
                params[key] = paramsObj.getString(key)
            }
            steps.add(Step(action, params))
        }
        return steps
    }
}
