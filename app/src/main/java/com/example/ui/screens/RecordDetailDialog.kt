package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CustomerRating
import com.example.data.model.DeliveryRecord
import com.example.ui.components.RatingBadge
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.UdhaarRedContainer
import com.example.ui.util.IntentHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordDetailDialog(
  record: DeliveryRecord,
  onDismiss: () -> Unit,
  onDelete: (DeliveryRecord) -> Unit,
  onSettlePayment: (DeliveryRecord, Double) -> Unit
) {
  val context = LocalContext.current
  val rating = CustomerRating.fromDisplayName(record.rating)
  val isPaid = record.pendingUdhaar <= 0.01
  var showDeleteConfirm by remember { mutableStateOf(false) }
  var showPaymentInput by remember { mutableStateOf(false) }
  var paymentAmountText by remember { mutableStateOf("") }
  val dateFormatted = remember(record.createdAt) {
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(record.createdAt))
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(vertical = 24.dp)
        .testTag("record_detail_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Delivery Record Details",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = dateFormatted,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider()

        // Customer Info
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = record.customerName,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold
            )
            RatingBadge(rating = rating)
          }
          Text(
            text = record.phoneNumber,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Cylinder & Company Section
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.LocalGasStation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Text(
                text = record.companyName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = "Cylinder #${record.cylinderNumber}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Financial Ledger Summary
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isPaid) PaidGreenContainer.copy(alpha = 0.4f) else UdhaarRedContainer.copy(alpha = 0.4f)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Total Real Rate:", style = MaterialTheme.typography.bodyMedium)
              Text(
                "Rs. ${String.format("%,.0f", record.realRate)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Amount Received:", style = MaterialTheme.typography.bodyMedium)
              Text(
                "Rs. ${String.format("%,.0f", record.amountReceived)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = PaidGreen
              )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isPaid) "Status:" else "Pending Udhaar:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isPaid) "PAID (Settled)" else "Rs. ${String.format("%,.0f", record.pendingUdhaar)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPaid) PaidGreen else UdhaarRed
              )
            }
          }
        }

        // Google Maps Link & Navigation Action (Mandatory per user spec)
        Button(
          onClick = {
            val destination = if (record.mapsLink.isNotBlank()) record.mapsLink else record.customerName
            IntentHelper.openGoogleMaps(context, destination, record.customerName)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("dialog_open_maps_button")
        ) {
          Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Open Location in Google Maps",
            fontWeight = FontWeight.Bold
          )
        }

        // Contact buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = { IntentHelper.dialPhoneNumber(context, record.phoneNumber) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Call")
          }
          OutlinedButton(
            onClick = {
              IntentHelper.openWhatsApp(
                context,
                record.phoneNumber,
                record.customerName,
                record.pendingUdhaar
              )
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp), tint = PaidGreen)
            Spacer(modifier = Modifier.width(6.dp))
            Text("WhatsApp")
          }
        }

        // Receive Payment Action (if Udhaar pending)
        if (!isPaid) {
          Button(
            onClick = { showPaymentInput = !showPaymentInput },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.secondary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("receive_payment_button")
          ) {
            Icon(imageVector = Icons.Default.Payment, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (showPaymentInput) "Hide Payment Form" else "Receive Udhaar Payment")
          }

          AnimatedVisibility(visible = showPaymentInput) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = paymentAmountText,
                onValueChange = { paymentAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Payment Received (Rs.)") },
                placeholder = { Text("e.g. ${String.format("%.0f", record.pendingUdhaar)}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("payment_amount_input")
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    paymentAmountText = String.format(Locale.US, "%.0f", record.pendingUdhaar)
                  },
                  modifier = Modifier.weight(1f)
                ) {
                  Text("Pay Full")
                }
                Button(
                  onClick = {
                    val amount = paymentAmountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                      onSettlePayment(record, amount)
                      paymentAmountText = ""
                      showPaymentInput = false
                    }
                  },
                  modifier = Modifier.weight(1f)
                ) {
                  Text("Save Payment")
                }
              }
            }
          }
        }

        // Delete button
        TextButton(
          onClick = { showDeleteConfirm = true },
          colors = ButtonDefaults.textButtonColors(contentColor = UdhaarRed),
          modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Delete This Record")
        }
      }
    }
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("Delete Delivery Record?") },
      text = { Text("Are you sure you want to delete the record for ${record.customerName}?") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirm = false
            onDelete(record)
          },
          colors = ButtonDefaults.buttonColors(containerColor = UdhaarRed)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
