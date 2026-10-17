package com.example.ui.screens

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
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.components.DeliveryOrderCard
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.UdhaarRed
import com.example.ui.viewmodel.DeliveryViewModel
import com.example.ui.viewmodel.OrderStats
import com.example.ui.viewmodel.OrderTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  viewModel: DeliveryViewModel,
  onNavigateToAdd: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val activeOrders by viewModel.filteredActiveOrders.collectAsStateWithLifecycle()
  val historyOrders by viewModel.filteredHistoryOrders.collectAsStateWithLifecycle()
  val stats by viewModel.stats.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

  val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

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
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.LocalGasStation,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
            Column {
              Text(
                text = "Orders & Udhaar Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Daily Deliveries & Temporary Credit",
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
        onClick = {
          viewModel.resetForm()
          onNavigateToAdd()
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("add_order_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "New Order")
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "New Order",
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
        OrdersStatsOverview(stats = stats)
      }

      // 2. Search Field
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.onSearchQueryChanged(it) },
          placeholder = { Text("Search orders by customer, phone, cylinder...") },
          leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
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
            .testTag("search_orders_input")
        )
      }

      // 3. Segmented Tabs: Active vs 7-Day History
      item {
        PrimaryTabRow(
          selectedTabIndex = if (currentTab == OrderTab.PENDING) 0 else 1,
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
          Tab(
            selected = currentTab == OrderTab.PENDING,
            onClick = { viewModel.setTab(OrderTab.PENDING) },
            text = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(imageVector = Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Active Orders (${stats.totalActiveOrders})", fontWeight = FontWeight.Bold)
              }
            },
            modifier = Modifier.testTag("tab_orders_active")
          )
          Tab(
            selected = currentTab == OrderTab.SEVEN_DAY_HISTORY,
            onClick = { viewModel.setTab(OrderTab.SEVEN_DAY_HISTORY) },
            text = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("7-Day History (${stats.settledCount7Days})", fontWeight = FontWeight.Bold)
              }
            },
            modifier = Modifier.testTag("tab_orders_history")
          )
        }
      }

      // 4. Tab Context Header
      item {
        if (currentTab == OrderTab.PENDING) {
          Text(
            text = "Pending Deliveries & Active Udhaar (Tap 'Confirm Payment' when cleared)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Text(
            text = "Settled Orders (Kept for 7 days reference, then automatically purged)",
            style = MaterialTheme.typography.labelMedium,
            color = PaidGreen
          )
        }
      }

      // 5. Orders List
      val ordersToShow = if (currentTab == OrderTab.PENDING) activeOrders else historyOrders

      if (ordersToShow.isEmpty()) {
        item {
          EmptyOrdersState(
            isHistory = currentTab == OrderTab.SEVEN_DAY_HISTORY,
            hasSearch = searchQuery.isNotBlank()
          )
        }
      } else {
        items(
          items = ordersToShow,
          key = { it.id }
        ) { order ->
          DeliveryOrderCard(
            order = order,
            onConfirmPayment = { confirmedOrder ->
              viewModel.confirmPayment(context, confirmedOrder)
            },
            onDelete = { orderToDelete ->
              viewModel.deleteOrder(context, orderToDelete)
            }
          )
        }
      }
    }
  }
}

@Composable
private fun OrdersStatsOverview(
  stats: OrderStats,
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Orders & Khata Balance",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${stats.totalActiveOrders} Active",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Collected
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Cash Collected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Rs. ${String.format("%,.0f", stats.totalCollected)}", style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp), fontWeight = FontWeight.Bold, color = PaidGreen)
          }
        }

        // Pending Udhaar
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Pending Udhaar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Rs. ${String.format("%,.0f", stats.totalPendingUdhaar)}", style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp), fontWeight = FontWeight.Bold, color = if (stats.totalPendingUdhaar > 0) UdhaarRed else PaidGreen)
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyOrdersState(
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
            imageVector = if (isHistory) Icons.Default.History else Icons.Default.LocalGasStation,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Text(
        text = when {
          hasSearch -> "No Matching Orders"
          isHistory -> "No Settled Orders in Last 7 Days"
          else -> "No Active Orders"
        },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )

      Text(
        text = when {
          hasSearch -> "Try adjusting your search query."
          isHistory -> "Confirmed paid deliveries stay here for 7 days before being automatically cleaned up."
          else -> "Tap '+ New Order' or click 'New Delivery' next to any customer in your Directory to create an order."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  }
}
