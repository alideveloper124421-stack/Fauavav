package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_directory")
data class CustomerProfile(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val customerName: String,
  val phoneNumber: String,
  val cylinderNumber: String,
  val companyName: String,
  val rating: String = CustomerRating.NORMAL.displayName,
  val mapsLink: String = "",
  val createdAt: Long = System.currentTimeMillis()
)
