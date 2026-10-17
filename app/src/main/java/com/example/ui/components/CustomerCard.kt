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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.data.model.CustomerProfile
import com.example.data.model.CustomerRating
import com.example.ui.theme.PaidGreen
import com.example.ui.util.IntentHelper

@Composable
fun CustomerCard(
  customer: CustomerProfile,
  onNewDeliveryClick: (CustomerProfile) -> Unit,
  onEditClick: (CustomerProfile) -> Unit,
  onDeleteClick: (CustomerProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val rating = CustomerRating.fromDisplayName(customer.rating)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .testTag("customer_card_${customer.id}"),
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
      // Header: Customer Name, Phone, and Rating
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = customer.customerName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = customer.phoneNumber,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.width(8.dp))
        RatingBadge(rating = rating)
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
              text = customer.companyName,
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
            text = "Cylinder #${customer.cylinderNumber}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(12.dp))

      // Primary Quick Action: "New Delivery" Button (User requirement)
      Button(
        onClick = { onNewDeliveryClick(customer) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("customer_new_delivery_button_${customer.id}")
      ) {
        Icon(
          imageVector = Icons.Default.ShoppingCart,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "New Delivery (Auto-fill)",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Secondary Utility Bar: Maps, Call, WhatsApp, Edit, Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Maps
        OutlinedButton(
          onClick = {
            val destination = if (customer.mapsLink.isNotBlank()) customer.mapsLink else customer.customerName
            IntentHelper.openGoogleMaps(context, destination, customer.customerName)
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("customer_maps_button_${customer.id}")
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Maps",
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
          )
        }

        // Call
        IconButton(
          onClick = { IntentHelper.dialPhoneNumber(context, customer.phoneNumber) },
          colors = IconButtonDefaults.filledTonalIconButtonColors(),
          modifier = Modifier.size(38.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Call",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
        }

        // WhatsApp
        IconButton(
          onClick = {
            IntentHelper.openWhatsApp(context, customer.phoneNumber, customer.customerName, 0.0)
          },
          colors = IconButtonDefaults.filledTonalIconButtonColors(),
          modifier = Modifier.size(38.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = "WhatsApp",
            tint = PaidGreen,
            modifier = Modifier.size(18.dp)
          )
        }

        // Edit
        IconButton(
          onClick = { onEditClick(customer) },
          colors = IconButtonDefaults.filledTonalIconButtonColors(),
          modifier = Modifier.size(38.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Profile",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Delete
        IconButton(
          onClick = { onDeleteClick(customer) },
          colors = IconButtonDefaults.filledTonalIconButtonColors(),
          modifier = Modifier.size(38.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
