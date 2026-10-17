package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object IntentHelper {

  fun openGoogleMaps(context: Context, mapsLinkOrAddress: String, customerName: String = "") {
    if (mapsLinkOrAddress.isBlank()) {
      Toast.makeText(context, "No location link provided for this customer", Toast.LENGTH_SHORT).show()
      return
    }

    try {
      val trimmed = mapsLinkOrAddress.trim()
      val uri = when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> Uri.parse(trimmed)
        trimmed.startsWith("geo:") -> Uri.parse(trimmed)
        else -> Uri.parse("geo:0,0?q=${Uri.encode(trimmed)}")
      }

      // Try explicit Google Maps package first for the best native app experience
      val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      if (mapsIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapsIntent)
      } else {
        // Fallback to any maps/browser app
        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallbackIntent)
      }
    } catch (e: Exception) {
      // Fallback web url
      try {
        val webUri = if (mapsLinkOrAddress.startsWith("http")) {
          Uri.parse(mapsLinkOrAddress)
        } else {
          Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(mapsLinkOrAddress)}")
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
      } catch (ex: Exception) {
        Toast.makeText(context, "Could not open maps application", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun dialPhoneNumber(context: Context, phone: String) {
    if (phone.isBlank()) {
      Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
      return
    }
    try {
      val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:${phone.trim()}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(context, "Cannot open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  fun openWhatsApp(context: Context, phone: String, customerName: String, pendingUdhaar: Double) {
    if (phone.isBlank()) {
      Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
      return
    }
    try {
      val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
      val message = if (pendingUdhaar > 0.01) {
        "Assalam-o-Alaikum $customerName, your gas cylinder delivery record shows pending Udhaar balance of Rs. ${String.format("%.0f", pendingUdhaar)}. Thank you!"
      } else {
        "Assalam-o-Alaikum $customerName, thank you for your gas cylinder delivery order!"
      }
      val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(context, "Cannot open WhatsApp", Toast.LENGTH_SHORT).show()
    }
  }
}
