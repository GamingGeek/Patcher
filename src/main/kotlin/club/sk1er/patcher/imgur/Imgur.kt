/*
 * Copyright © 2020 by Sk1er LLC
 *
 * All rights reserved.
 *
 * Sk1er LLC
 * 444 S Fulton Ave
 * Mount Vernon, NY
 * sk1er.club
 */

package club.sk1er.patcher.imgur

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class Imgur(private val clientId: String) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun upload(file: File): String {
        val fileContent = withContext(Dispatchers.IO) { file.readBytes() }
        val data = Base64.getEncoder().encodeToString(fileContent)
        val encodedParams = "image=" + withContext(Dispatchers.IO) { URLEncoder.encode(data, "UTF-8") }

        return withContext(Dispatchers.IO) {
            val connection = URL("https://api.imgur.com/3/image").openConnection() as HttpURLConnection
            connection.doOutput = true
            connection.doInput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Client-ID $clientId")
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.connect()

            connection.outputStream.bufferedWriter().use { it.write(encodedParams) }
            if (connection.responseCode != 200) {
                error("Imgur responded with ${connection.responseCode}")
            }

            val parser = JsonParser()
            connection.inputStream.reader().use {
                val imgurJson = parser.parse(it).asJsonObject
                val dataJson = imgurJson.getAsJsonObject("data")
                dataJson.get("link").asString
            }
        }
    }
}
