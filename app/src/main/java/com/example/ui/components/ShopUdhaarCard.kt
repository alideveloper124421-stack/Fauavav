package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopUdhaarRecord
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.UdhaarRedContainer
import com.example.ui.util.IntentHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShopUdhaarCard(
  record: ShopUdhaarRecord,
  onConfirmPayment: (ShopUdhaarRecord) -> Unit,
  onDelete: (ShopUdhaarRecord) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val now = System.currentTimeMillis()
  val isOverdue = !record.isPaid && now > record.deadlineTimestamp

  val deadlineText = remember(record.deadlineTimestamp) {
    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(record.deadlineTimestamp))
  }

  val paidText = remember(record.confirmedPaidAt) {
    record.confirmedPaidAt?.let {
      SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(it))
    } ?: ""
  }

  val daysRemainingInHistory = remember(record.confirmedPaidAt) {
    record.confirmedPaidAt?.let { paidAt ->
      val expiresAt = paidAt + (7L * 24L * 60L * 60L * 1000L)
      val diffDays = ((expiresAt - now) / (24L * 60L * 60L * 1000L)).coerceAtLeast(0)
      diffDays
    } ?: 7L
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .testTag("shop_udhaar_card_${record.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header: Customer Name and Amount
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = record.customerName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = record.phoneNumber,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Amount Badge
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (record.isPaid) PaidGreenContainer else UdhaarRedContainer
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = if (record.isPaid) "SETTLED" else "UDHAAR",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.Bold,
              color = if (record.isPaid) PaidGreen else UdhaarRed
            )
            Text(
              text = "Rs. ${String.format("%,.0f", record.udhaarAmount)}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (record.isPaid) PaidGreen else UdhaarRed
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status Info Row
      if (!record.isPaid) {
        // Pending Status: Deadline & Reminder
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isOverdue) UdhaarRedContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = if (isOverdue) Icons.Default.NotificationsActive else Icons.Default.AccessTime,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = if (isOverdue) UdhaarRed else MaterialTheme.colorScheme.primary
            )
            Text(
              text = if (isOverdue) "OVERDUE! Reminder was set for $deadlineText" else "Due on: $deadlineText (${record.reminderLabel})",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium,
              color = if (isOverdue) UdhaarRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        // History Status: When Paid & 7-Day Auto-delete Countdown
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = PaidGreenContainer.copy(alpha = 0.35f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = PaidGreen
              )
              Text(
                text = "Paid on $paidText",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = PaidGreen
              )
            }

            Text(
              text = "Auto-deletes in $daysRemainingInHistory d",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(12.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (!record.isPaid) {
          // REQUIRED FEATURE: "Confirm Payment" button on active list
          Button(
            onClick = { onConfirmPayment(record) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = PaidGreen,
              contentColor = Color.White
            ),
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
              .testTag("confirm_payment_button_${record.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Confirm Payment",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold
            )
          }

          // Call button
          IconButton(
            onClick = { IntentHelper.dialPhoneNumber(context, record.phoneNumber) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(),
            modifier = Modifier.size(42.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = "Call Customer",
              tint = MaterialTheme.colorScheme.primary
            )
          }

          // WhatsApp button
          IconButton(
            onClick = {
              IntentHelper.openWhatsApp(
                context,
                record.phoneNumber,
                record.customerName,
                record.udhaarAmount
              )
            },
            colors = IconButtonDefaults.filledTonalIconButtonColors(),
            modifier = Modifier.size(42.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Chat,
              contentDescription = "WhatsApp Customer",
              tint = PaidGreen
            )
          }
        } else {
          // History items: Kept for 7 days reference, or manual delete
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Kept for 7 days reference",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            IconButton(
              onClick = { onDelete(record) },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete record",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }
}
