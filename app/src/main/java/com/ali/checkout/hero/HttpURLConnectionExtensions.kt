package com.ali.checkout.hero

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.nio.charset.Charset

fun HttpURLConnection.postJson(body: Any): String {
    val gson = Gson()

    try {
        this.setRequestProperty("Content-Type", "application/json")
        this.setRequestProperty("Accept", "*/*")
        this.instanceFollowRedirects = false
        this.requestMethod = "POST"
        this.doOutput = true
        this.doInput = true
        this.setChunkedStreamingMode(0)

        this.outputStream.use { os ->
            val input: ByteArray = gson.toJson(body).toByteArray(Charset.forName("utf-8"))
            os.write(input, 0, input.size)
        }

        val responseCode = this.responseCode
        val stream = if (responseCode < 300) this.inputStream else this.errorStream
        val text = StringBuilder()

        BufferedReader(InputStreamReader(stream, "utf-8")).use { br ->
            var responseLine: String?
            while (br.readLine().also { responseLine = it } != null) {
                text.append(responseLine!!.trim { it <= ' ' })
            }
        }

        val result = text.toString()
        return result
    } finally {
        this.disconnect()
    }
}
