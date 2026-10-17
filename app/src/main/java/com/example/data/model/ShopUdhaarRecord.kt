package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_udhaar_records")
data class ShopUdhaarRecord(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val customerName: String,
  val phoneNumber: String,
  val udhaarAmount: Double,
  val createdAt: Long = System.currentTimeMillis(),
  val deadlineTimestamp: Long,
  val reminderLabel: String, // e.g. "1 Day", "2 Days", "5 Days", "Custom Date"
  val isPaid: Boolean = false,
  val confirmedPaidAt: Long? = null,
  val notes: String = ""
)
