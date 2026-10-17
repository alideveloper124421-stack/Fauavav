package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GasDeliveryDatabase
import com.example.data.model.CustomerProfile
import com.example.data.model.DeliveryOrder
import com.example.data.repository.DeliveryOrderRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OrderTab {
  PENDING,
  SEVEN_DAY_HISTORY
}

data class OrderFormState(
  val customerId: Long? = null,
  val customerName: String = "",
  val phoneNumber: String = "",
  val cylinderNumber: String = "20",
  val customCylinderNumber: String = "",
  val companyName: String = "",
  val mapsLink: String = "",
  val realRate: String = "",
  val amountReceived: String = "",
  val manualUdhaar: String = "",
  val isManualUdhaarActive: Boolean = false,
  val reminderChoice: ReminderChoice = ReminderChoice.ONE_DAY,
  val customDateTimestamp: Long = System.currentTimeMillis() + 86400000L * 2,
  val nameError: String? = null,
  val phoneError: String? = null,
  val companyError: String? = null,
  val rateError: String? = null,
  val isSaving: Boolean = false
) {
  val calculatedBalance: Double
    get() {
      if (isManualUdhaarActive && manualUdhaar.isNotBlank()) {
        return manualUdhaar.toDoubleOrNull() ?: 0.0
      }
      val rate = realRate.toDoubleOrNull() ?: 0.0
      val received = amountReceived.toDoubleOrNull() ?: 0.0
      return (rate - received).coerceAtLeast(0.0)
    }

  val effectiveCylinderNumber: String
    get() = if (cylinderNumber == "Custom") customCylinderNumber.trim() else cylinderNumber

  val targetDeadlineMillis: Long
    get() {
      return if (reminderChoice == ReminderChoice.CUSTOM_DATE) {
        customDateTimestamp
      } else {
        System.currentTimeMillis() + reminderChoice.durationMillis
      }
    }
}

data class OrderStats(
  val totalActiveOrders: Int = 0,
  val totalPendingUdhaar: Double = 0.0,
  val totalCollected: Double = 0.0,
  val totalSettled7Days: Double = 0.0,
  val settledCount7Days: Int = 0
)

class DeliveryViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: DeliveryOrderRepository
  private val SEVEN_DAYS_MILLIS = 7L * 24L * 60L * 60L * 1000L

  val availableCylinderNumbers = listOf("20", "21", "22", "23", "24", "25", "45", "Custom")
  val popularCompanies = listOf("PSO Gas", "Shell Gas", "Bharat Gas", "Indane", "HP Gas", "Pak Gas", "Total Gas")

  private val _currentTab = MutableStateFlow(OrderTab.PENDING)
  val currentTab: StateFlow<OrderTab> = _currentTab.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _formState = MutableStateFlow(OrderFormState())
  val formState: StateFlow<OrderFormState> = _formState.asStateFlow()

  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  init {
    val db = GasDeliveryDatabase.getDatabase(application, viewModelScope)
    repository = DeliveryOrderRepository(db.deliveryOrderDao())

    // Auto cleanup expired 7-day history
    cleanupExpiredHistory()
  }

  val activeOrders: StateFlow<List<DeliveryOrder>> = repository.activeOrders
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val historyOrders: StateFlow<List<DeliveryOrder>> = repository.getHistoryOrders(
    System.currentTimeMillis() - SEVEN_DAYS_MILLIS
  ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredActiveOrders: StateFlow<List<DeliveryOrder>> = combine(
    activeOrders,
    _searchQuery
  ) { orders, query ->
    if (query.isBlank()) orders
    else orders.filter {
      it.customerName.contains(query, ignoreCase = true) ||
        it.phoneNumber.contains(query, ignoreCase = true) ||
        it.companyName.contains(query, ignoreCase = true) ||
        it.cylinderNumber.contains(query, ignoreCase = true)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredHistoryOrders: StateFlow<List<DeliveryOrder>> = combine(
    historyOrders,
    _searchQuery
  ) { orders, query ->
    if (query.isBlank()) orders
    else orders.filter {
      it.customerName.contains(query, ignoreCase = true) ||
        it.phoneNumber.contains(query, ignoreCase = true) ||
        it.companyName.contains(query, ignoreCase = true) ||
        it.cylinderNumber.contains(query, ignoreCase = true)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val stats: StateFlow<OrderStats> = combine(activeOrders, historyOrders) { active, history ->
    val activeCount = active.size
    val pendingUdhaarSum = active.sumOf { it.pendingUdhaar }
    val collectedSum = active.sumOf { it.amountReceived }
    val settledSum = history.sumOf { it.realRate }
    val settledCount = history.size
    OrderStats(
      totalActiveOrders = activeCount,
      totalPendingUdhaar = pendingUdhaarSum,
      totalCollected = collectedSum,
      totalSettled7Days = settledSum,
      settledCount7Days = settledCount
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderStats())

  fun setTab(tab: OrderTab) {
    _currentTab.value = tab
  }

  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  // Quick Action: Auto-fill from Customer Profile when clicking "New Delivery"
  fun prefillFromCustomer(customer: CustomerProfile) {
    _formState.value = OrderFormState(
      customerId = customer.id,
      customerName = customer.customerName,
      phoneNumber = customer.phoneNumber,
      cylinderNumber = if (customer.cylinderNumber in listOf("20", "21", "22", "23", "24", "25", "45")) customer.cylinderNumber else "Custom",
      customCylinderNumber = if (customer.cylinderNumber in listOf("20", "21", "22", "23", "24", "25", "45")) "" else customer.cylinderNumber,
      companyName = customer.companyName,
      mapsLink = customer.mapsLink
    )
  }

  fun updateCustomerName(name: String) {
    _formState.value = _formState.value.copy(customerName = name, nameError = null)
  }

  fun updatePhoneNumber(phone: String) {
    _formState.value = _formState.value.copy(phoneNumber = phone, phoneError = null)
  }

  fun updateCylinderNumber(number: String) {
    _formState.value = _formState.value.copy(cylinderNumber = number)
  }

  fun updateCustomCylinderNumber(number: String) {
    _formState.value = _formState.value.copy(customCylinderNumber = number)
  }

  fun updateCompanyName(company: String) {
    _formState.value = _formState.value.copy(companyName = company, companyError = null)
  }

  fun updateMapsLink(link: String) {
    _formState.value = _formState.value.copy(mapsLink = link.trim())
  }

  fun updateRealRate(rate: String) {
    val clean = rate.filter { it.isDigit() || it == '.' }
    _formState.value = _formState.value.copy(realRate = clean, rateError = null)
  }

  fun updateAmountReceived(amount: String) {
    val clean = amount.filter { it.isDigit() || it == '.' }
    _formState.value = _formState.value.copy(amountReceived = clean)
  }

  fun updateManualUdhaar(manual: String) {
    val clean = manual.filter { it.isDigit() || it == '.' }
    _formState.value = _formState.value.copy(
      manualUdhaar = clean,
      isManualUdhaarActive = clean.isNotBlank()
    )
  }

  fun toggleManualUdhaar(active: Boolean) {
    _formState.value = _formState.value.copy(isManualUdhaarActive = active)
  }

  fun updateReminderChoice(choice: ReminderChoice) {
    _formState.value = _formState.value.copy(reminderChoice = choice)
  }

  fun updateCustomDateTimestamp(timestamp: Long) {
    _formState.value = _formState.value.copy(customDateTimestamp = timestamp)
  }

  fun resetForm() {
    _formState.value = OrderFormState()
  }

  fun saveOrder(context: Context, onSuccess: () -> Unit) {
    val state = _formState.value
    var hasError = false
    var nameErr: String? = null
    var phoneErr: String? = null
    var compErr: String? = null
    var rateErr: String? = null

    if (state.customerName.isBlank()) {
      nameErr = "Customer name is required"
      hasError = true
    }
    if (state.phoneNumber.isBlank()) {
      phoneErr = "Phone number is required"
      hasError = true
    }
    if (state.companyName.isBlank()) {
      compErr = "Company name is required"
      hasError = true
    }
    val rate = state.realRate.toDoubleOrNull()
    if (rate == null || rate <= 0) {
      rateErr = "Enter a valid order price"
      hasError = true
    }

    val cylNum = state.effectiveCylinderNumber
    if (cylNum.isBlank()) {
      compErr = "Cylinder number cannot be empty"
      hasError = true
    }

    if (hasError) {
      _formState.value = state.copy(
        nameError = nameErr,
        phoneError = phoneErr,
        companyError = compErr,
        rateError = rateErr
      )
      return
    }

    val received = state.amountReceived.toDoubleOrNull() ?: 0.0
    val finalUdhaar = if (state.isManualUdhaarActive && state.manualUdhaar.isNotBlank()) {
      state.manualUdhaar.toDoubleOrNull() ?: 0.0
    } else {
      (rate!! - received).coerceAtLeast(0.0)
    }

    _formState.value = state.copy(isSaving = true)

    viewModelScope.launch {
      val deadline = state.targetDeadlineMillis
      val order = DeliveryOrder(
        customerId = state.customerId,
        customerName = state.customerName.trim(),
        phoneNumber = state.phoneNumber.trim(),
        cylinderNumber = cylNum,
        companyName = state.companyName.trim(),
        mapsLink = state.mapsLink.trim(),
        realRate = rate!!,
        amountReceived = received,
        pendingUdhaar = finalUdhaar,
        isManualUdhaar = state.isManualUdhaarActive,
        deadlineTimestamp = deadline,
        reminderLabel = state.reminderChoice.label,
        isPaid = finalUdhaar <= 0.01,
        confirmedPaidAt = if (finalUdhaar <= 0.01) System.currentTimeMillis() else null,
        createdAt = System.currentTimeMillis()
      )

      val orderId = repository.insert(order)

      // Schedule local notification if there is pending Udhaar
      if (finalUdhaar > 0.01) {
        NotificationHelper.scheduleReminder(
          context = context,
          recordId = orderId,
          customerName = order.customerName,
          amount = finalUdhaar,
          triggerAtMillis = deadline
        )
      }

      _formState.value = OrderFormState()
      _userMessage.value = "Delivery order saved! Udhaar tracked independently."
      onSuccess()
    }
  }

  // When customer pays their Udhaar, tap "Confirm Payment":
  // pending balance clears and record moves to History for 7 days
  fun confirmPayment(context: Context, order: DeliveryOrder) {
    viewModelScope.launch {
      val updated = order.copy(
        amountReceived = order.realRate,
        pendingUdhaar = 0.0,
        isPaid = true,
        confirmedPaidAt = System.currentTimeMillis()
      )
      repository.update(updated)

      // Cancel scheduled reminder
      NotificationHelper.cancelReminder(context, order.id)

      _userMessage.value = "Payment confirmed for ${order.customerName}! Cleared & moved to 7-Day History."
    }
  }

  fun deleteOrder(context: Context, order: DeliveryOrder) {
    viewModelScope.launch {
      repository.delete(order)
      NotificationHelper.cancelReminder(context, order.id)
      _userMessage.value = "Order deleted"
    }
  }

  private fun cleanupExpiredHistory() {
    viewModelScope.launch {
      val cutoff = System.currentTimeMillis() - SEVEN_DAYS_MILLIS
      repository.deleteExpiredHistory(cutoff)
    }
  }
}
