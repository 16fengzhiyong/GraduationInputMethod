package com.nuc.omeletteinputmethod.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TranslateRepository
    @Inject
    constructor(
        @Named("plainClient") private val okHttpClient: OkHttpClient,
    ) {
        private val BASE_URL = "https://translate.google.cn/translate_a/single"
        private val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36"

        suspend fun translate(
            content: String,
            sourceLan: String = "auto",
            targetLan: String = "zh",
        ): String {
            return withContext(Dispatchers.IO) {
                if (content.isBlank()) return@withContext ""

                try {
                    // Construct URL manually to avoid complex Retrofit setup for this single dynamic endpoint
                    val url = "$BASE_URL?client=gtx&sl=$sourceLan&tl=$targetLan&dt=t&q=${URLEncoder.encode(content, "UTF-8")}"

                    val request =
                        Request
                            .Builder()
                            .url(url)
                            .header("User-Agent", USER_AGENT)
                            .build()

                    val response = okHttpClient.newCall(request).execute()
                    val body = response.body?.string() ?: return@withContext ""

                    // Parse Logic from legacy code:
                    // JSONArray jsonArray = new JSONArray(googleResult).getJSONArray(0);
                    // for (int i = 0; i < jsonArray.length(); i++) { result += jsonArray.getJSONArray(i).getString(0); }

                    var result = ""
                    val jsonArray = JSONArray(body).getJSONArray(0)
                    for (i in 0 until jsonArray.length()) {
                        result += jsonArray.getJSONArray(i).getString(0)
                    }
                    result
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Wait, Translation Error"
                }
            }
        }
    }
