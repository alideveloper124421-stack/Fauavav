package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ShopUdhaarCard
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.UdhaarRed
import com.example.ui.viewmodel.ShopUdhaarStats
import com.example.ui.viewmodel.ShopUdhaarTab
import com.example.ui.viewmodel.ShopUdhaarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopUdhaarScreen(
  viewModel: ShopUdhaarViewModel,
  onNavigateToAdd: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val activeRecords by viewModel.filteredActiveRecords.collectAsStateWithLifecycle()
  val historyRecords by viewModel.filteredHistoryRecords.collectAsStateWithLifecycle()
  val stats by viewModel.stats.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(userMessage) {
    userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearUserMessage()
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Storefront,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
            Column {
              Text(
                text = "Shop Udhaar Khata",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Direct Customer Credit & Alerts",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        )
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onNavigateToAdd,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("add_shop_udhaar_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Shop Udhaar")
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "New Shop Udhaar",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Stats Summary Card
      item {
        ShopUdhaarStatsCard(stats = stats)
      }

      // 2. Search Field
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.onSearchQueryChanged(it) },
          placeholder = { Text("Search customer name or phone...") },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
          },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_shop_udhaar_input")
        )
      }

      // 3. Primary Tabs: Pending vs 7-Day History
      item {
        PrimaryTabRow(
          selectedTabIndex = if (currentTab == ShopUdhaarTab.PENDING) 0 else 1,
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
          Tab(
            selected = currentTab == ShopUdhaarTab.PENDING,
            onClick = { viewModel.setTab(ShopUdhaarTab.PENDING) },
            text = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(imageVector = Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Pending (${stats.pendingCount})", fontWeight = FontWeight.Bold)
              }
            },
            modifier = Modifier.testTag("tab_pending")
          )
          Tab(
            selected = currentTab == ShopUdhaarTab.SEVEN_DAY_HISTORY,
            onClick = { viewModel.setTab(ShopUdhaarTab.SEVEN_DAY_HISTORY) },
            text = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("7-Day History (${stats.recoveredCount7Days})", fontWeight = FontWeight.Bold)
              }
            },
            modifier = Modifier.testTag("tab_history")
          )
        }
      }

      // 4. Tab Header Description
      item {
        if (currentTab == ShopUdhaarTab.PENDING) {
          Text(
            text = "Active Shop Credit with Scheduled Notification Reminders",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Text(
            text = "Paid Shop Credit (Kept for 7 days reference, then auto-deleted)",
            style = MaterialTheme.typography.labelMedium,
            color = PaidGreen
          )
        }
      }

      // 5. Records List
      val recordsToShow = if (currentTab == ShopUdhaarTab.PENDING) activeRecords else historyRecords

      if (recordsToShow.isEmpty()) {
        item {
          EmptyShopRecordsState(
            isHistory = currentTab == ShopUdhaarTab.SEVEN_DAY_HISTORY,
            hasSearch = searchQuery.isNotBlank()
          )
        }
      } else {
        items(
          items = recordsToShow,
          key = { it.id }
        ) { record ->
          ShopUdhaarCard(
            record = record,
            onConfirmPayment = { confirmedRecord ->
              viewModel.confirmPayment(context, confirmedRecord)
            },
            onDelete = { recordToDelete ->
              viewModel.deleteRecord(context, recordToDelete)
            }
          )
        }
      }
    }
  }
}

@Composable
private fun ShopUdhaarStatsCard(
  stats: ShopUdhaarStats,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = "Shop Credit Overview",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Pending
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Total Active Udhaar",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Rs. ${String.format("%,.0f", stats.totalPendingAmount)}",
              style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
              fontWeight = FontWeight.Bold,
              color = UdhaarRed
            )
            Text(
              text = "${stats.pendingCount} customers pending",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Recovered this week
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Paid (Last 7 Days)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Rs. ${String.format("%,.0f", stats.totalRecovered7Days)}",
              style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
              fontWeight = FontWeight.Bold,
              color = PaidGreen
            )
            Text(
              text = "${stats.recoveredCount7Days} payments settled",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = PaidGreen
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyShopRecordsState(
  isHistory: Boolean,
  hasSearch: Boolean
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 48.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.padding(horizontal = 24.dp)
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(72.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = if (isHistory) Icons.Default.History else Icons.Default.Storefront,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Text(
        text = when {
          hasSearch -> "No Matching Records"
          isHistory -> "No Settled Records in Last 7 Days"
          else -> "No Active Shop Udhaar"
        },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )

      Text(
        text = when {
          hasSearch -> "Try adjusting your search query."
          isHistory -> "When you confirm payment on pending shop credit, records stay here for 7 days before auto-deleting."
          else -> "Tap '+ New Shop Udhaar' to log credit taken from the shop with automatic reminder notifications."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  }
}
