package org.stypox.dicio.skills.bridge

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dicio.skill.context.SkillContext
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.json.JSONObject
import org.stypox.dicio.sentences.Sentences
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class BridgeSkill(
    correspondingSkillInfo: BridgeInfo,
    data: StandardRecognizerData<Sentences.Bridge>
) : StandardRecognizerSkill<Sentences.Bridge>(correspondingSkillInfo, data) {

    private val TAG = "BridgeSkill"
    private val BRIDGE_URL = "http://127.0.0.1:8765/execute"
    private val TOKEN = "test-token-123456789012345678901234"

    override suspend fun generateOutput(ctx: SkillContext, inputData: Sentences.Bridge): BridgeOutput {
        return withContext(Dispatchers.IO) {
            val tool = inputData.tool ?: return@withContext BridgeOutput.Error("unknown", Exception("No tool specified"))
            val args = buildArgs(inputData)
            
            try {
                val result = callBridge(tool, args)
                BridgeOutput.Success(tool, result)
            } catch (e: Exception) {
                Log.e(TAG, "Bridge call failed for $tool", e)
                BridgeOutput.Error(tool, e)
            }
        }
    }

    private fun buildArgs(inputData: Sentences.Bridge): JSONObject {
        val args = JSONObject()
        inputData.captures?.forEach { (key, value) ->
            // Convert captures to appropriate types
            args.put(key, when {
                value.matches(Regex("^\\d+$")) -> value.toInt()
                value.matches(Regex("^\\d+\\.\\d+$")) -> value.toDouble()
                value.lowercase() in listOf("true", "false") -> value.toBoolean()
                else -> value
            })
        }
        return args
    }

    private fun callBridge(tool: String, args: JSONObject): String {
        val url = URL(BRIDGE_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $TOKEN")

        val requestBody = JSONObject().apply {
            put("tool", tool)
            put("args", args)
        }.toString()

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody)
            writer.flush()
        }

        val responseCode = connection.responseCode
        val inputStream = if (responseCode >= 400) connection.errorStream else connection.inputStream
        
        val response = BufferedReader(InputStreamReader(inputStream!!)).use { reader ->
            reader.readText()
        }

        if (responseCode != 200) {
            throw Exception("Bridge returned $responseCode: $response")
        }

        val json = JSONObject(response)
        val exit = json.getInt("exit")
        val output = json.optString("output", "")
        val error = json.optString("error", "")

        if (exit != 0) {
            throw Exception("Tool $tool failed (exit=$exit): $error")
        }

        return if (output.isNotBlank()) output else "OK"
    }
}