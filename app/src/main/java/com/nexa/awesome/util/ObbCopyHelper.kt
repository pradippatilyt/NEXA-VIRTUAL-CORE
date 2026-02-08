package com.nexa.awesome.util

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File

object ObbCopyHelper {

    fun copyGameObbs(context: Context, callback: (Boolean, String) -> Unit) {
        Thread {
            try {
                // Real path: /storage/emulated/0/Android/obb
                val root = Environment.getExternalStorageDirectory()
                val realObbDir = File(root, "Android/obb")
                // Fake path: /storage/emulated/0/SdCard/Android/obb
                // User said: /storage/emulated/0/SdCard is the fake directory root
                val fakeObbDir = File(root, "SdCard/Android/obb")

                Log.d("ObbCopyHelper", "Source: ${realObbDir.absolutePath}")
                Log.d("ObbCopyHelper", "Dest: ${fakeObbDir.absolutePath}")

                if (!realObbDir.exists() || !realObbDir.isDirectory) {
                    postCallback(callback, false, "Real OBB folder not found: ${realObbDir.absolutePath}")
                    return@Thread
                }

                if (!fakeObbDir.exists()) {
                    if (!fakeObbDir.mkdirs()) {
                        postCallback(callback, false, "Failed to create fake OBB folder: ${fakeObbDir.absolutePath}")
                        return@Thread
                    }
                }

                val subDirs = realObbDir.listFiles()
                if (subDirs == null) {
                    postCallback(callback, false, "Failed to list real OBB directory (Permission?)")
                    return@Thread
                }

                var copyCount = 0
                var errorCount = 0

                for (dir in subDirs) {
                    if (dir.isDirectory) {
                        // Check if it's a game (has .obb files)
                        val obbFiles = dir.listFiles { _, name -> name.endsWith(".obb", ignoreCase = true) }
                        
                        // Strict check: User said "sirf game ka obb copy kare". 
                        // If it has .obb, we consider it a game.
                        if (obbFiles != null && obbFiles.isNotEmpty()) {
                            val targetDir = File(fakeObbDir, dir.name)
                            if (!targetDir.exists()) {
                                targetDir.mkdirs()
                            }

                            for (obb in obbFiles) {
                                val targetFile = File(targetDir, obb.name)
                                // Copy if not exists or size differs
                                if (!targetFile.exists() || targetFile.length() != obb.length()) {
                                    try {
                                        obb.copyTo(targetFile, overwrite = true)
                                    } catch (e: Exception) {
                                        Log.e("ObbCopyHelper", "Failed to copy ${obb.name}", e)
                                        errorCount++
                                    }
                                }
                            }
                            copyCount++
                        }
                    }
                }
                
                val msg = if (errorCount > 0) {
                    "Copied OBBs for $copyCount games (with $errorCount errors)"
                } else {
                    "Successfully copied OBBs for $copyCount games"
                }
                postCallback(callback, true, msg)

            } catch (e: Exception) {
                e.printStackTrace()
                postCallback(callback, false, "Error: ${e.message}")
            }
        }.start()
    }

    private fun postCallback(callback: (Boolean, String) -> Unit, success: Boolean, msg: String) {
        Handler(Looper.getMainLooper()).post {
            callback(success, msg)
        }
    }

    fun hasObb(context: Context, packageName: String): Boolean {
        try {
            val root = Environment.getExternalStorageDirectory()
            val realObbDir = File(root, "Android/obb/$packageName")
            
            if (realObbDir.exists() && realObbDir.isDirectory) {
                val obbFiles = realObbDir.listFiles { _, name -> name.endsWith(".obb", ignoreCase = true) }
                return obbFiles != null && obbFiles.isNotEmpty()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
