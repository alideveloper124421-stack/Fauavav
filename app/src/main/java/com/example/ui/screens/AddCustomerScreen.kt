package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomerRating
import com.example.ui.components.RatingBadge
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.UdhaarRed
import com.example.ui.theme.UdhaarRedContainer
import com.example.ui.util.IntentHelper
import com.example.ui.util.LocationHelper
import com.example.ui.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCustomerScreen(
  viewModel: CustomerViewModel,
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
  val clipboardManager = LocalClipboardManager.current
  var ratingDropdownExpanded by remember { mutableStateOf(false) }
  var showManualLocationInput by remember { mutableStateOf(false) }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (fineGranted || coarseGranted) {
      viewModel.fetchCurrentLocation(context)
    } else {
      Toast.makeText(
        context,
        "GPS permission is needed to automatically capture customer coordinates",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (formState.editingCustomerId != null) "Edit Customer Profile" else "New Customer Profile",
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
            modifier = Modifier.testTag("back_button_customer")
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

      // Card 1: Permanent Identity Details
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
            text = "Customer Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          // Customer Name
          OutlinedTextField(
            value = formState.customerName,
            onValueChange = { viewModel.updateCustomerName(it) },
            label = { Text("Customer Name *") },
            placeholder = { Text("e.g. Haji Muhammad Aslam") },
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            isError = formState.nameError != null,
            supportingText = formState.nameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("directory_customer_name_input"),
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
              .testTag("directory_phone_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Customer Rating: Dropdown menu with exact options:
          // Excellent, Very Good, Good, Normal, Bad, Very Bad
          Column {
            Text(
              text = "Customer Rating *",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
              OutlinedCard(
                onClick = { ratingDropdownExpanded = true },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("directory_rating_trigger"),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    RatingBadge(rating = formState.rating)
                    Text(
                      text = "Rating: ${formState.rating.displayName}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Medium
                    )
                  }
                  Icon(
                    imageVector = if (ratingDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                  )
                }
              }

              DropdownMenu(
                expanded = ratingDropdownExpanded,
                onDismissRequest = { ratingDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
              ) {
                CustomerRating.entries.forEach { ratingOption ->
                  DropdownMenuItem(
                    text = {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                      ) {
                        RatingBadge(rating = ratingOption)
                        Text(
                          text = ratingOption.displayName,
                          fontWeight = if (formState.rating == ratingOption) FontWeight.Bold else FontWeight.Normal
                        )
                      }
                    },
                    trailingIcon = {
                      if (formState.rating == ratingOption) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                      }
                    },
                    onClick = {
                      viewModel.updateRating(ratingOption)
                      ratingDropdownExpanded = false
                    }
                  )
                }
              }
            }
          }
        }
      }

      // Card 2: Cylinder & Company Defaults
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
            text = "Cylinder Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          // Company Name
          OutlinedTextField(
            value = formState.companyName,
            onValueChange = { viewModel.updateCompanyName(it) },
            label = { Text("Company Name *") },
            placeholder = { Text("Type custom company (e.g. PSO Gas, Shell Gas)") },
            leadingIcon = { Icon(imageVector = Icons.Default.Business, contentDescription = null) },
            isError = formState.companyError != null,
            supportingText = formState.companyError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("directory_company_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Suggestion Chips
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            viewModel.popularCompanies.forEach { company ->
              FilterChip(
                selected = formState.companyName.equals(company, ignoreCase = true),
                onClick = { viewModel.updateCompanyName(company) },
                label = { Text(company, style = MaterialTheme.typography.labelSmall) },
                shape = RoundedCornerShape(8.dp)
              )
            }
          }

          // Cylinder Number
          Column {
            Text(
              text = "Cylinder Number * (e.g. 20, 21, 22)",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

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
        }
      }

      // Card 3: Google Maps Location (with GPS button)
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
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Google Maps Location",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          // "Get Current Location" Button
          Button(
            onClick = {
              if (LocationHelper.hasLocationPermission(context)) {
                viewModel.fetchCurrentLocation(context)
              } else {
                locationPermissionLauncher.launch(
                  arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                  )
                )
              }
            },
            enabled = !formState.isFetchingLocation,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("directory_get_location_button")
          ) {
            if (formState.isFetchingLocation) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text("Detecting High-Accuracy GPS...", fontWeight = FontWeight.Bold)
            } else {
              Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (formState.mapsLink.isNotBlank()) "Re-fetch Current Location" else "Get Current Location",
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Error Banner
          AnimatedVisibility(visible = formState.locationError != null) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = UdhaarRedContainer.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = UdhaarRed, modifier = Modifier.size(20.dp))
                Text(
                  text = formState.locationError ?: "",
                  style = MaterialTheme.typography.bodySmall,
                  color = UdhaarRed,
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }

          // Location Captured Card
          AnimatedVisibility(visible = formState.mapsLink.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = PaidGreenContainer.copy(alpha = 0.45f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PaidGreen, modifier = Modifier.size(18.dp))
                  Text("Location Captured & Converted to Maps URL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PaidGreen)
                }

                if (formState.detectedCoordinates != null) {
                  Text(formState.detectedCoordinates ?: "", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Text(formState.mapsLink, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                  Button(
                    onClick = { IntentHelper.openGoogleMaps(context, formState.mapsLink) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen, contentColor = Color.White),
                    modifier = Modifier.height(34.dp)
                  ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview in Maps", style = MaterialTheme.typography.labelSmall)
                  }
                }
              }
            }
          }

          // Manual link toggle
          Text(
            text = if (showManualLocationInput) "Hide manual entry" else "Or type / paste link manually",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .clickable { showManualLocationInput = !showManualLocationInput }
              .padding(vertical = 4.dp)
          )

          AnimatedVisibility(visible = showManualLocationInput) {
            OutlinedTextField(
              value = formState.mapsLink,
              onValueChange = { viewModel.updateMapsLink(it) },
              label = { Text("Manual Google Maps Link / Lat,Lng") },
              placeholder = { Text("https://maps.google.com/?q=lat,lng") },
              leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
              trailingIcon = {
                IconButton(
                  onClick = {
                    val text = clipboardManager.getText()?.text
                    if (!text.isNullOrBlank()) {
                      viewModel.updateMapsLink(text)
                    }
                  }
                ) {
                  Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
              },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
            )
          }
        }
      }

      // Save Customer CTA Button
      Button(
        onClick = { viewModel.saveCustomer(onSuccess = onNavigateBack) },
        enabled = !formState.isSaving,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("save_customer_button")
      ) {
        if (formState.isSaving) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        } else {
          Icon(imageVector = Icons.Default.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (formState.editingCustomerId != null) "Update Customer Profile" else "Save Customer to Directory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
