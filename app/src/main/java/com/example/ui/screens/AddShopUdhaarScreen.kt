package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.viewmodel.ReminderChoice
import com.example.ui.viewmodel.ShopUdhaarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddShopUdhaarScreen(
  viewModel: ShopUdhaarViewModel,
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
            text = "Add Shop Udhaar Record",
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
            modifier = Modifier.testTag("back_button_shop")
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

      // Main Form Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.size(32.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Storefront,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
            Text(
              text = "Customer & Udhaar Details",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          // Customer Name (Text input)
          OutlinedTextField(
            value = formState.customerName,
            onValueChange = { viewModel.updateCustomerName(it) },
            label = { Text("Customer Name *") },
            placeholder = { Text("e.g. Asad Ullah") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Person, contentDescription = null)
            },
            isError = formState.nameError != null,
            supportingText = formState.nameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("shop_customer_name_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Phone Number (Numeric input)
          OutlinedTextField(
            value = formState.phoneNumber,
            onValueChange = { viewModel.updatePhoneNumber(it) },
            label = { Text("Phone Number *") },
            placeholder = { Text("e.g. 03001234567") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Phone, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = formState.phoneError != null,
            supportingText = formState.phoneError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("shop_phone_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Udhaar Amount (Numeric input e.g. 500)
          OutlinedTextField(
            value = formState.udhaarAmount,
            onValueChange = { viewModel.updateUdhaarAmount(it) },
            label = { Text("Udhaar Amount (Rs.) *") },
            placeholder = { Text("e.g. 500") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = formState.amountError != null,
            supportingText = formState.amountError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("shop_udhaar_amount_input"),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      // Reminder Timer / Deadline Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier.size(32.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Schedule,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
              }
            }
            Text(
              text = "Reminder Timer / Deadline *",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "Select when you want to receive an automatic phone reminder:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Selection Chips: "1 Day", "2 Days", "5 Days", "Custom Date"
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
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("reminder_chip_${choice.name}")
              )
            }
          }

          // Custom Date Picker Trigger
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
                Icon(
                  imageVector = Icons.Default.NotificationsActive,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "Scheduled Push Notification Preview:",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Text(
                text = "Exact Alert Time: $deadlineFormatted",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
              )

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "\"Payment of Rs. ${if (formState.udhaarAmount.isNotBlank()) formState.udhaarAmount else "[Amount]"} is due from ${if (formState.customerName.isNotBlank()) formState.customerName else "[Customer Name]"}\"",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(10.dp)
                )
              }
            }
          }
        }
      }

      // Save CTA Button
      Button(
        onClick = {
          // Check notification permission for Android 13+
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
          viewModel.saveRecord(context, onSuccess = onNavigateBack)
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
          .testTag("save_shop_udhaar_button")
      ) {
        if (formState.isSaving) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Icon(imageVector = Icons.Default.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Save & Schedule Reminder",
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
              // Preserve time of day (or set to 10:00 AM)
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
