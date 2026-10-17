package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.data.model.DeliveryOrder
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.UdhaarRedContainer
import com.example.ui.util.IntentHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeliveryOrderCard(
  order: DeliveryOrder,
  onConfirmPayment: (DeliveryOrder) -> Unit,
  onDelete: (DeliveryOrder) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val now = System.currentTimeMillis()
  val isOverdue = !order.isPaid && now > order.deadlineTimestamp

  val deadlineText = remember(order.deadlineTimestamp) {
    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.deadlineTimestamp))
  }

  val paidText = remember(order.confirmedPaidAt) {
    order.confirmedPaidAt?.let {
      SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(it))
    } ?: ""
  }

  val daysRemainingInHistory = remember(order.confirmedPaidAt) {
    order.confirmedPaidAt?.let { paidAt ->
      val expiresAt = paidAt + (7L * 24L * 60L * 60L * 1000L)
      val diffDays = ((expiresAt - now) / (24L * 60L * 60L * 1000L)).coerceAtLeast(0)
      diffDays
    } ?: 7L
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .testTag("delivery_order_card_${order.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header: Customer Name and Cylinder Details
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = order.customerName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = order.phoneNumber,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Udhaar Badge
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (order.isPaid) PaidGreenContainer else UdhaarRedContainer
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = if (order.isPaid) "PAID" else "PENDING UDHAAR",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.Bold,
              color = if (order.isPaid) PaidGreen else UdhaarRed
            )
            Text(
              text = if (order.isPaid) "Settled" else "Rs. ${String.format("%,.0f", order.pendingUdhaar)}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (order.isPaid) PaidGreen else UdhaarRed
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Cylinder & Company Tags
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocalGasStation,
              contentDescription = null,
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = order.companyName,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ) {
          Text(
            text = "Cylinder #${order.cylinderNumber}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Financial Breakdown
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Total Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Rs. ${String.format("%,.0f", order.realRate)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Column {
          Text("Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Rs. ${String.format("%,.0f", order.amountReceived)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PaidGreen)
        }
        Column {
          Text("Udhaar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Rs. ${String.format("%,.0f", order.pendingUdhaar)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (order.isPaid) PaidGreen else UdhaarRed)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status info: Pending Reminder or 7-Day History Countdown
      if (!order.isPaid) {
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
              text = if (isOverdue) "OVERDUE! Reminder was set for $deadlineText" else "Payment Reminder: $deadlineText (${order.reminderLabel})",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium,
              color = if (isOverdue) UdhaarRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = PaidGreen)
              Text("Paid on $paidText", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PaidGreen)
            }
            Text("Auto-deletes in $daysRemainingInHistory d", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        if (!order.isPaid) {
          // Confirm Payment Button (User requirement)
          Button(
            onClick = { onConfirmPayment(order) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = PaidGreen,
              contentColor = Color.White
            ),
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
              .testTag("confirm_order_payment_${order.id}")
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Confirm Payment", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
          }
        }

        // Maps Button
        OutlinedButton(
          onClick = {
            val destination = if (order.mapsLink.isNotBlank()) order.mapsLink else order.customerName
            IntentHelper.openGoogleMaps(context, destination, order.customerName)
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.height(42.dp)
        ) {
          Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Maps", style = MaterialTheme.typography.labelMedium)
        }

        // Call
        IconButton(
          onClick = { IntentHelper.dialPhoneNumber(context, order.phoneNumber) },
          colors = IconButtonDefaults.filledTonalIconButtonColors(),
          modifier = Modifier.size(42.dp)
        ) {
          Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
        }

        // WhatsApp
        IconButton(
          onClick = {
            IntentHelper.openWhatsApp(context, order.phoneNumber, order.customerName, order.pendingUdhaar)
          },
          colors = IconButtonDefaults.filledTonalIconButtonColors(),
          modifier = Modifier.size(42.dp)
        ) {
          Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp", tint = PaidGreen)
        }

        // Delete (if in history)
        if (order.isPaid) {
          IconButton(
            onClick = { onDelete(order) },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
          }
        }
      }
    }
  }
}
