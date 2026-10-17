package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object NotificationHelper {

  fun scheduleReminder(
    context: Context,
    recordId: Long,
    customerName: String,
    amount: Double,
    triggerAtMillis: Long
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val intent = Intent(context, ShopUdhaarReminderReceiver::class.java).apply {
      putExtra(ShopUdhaarReminderReceiver.EXTRA_RECORD_ID, recordId)
      putExtra(ShopUdhaarReminderReceiver.EXTRA_CUSTOMER_NAME, customerName)
      putExtra(ShopUdhaarReminderReceiver.EXTRA_AMOUNT, amount)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      recordId.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
          alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
          )
        } else {
          alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
          )
        }
      } else {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      }
    } catch (e: SecurityException) {
      // Fallback to standard alarm if exact alarm permission is missing
      alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        pendingIntent
      )
    }
  }

  fun cancelReminder(context: Context, recordId: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, ShopUdhaarReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      recordId.toInt(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }
}
