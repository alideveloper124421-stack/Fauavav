package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_records")
data class DeliveryRecord(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val customerName: String,
  val phoneNumber: String,
  val mapsLink: String,
  val rating: String = CustomerRating.NORMAL.displayName,
  val companyName: String,
  val cylinderNumber: String,
  val realRate: Double,
  val amountReceived: Double,
  val pendingUdhaar: Double,
  val isManualUdhaar: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val notes: String = ""
)
