package com.amcodebase.myapplication

import java.io.File
import java.util.zip.ZipFile

class ExploreJarClass {
    fun listJarContents(jarFilePath: String): List<String> {
        val file = File(jarFilePath)
        if (!file.exists()) {
            throw IllegalArgumentException("JAR file not found at: $jarFilePath")
        } else if (isJarEncrypted(jarFilePath)) {
            throw IllegalArgumentException("JAR file is encrypted: $jarFilePath")
        }

        val contents = mutableListOf<String>()
        val zipFile = ZipFile(file)

        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            contents.add(entry.name)
        }

        return contents
    }

    fun isJarEncrypted(jarPath: String): Boolean {
        return try {
            val zip = ZipFile(File(jarPath))
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val inputStream = zip.getInputStream(entry)
                val bytes = inputStream.readNBytes(8)

                // Optional: Check for readable ASCII
                val readable = bytes.any { it.toInt() in 32..126 }
                if (!readable) return true
            }
            false
        } catch (e: Exception) {
            true // Exception likely means invalid/obfuscated/encrypted
        }
    }
}