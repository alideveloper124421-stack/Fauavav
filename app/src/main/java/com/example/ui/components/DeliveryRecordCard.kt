package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
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
import com.example.data.model.CustomerRating
import com.example.data.model.DeliveryRecord
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.UdhaarRedContainer
import com.example.ui.util.IntentHelper

@Composable
fun DeliveryRecordCard(
  record: DeliveryRecord,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val rating = CustomerRating.fromDisplayName(record.rating)
  val isPaid = record.pendingUdhaar <= 0.01

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag("delivery_record_card_${record.id}"),
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
      // Top Row: Customer Name, Phone, and Rating Badge
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
            color = MaterialTheme.colorScheme.onSurface,
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

        Spacer(modifier = Modifier.width(8.dp))

        // Rating Chip
        RatingBadge(rating = rating)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Cylinder Information Tags
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Company Tag
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
              text = record.companyName,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Cylinder Number Tag
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ) {
          Text(
            text = "Cylinder #${record.cylinderNumber}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(12.dp))

      // Financial Details Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Real Rate",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Rs. ${String.format("%,.0f", record.realRate)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        Column {
          Text(
            text = "Received",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Rs. ${String.format("%,.0f", record.amountReceived)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = PaidGreen
          )
        }

        // Udhaar Badge
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isPaid) PaidGreenContainer else UdhaarRedContainer
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = if (isPaid) "PAID" else "PENDING UDHAAR",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.Bold,
              color = if (isPaid) PaidGreen else UdhaarRed
            )
            Text(
              text = if (isPaid) "Settled" else "Rs. ${String.format("%,.0f", record.pendingUdhaar)}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (isPaid) PaidGreen else UdhaarRed
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Direct Google Maps Clickable Button (as requested by user)
        Button(
          onClick = {
            val target = if (record.mapsLink.isNotBlank()) record.mapsLink else record.customerName
            IntentHelper.openGoogleMaps(context, target, record.customerName)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .testTag("open_maps_button_${record.id}")
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Google Maps",
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Open in Maps",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
          )
        }

        // Call Action Button
        IconButton(
          onClick = {
            IntentHelper.dialPhoneNumber(context, record.phoneNumber)
          },
          colors = IconButtonDefaults.filledTonalIconButtonColors(),
          modifier = Modifier
            .size(42.dp)
            .testTag("call_button_${record.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Call Customer",
            tint = MaterialTheme.colorScheme.primary
          )
        }

        // WhatsApp Action Button
        IconButton(
          onClick = {
            IntentHelper.openWhatsApp(
              context,
              record.phoneNumber,
              record.customerName,
              record.pendingUdhaar
            )
          },
          colors = IconButtonDefaults.filledTonalIconButtonColors(),
          modifier = Modifier
            .size(42.dp)
            .testTag("whatsapp_button_${record.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = "WhatsApp Customer",
            tint = PaidGreen
          )
        }
      }
    }
  }
}

@Composable
fun RatingBadge(rating: CustomerRating) {
  val backgroundColor = Color(rating.badgeColor).copy(alpha = 0.12f)
  val textColor = Color(rating.badgeColor)

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor)
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Star,
        contentDescription = null,
        modifier = Modifier.size(13.dp),
        tint = textColor
      )
      Text(
        text = rating.displayName,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
    }
  }
}
