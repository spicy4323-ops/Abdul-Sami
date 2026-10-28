package com.example.actions

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

sealed class ActionResult {
    data class Success(val message: String, val appOpened: String? = null) : ActionResult()
    data class RequiresConfirmation(
        val actionTitle: String,
        val details: String,
        val onConfirm: () -> ActionResult
    ) : ActionResult()
    data class Error(val message: String) : ActionResult()
}

class DeviceActionEngine(private val context: Context) {
    private val tag = "DeviceActionEngine"

    fun openYouTube(): ActionResult {
        return launchAppWithFallback(
            packageName = "com.google.android.youtube",
            fallbackUri = "https://www.youtube.com",
            appName = "YouTube"
        )
    }

    fun openChrome(): ActionResult {
        return launchAppWithFallback(
            packageName = "com.android.chrome",
            fallbackUri = "https://www.google.com",
            appName = "Google Chrome"
        )
    }

    fun openCamera(): ActionResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ActionResult.Success("Opening Camera...", "Camera")
            } else {
                ActionResult.Error("No camera application found on this device.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to open camera", e)
            ActionResult.Error("Unable to open camera: ${e.localizedMessage}")
        }
    }

    fun openSettings(): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("Opening Device Settings...", "Settings")
        } catch (e: Exception) {
            Log.e(tag, "Failed to open settings", e)
            ActionResult.Error("Unable to open settings: ${e.localizedMessage}")
        }
    }

    fun openMaps(query: String = ""): ActionResult {
        return try {
            val uri = if (query.isNotBlank()) {
                Uri.parse("geo:0,0?q=${Uri.encode(query)}")
            } else {
                Uri.parse("geo:0,0")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("Opening Maps for '$query'", "Maps")
        } catch (e: Exception) {
            Log.e(tag, "Failed to open maps", e)
            searchWeb("maps $query")
        }
    }

    fun openDialer(phoneNumber: String = ""): ActionResult {
        return try {
            val uri = if (phoneNumber.isNotBlank()) {
                Uri.parse("tel:${Uri.encode(phoneNumber)}")
            } else {
                Uri.parse("tel:")
            }
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("Opening phone dialer...", "Phone")
        } catch (e: Exception) {
            Log.e(tag, "Failed to open dialer", e)
            ActionResult.Error("Unable to open phone dialer.")
        }
    }

    fun searchWeb(query: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ActionResult.Success("Searching web for '$query'...", "Web Search")
            } else {
                // Fallback to browser URL
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                ActionResult.Success("Searching web for '$query' in browser...", "Web Search")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to search web", e)
            ActionResult.Error("Failed to perform web search: ${e.localizedMessage}")
        }
    }

    fun prepareMessage(recipient: String = "", message: String): ActionResult {
        return ActionResult.RequiresConfirmation(
            actionTitle = "Send Message",
            details = "Draft to: ${if (recipient.isNotBlank()) recipient else "contact"}\nContent: $message",
            onConfirm = {
                try {
                    val uri = if (recipient.isNotBlank()) Uri.parse("smsto:${Uri.encode(recipient)}") else Uri.parse("smsto:")
                    val smsIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
                        putExtra("sms_body", message)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(smsIntent)
                    ActionResult.Success("Opening messaging app with draft ready to send.")
                } catch (e: Exception) {
                    ActionResult.Error("Unable to open messaging app: ${e.localizedMessage}")
                }
            }
        )
    }

    private fun launchAppWithFallback(
        packageName: String,
        fallbackUri: String,
        appName: String
    ): ActionResult {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ActionResult.Success("Opening $appName...", appName)
            } else {
                // Launch web fallback
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUri)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                ActionResult.Success("$appName app is not installed; opening web version in browser.", appName)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error launching $appName", e)
            ActionResult.Error("Failed to open $appName: ${e.localizedMessage}")
        }
    }
}
