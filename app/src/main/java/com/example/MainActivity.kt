package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AddCustomerScreen
import com.example.ui.screens.AddRecordScreen
import com.example.ui.screens.CustomerDirectoryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CustomerViewModel
import com.example.ui.viewmodel.DeliveryViewModel

enum class MainAppTab {
  DIRECTORY,       // Section 1: Customer Directory (Digital Diary)
  ORDERS_UDHAAR    // Section 2: Delivery & Udhaar Management
}

enum class AppNavScreen {
  MAIN,
  ADD_CUSTOMER,
  ADD_ORDER
}

class MainActivity : ComponentActivity() {

  private val customerViewModel: CustomerViewModel by viewModels()
  private val deliveryViewModel: DeliveryViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val openTabExtra = intent?.getStringExtra("open_tab")

    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          GasDeliveryMainApp(
            customerViewModel = customerViewModel,
            deliveryViewModel = deliveryViewModel,
            initialTab = if (openTabExtra == "shop_udhaar" || openTabExtra == "orders") MainAppTab.ORDERS_UDHAAR else MainAppTab.DIRECTORY
          )
        }
      }
    }
  }
}

@Composable
fun GasDeliveryMainApp(
  customerViewModel: CustomerViewModel,
  deliveryViewModel: DeliveryViewModel,
  initialTab: MainAppTab = MainAppTab.DIRECTORY,
  modifier: Modifier = Modifier
) {
  var currentTab by remember { mutableStateOf(initialTab) }
  var currentScreen by remember { mutableStateOf(AppNavScreen.MAIN) }

  val customers by customerViewModel.allCustomers.collectAsStateWithLifecycle()
  val orderStats by deliveryViewModel.stats.collectAsStateWithLifecycle()

  AnimatedContent(
    targetState = currentScreen,
    transitionSpec = {
      if (targetState != AppNavScreen.MAIN) {
        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
      } else {
        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
      }
    },
    label = "AppScreenTransition",
    modifier = modifier.fillMaxSize()
  ) { screen ->
    when (screen) {
      AppNavScreen.MAIN -> {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          bottomBar = {
            NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
              // Section 1: Customer Directory
              NavigationBarItem(
                selected = currentTab == MainAppTab.DIRECTORY,
                onClick = { currentTab = MainAppTab.DIRECTORY },
                icon = {
                  BadgedBox(
                    badge = {
                      if (customers.isNotEmpty()) {
                        Badge { Text("${customers.size}") }
                      }
                    }
                  ) {
                    Icon(
                      imageVector = if (currentTab == MainAppTab.DIRECTORY) Icons.Filled.Contacts else Icons.Outlined.Contacts,
                      contentDescription = "Customer Directory"
                    )
                  }
                },
                label = { Text("Directory", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("nav_tab_directory")
              )

              // Section 2: Orders & Udhaar Management
              NavigationBarItem(
                selected = currentTab == MainAppTab.ORDERS_UDHAAR,
                onClick = { currentTab = MainAppTab.ORDERS_UDHAAR },
                icon = {
                  BadgedBox(
                    badge = {
                      if (orderStats.totalActiveOrders > 0) {
                        Badge { Text("${orderStats.totalActiveOrders}") }
                      }
                    }
                  ) {
                    Icon(
                      imageVector = if (currentTab == MainAppTab.ORDERS_UDHAAR) Icons.Filled.LocalGasStation else Icons.Outlined.LocalGasStation,
                      contentDescription = "Orders & Udhaar"
                    )
                  }
                },
                label = { Text("Orders & Udhaar", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("nav_tab_orders_udhaar")
              )
            }
          }
        ) { paddingValues ->
          when (currentTab) {
            MainAppTab.DIRECTORY -> {
              CustomerDirectoryScreen(
                viewModel = customerViewModel,
                onNavigateToAddCustomer = { currentScreen = AppNavScreen.ADD_CUSTOMER },
                onNavigateToNewDelivery = { customer ->
                  // Auto-fill order form with customer details & location
                  deliveryViewModel.prefillFromCustomer(customer)
                  currentScreen = AppNavScreen.ADD_ORDER
                },
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
              )
            }
            MainAppTab.ORDERS_UDHAAR -> {
              DashboardScreen(
                viewModel = deliveryViewModel,
                onNavigateToAdd = { currentScreen = AppNavScreen.ADD_ORDER },
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
              )
            }
          }
        }
      }

      AppNavScreen.ADD_CUSTOMER -> {
        AddCustomerScreen(
          viewModel = customerViewModel,
          onNavigateBack = { currentScreen = AppNavScreen.MAIN }
        )
      }

      AppNavScreen.ADD_ORDER -> {
        AddRecordScreen(
          viewModel = deliveryViewModel,
          onNavigateBack = { currentScreen = AppNavScreen.MAIN }
        )
      }
    }
  }
}
