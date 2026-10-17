package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class ShopUdhaarReminderReceiver : BroadcastReceiver() {

  companion object {
    const val CHANNEL_ID = "shop_udhaar_reminders_channel"
    const val CHANNEL_NAME = "Shop Udhaar Reminders"
    const val EXTRA_RECORD_ID = "extra_record_id"
    const val EXTRA_CUSTOMER_NAME = "extra_customer_name"
    const val EXTRA_AMOUNT = "extra_amount"
  }

  override fun onReceive(context: Context, intent: Intent) {
    val recordId = intent.getLongExtra(EXTRA_RECORD_ID, 0L)
    val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: "Customer"
    val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)

    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create Notification Channel for Android 8.0+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts for due customer shop credit payments"
        enableLights(true)
        enableVibration(true)
      }
      notificationManager.createNotificationChannel(channel)
    }

    // Launch app on tap
    val launchIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("open_tab", "shop_udhaar")
    }

    val contentPendingIntent = PendingIntent.getActivity(
      context,
      recordId.toInt(),
      launchIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val formattedAmount = String.format("%,.0f", amount)
    // Notification message as requested: "Payment of [Amount] is due from [Customer Name]"
    val message = "Payment of Rs. $formattedAmount is due from $customerName"

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Shop Udhaar Due Reminder")
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(contentPendingIntent)
      .build()

    notificationManager.notify(recordId.toInt(), notification)
  }
}
