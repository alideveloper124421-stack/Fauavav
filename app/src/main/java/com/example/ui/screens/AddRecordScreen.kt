package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.UdhaarRedContainer
import com.example.ui.util.IntentHelper
import com.example.ui.viewmodel.DeliveryViewModel
import com.example.ui.viewmodel.ReminderChoice
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecordScreen(
  viewModel: DeliveryViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.resetForm()
    onNavigateBack()
  }

  val context = LocalContext.current
  val formState by viewModel.formState.collectAsStateWithLifecycle()
  val scrollState = rememberScrollState()
  var showDatePickerDialog by remember { mutableStateOf(false) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (!isGranted) {
      Toast.makeText(
        context,
        "Notification permission is needed to receive payment due alerts.",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  val deadlineFormatted = remember(formState.targetDeadlineMillis) {
    SimpleDateFormat("EEEE, dd MMM yyyy - hh:mm a", Locale.getDefault())
      .format(Date(formState.targetDeadlineMillis))
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Create Delivery Order",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
        },
        navigationIcon = {
          IconButton(
            onClick = {
              viewModel.resetForm()
              onNavigateBack()
            },
            modifier = Modifier.testTag("back_button_order")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

      // Auto-fill confirmation notice if coming from Customer Directory
      if (formState.customerId != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = PaidGreenContainer.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = PaidGreen,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Auto-filled from Customer Directory! (Udhaar tracked separately)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = PaidGreen
            )
          }
        }
      }

      // SECTION 1: Customer & Cylinder Details
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "Customer & Delivery Info",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          // Customer Name
          OutlinedTextField(
            value = formState.customerName,
            onValueChange = { viewModel.updateCustomerName(it) },
            label = { Text("Customer Name *") },
            placeholder = { Text("e.g. Ali Raza Khan") },
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            isError = formState.nameError != null,
            supportingText = formState.nameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("order_customer_name_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Phone Number
          OutlinedTextField(
            value = formState.phoneNumber,
            onValueChange = { viewModel.updatePhoneNumber(it) },
            label = { Text("Phone Number *") },
            placeholder = { Text("e.g. 03001234567") },
            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = formState.phoneError != null,
            supportingText = formState.phoneError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("order_phone_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Company Name
          OutlinedTextField(
            value = formState.companyName,
            onValueChange = { viewModel.updateCompanyName(it) },
            label = { Text("Company Name *") },
            placeholder = { Text("e.g. PSO Gas, Pak Gas") },
            leadingIcon = { Icon(imageVector = Icons.Default.Business, contentDescription = null) },
            isError = formState.companyError != null,
            supportingText = formState.companyError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          // Cylinder Number
          Column {
            Text(
              text = "Cylinder Number *",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              viewModel.availableCylinderNumbers.forEach { num ->
                val isSelected = formState.cylinderNumber == num
                FilterChip(
                  selected = isSelected,
                  onClick = { viewModel.updateCylinderNumber(num) },
                  label = {
                    Text(
                      text = if (num == "Custom") "Custom" else "#$num",
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                  ),
                  shape = RoundedCornerShape(10.dp)
                )
              }
            }

            AnimatedVisibility(visible = formState.cylinderNumber == "Custom") {
              Column(modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                  value = formState.customCylinderNumber,
                  onValueChange = { viewModel.updateCustomCylinderNumber(it) },
                  label = { Text("Enter Custom Cylinder Number") },
                  placeholder = { Text("e.g. 35, 50, 11.8") },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp)
                )
              }
            }
          }

          // Maps Link
          if (formState.mapsLink.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Text("Saved Location attached", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Button(
                  onClick = { IntentHelper.openGoogleMaps(context, formState.mapsLink) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Text("Preview", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
          }
        }
      }

      // SECTION 2: Payment & Udhaar (Khata) Section
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "Payment & Udhaar Calculation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          // Real Rate (Total Price)
          OutlinedTextField(
            value = formState.realRate,
            onValueChange = { viewModel.updateRealRate(it) },
            label = { Text("Real Rate / Total Price (Rs.) *") },
            placeholder = { Text("e.g. 5000") },
            leadingIcon = { Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = formState.rateError != null,
            supportingText = formState.rateError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("order_real_rate_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Amount Received
          OutlinedTextField(
            value = formState.amountReceived,
            onValueChange = { viewModel.updateAmountReceived(it) },
            label = { Text("Amount Received (Rs.)") },
            placeholder = { Text("e.g. 2500 (or leave 0 for full Udhaar)") },
            leadingIcon = { Icon(imageVector = Icons.Default.Savings, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("order_amount_received_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Auto-Calculate Balance Card
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (formState.calculatedBalance > 0.01) UdhaarRedContainer.copy(alpha = 0.5f) else PaidGreenContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Remaining Udhaar Balance", style = MaterialTheme.typography.labelMedium)
                Text("Real Rate - Amount Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Text(
                text = "Rs. ${String.format("%,.0f", formState.calculatedBalance)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (formState.calculatedBalance > 0.01) UdhaarRed else PaidGreen
              )
            }
          }

          // Manual Udhaar Override
          OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                  Text("Manual Udhaar Entry", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }

                Switch(
                  checked = formState.isManualUdhaarActive,
                  onCheckedChange = { viewModel.toggleManualUdhaar(it) }
                )
              }

              Text(
                text = "Directly type the total pending amount if you don't want to use auto-calculate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              AnimatedVisibility(visible = formState.isManualUdhaarActive) {
                OutlinedTextField(
                  value = formState.manualUdhaar,
                  onValueChange = { viewModel.updateManualUdhaar(it) },
                  label = { Text("Manual Pending Amount (Rs.)") },
                  placeholder = { Text("e.g. 3000 or 5000") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  singleLine = true,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_manual_udhaar_input"),
                  shape = RoundedCornerShape(10.dp)
                )
              }
            }
          }
        }
      }

      // SECTION 3: Reminder Timer & Notifications
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Payment Reminder Timer / Deadline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = "Select when you want to receive an automatic phone reminder if Udhaar remains pending:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Selection Chips: 1 Day, 2 Days, 5 Days, Custom Date
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ReminderChoice.entries.forEach { choice ->
              val isSelected = formState.reminderChoice == choice
              FilterChip(
                selected = isSelected,
                onClick = {
                  viewModel.updateReminderChoice(choice)
                  if (choice == ReminderChoice.CUSTOM_DATE) {
                    showDatePickerDialog = true
                  }
                },
                label = {
                  Text(
                    text = choice.label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                leadingIcon = {
                  if (choice == ReminderChoice.CUSTOM_DATE) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                  }
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }

          AnimatedVisibility(visible = formState.reminderChoice == ReminderChoice.CUSTOM_DATE) {
            OutlinedButton(
              onClick = { showDatePickerDialog = true },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Change Custom Date & Time")
            }
          }

          // Live Scheduled Alert Preview
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Text("Scheduled Notification:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }
              Text("Exact Alert Time: $deadlineFormatted", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
              Text(
                text = "\"Payment of Rs. ${String.format("%,.0f", formState.calculatedBalance)} is due from ${if (formState.customerName.isNotBlank()) formState.customerName else "Customer"}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Save Order CTA Button
      Button(
        onClick = {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
          viewModel.saveOrder(context, onSuccess = onNavigateBack)
        },
        enabled = !formState.isSaving,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("save_order_button")
      ) {
        if (formState.isSaving) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        } else {
          Icon(imageVector = Icons.Default.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Save Delivery & Udhaar Order",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Material 3 DatePickerDialog for Custom Date
  if (showDatePickerDialog) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = formState.customDateTimestamp
    )

    DatePickerDialog(
      onDismissRequest = { showDatePickerDialog = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { selectedUtc ->
              val cal = Calendar.getInstance().apply {
                timeInMillis = selectedUtc
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
              }
              viewModel.updateCustomDateTimestamp(cal.timeInMillis)
            }
            showDatePickerDialog = false
          }
        ) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePickerDialog = false }) {
          Text("Cancel")
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }
}
