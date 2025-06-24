package com.amcodebase.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        printJarFileContents(this)
        jarFileContentReflection(this)
    }
}

fun printJarFileContents(context: Context) {
    val jarPath = File(context.filesDir, "myplugin.jar").absolutePath
    val jarContents = ExploreJarClass().listJarContents(jarPath)

    jarContents.forEach { entry ->
        Log.d("JAR_ENTRY", entry)
    }
}

fun jarFileContentReflection(context: Context) {
    val exploreJarReflection = ExploreJarReflection()
    val jarFile = File(context.filesDir, "plugin.jar")
    val isEncrypted = exploreJarReflection.isJarPossiblyEncrypted(jarFile)
    if (isEncrypted) {
        Log.e("JAR", "JAR file appears to be encrypted or invalid.")
    } else {
        val classLoader = exploreJarReflection.loadDexClassLoader(context, jarFile.absolutePath)
        val classNames = exploreJarReflection.listClassNamesFromJar(jarFile.absolutePath)

        classNames.forEach { className ->
            exploreJarReflection.reflectOnClass(classLoader, className)
        }
    }

}