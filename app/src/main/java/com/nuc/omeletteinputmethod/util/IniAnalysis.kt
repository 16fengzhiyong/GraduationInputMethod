package com.nuc.omeletteinputmethod.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class IniAnalysis @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val path = "symbols"

    fun splitIni(rawlist: List<String>): List<String> {
        val values = mutableListOf<String>()
        for (value in rawlist) {
            val tmpStrings = value.split("\n")
            for (str in tmpStrings) {
                values.add(str)
            }
        }
        return values
    }

    @Throws(IOException::class)
    fun getValuesFromFile(path: String): List<String> {
        val inputStream: InputStream = context.assets.open(path)
        val values = mutableListOf<String>()
        val count = inputStream.available()
        val b = ByteArray(count)
        var totalRead = 0
        while (totalRead < count) {
            val bytesRead = inputStream.read(b, totalRead, count - totalRead)
            if (bytesRead == -1) break
            totalRead += bytesRead
        }
        values.add(String(b))
        val result = splitIni(values)
        inputStream.close()
        return result
    }
}