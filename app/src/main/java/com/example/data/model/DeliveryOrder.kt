package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_orders")
data class DeliveryOrder(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val customerId: Long? = null,
  val customerName: String,
  val phoneNumber: String,
  val cylinderNumber: String,
  val companyName: String,
  val mapsLink: String = "",
  val realRate: Double,
  val amountReceived: Double,
  val pendingUdhaar: Double,
  val isManualUdhaar: Boolean = false,
  val deadlineTimestamp: Long = System.currentTimeMillis() + 86400000L,
  val reminderLabel: String = "1 Day",
  val isPaid: Boolean = false,
  val confirmedPaidAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis()
)
