package com.amcodebase.myapplication

import android.content.Context
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.File
import java.util.zip.ZipFile

class ExploreJarReflection {

    fun loadDexClassLoader(context: Context, jarPath: String): DexClassLoader {
        val optimizedDir = context.getDir("dex_opt", Context.MODE_PRIVATE).absolutePath

        return DexClassLoader(
            jarPath,
            optimizedDir,
            null,
            context.classLoader
        )
    }

    fun listClassNamesFromJar(jarPath: String): List<String> {
        val classNames = mutableListOf<String>()
        val zipFile = ZipFile(jarPath)

        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val name = entry.name

            if (name.endsWith(".class") && !name.contains("META-INF")) {
                // Convert path to fully qualified class name
                val className = name.removeSuffix(".class").replace("/", ".")
                classNames.add(className)
            }
        }
        return classNames
    }

    fun reflectOnClass(classLoader: ClassLoader, className: String) {
        try {
            val clazz = classLoader.loadClass(className)

            Log.d("JAR_REFLECT", "Class: $className")

            // List declared methods
            clazz.declaredMethods.forEach {
                Log.d("JAR_REFLECT", "Method: ${it.name} - Parameters: ${it.parameterTypes.joinToString()}")
            }

            // List constructors
            clazz.declaredConstructors.forEach {
                Log.d("JAR_REFLECT", "Constructor: $it")
            }

            // List fields
            clazz.declaredFields.forEach {
                Log.d("JAR_REFLECT", "Field: ${it.name} - Type: ${it.type}")
            }

            // List interfaces and superclass
            Log.d("JAR_REFLECT", "Superclass: ${clazz.superclass?.name}")
            clazz.interfaces.forEach {
                Log.d("JAR_REFLECT", "Implements: ${it.name}")
            }

        } catch (e: Exception) {
            Log.e("JAR_REFLECT", "Error reflecting on $className: ${e.message}")
        }
    }

    fun isJarPossiblyEncrypted(file: File): Boolean {
        return try {
            val zip = ZipFile(file)
            val entries = zip.entries()
            var checked = 0

            while (entries.hasMoreElements() && checked < 5) { // Limit checks for performance
                val entry = entries.nextElement()
                if (entry.name.endsWith(".class")) {
                    val inputStream = zip.getInputStream(entry)
                    val magicBytes = inputStream.readNBytes(4)
                    inputStream.close()

                    // Check if the class file starts with 0xCAFEBABE
                    val isValidClass = magicBytes[0] == 0xCA.toByte() &&
                            magicBytes[1] == 0xFE.toByte() &&
                            magicBytes[2] == 0xBA.toByte() &&
                            magicBytes[3] == 0xBE.toByte()

                    if (!isValidClass) {
                        return true // Possibly encrypted or invalid class file
                    }

                    checked++
                }
            }

            false // All checked class files appear valid
        } catch (e: Exception) {
            true // Any error indicates potential corruption or encryption
        }
    }

}