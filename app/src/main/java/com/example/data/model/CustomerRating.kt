package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class CustomerRating(val displayName: String, val stars: Int, val badgeColor: Long) {
  EXCELLENT("Excellent", 5, 0xFF00796B),
  VERY_GOOD("Very Good", 4, 0xFF2E7D32),
  GOOD("Good", 3, 0xFF388E3C),
  NORMAL("Normal", 2, 0xFFF57C00),
  BAD("Bad", 1, 0xFFD32F2F),
  VERY_BAD("Very Bad", 0, 0xFFB71C1C);

  companion object {
    fun fromDisplayName(name: String): CustomerRating {
      return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: NORMAL
    }
  }
}
