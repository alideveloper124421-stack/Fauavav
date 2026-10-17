package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GasDeliveryDatabase
import com.example.data.model.ShopUdhaarRecord
import com.example.data.repository.ShopUdhaarRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ShopUdhaarTab {
  PENDING,
  SEVEN_DAY_HISTORY
}

enum class ReminderChoice(val label: String, val durationMillis: Long) {
  ONE_DAY("1 Day", 24L * 60L * 60L * 1000L),
  TWO_DAYS("2 Days", 2L * 24L * 60L * 60L * 1000L),
  FIVE_DAYS("5 Days", 5L * 24L * 60L * 60L * 1000L),
  CUSTOM_DATE("Custom Date", 0L)
}

data class ShopFormState(
  val customerName: String = "",
  val phoneNumber: String = "",
  val udhaarAmount: String = "",
  val reminderChoice: ReminderChoice = ReminderChoice.ONE_DAY,
  val customDateTimestamp: Long = System.currentTimeMillis() + 86400000L * 3,
  val nameError: String? = null,
  val phoneError: String? = null,
  val amountError: String? = null,
  val isSaving: Boolean = false
) {
  val targetDeadlineMillis: Long
    get() {
      return if (reminderChoice == ReminderChoice.CUSTOM_DATE) {
        customDateTimestamp
      } else {
        System.currentTimeMillis() + reminderChoice.durationMillis
      }
    }
}

data class ShopUdhaarStats(
  val totalPendingAmount: Double = 0.0,
  val pendingCount: Int = 0,
  val totalRecovered7Days: Double = 0.0,
  val recoveredCount7Days: Int = 0
)

class ShopUdhaarViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: ShopUdhaarRepository
  private val SEVEN_DAYS_MILLIS = 7L * 24L * 60L * 60L * 1000L

  private val _currentTab = MutableStateFlow(ShopUdhaarTab.PENDING)
  val currentTab: StateFlow<ShopUdhaarTab> = _currentTab.asStateFlow()

  private val _formState = MutableStateFlow(ShopFormState())
  val formState: StateFlow<ShopFormState> = _formState.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  init {
    val db = GasDeliveryDatabase.getDatabase(application, viewModelScope)
    repository = ShopUdhaarRepository(db.shopUdhaarDao())

    // Auto-clean records that were paid more than 7 days ago
    cleanupExpiredHistory()
  }

  val activeRecords: StateFlow<List<ShopUdhaarRecord>> = repository.activeRecords
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val historyRecords: StateFlow<List<ShopUdhaarRecord>> = repository.getHistoryRecords(
    System.currentTimeMillis() - SEVEN_DAYS_MILLIS
  ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredActiveRecords: StateFlow<List<ShopUdhaarRecord>> = combine(
    activeRecords,
    _searchQuery
  ) { records, query ->
    if (query.isBlank()) records
    else records.filter {
      it.customerName.contains(query, ignoreCase = true) ||
        it.phoneNumber.contains(query, ignoreCase = true)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredHistoryRecords: StateFlow<List<ShopUdhaarRecord>> = combine(
    historyRecords,
    _searchQuery
  ) { records, query ->
    if (query.isBlank()) records
    else records.filter {
      it.customerName.contains(query, ignoreCase = true) ||
        it.phoneNumber.contains(query, ignoreCase = true)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val stats: StateFlow<ShopUdhaarStats> = combine(activeRecords, historyRecords) { active, history ->
    ShopUdhaarStats(
      totalPendingAmount = active.sumOf { it.udhaarAmount },
      pendingCount = active.size,
      totalRecovered7Days = history.sumOf { it.udhaarAmount },
      recoveredCount7Days = history.size
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopUdhaarStats())

  fun setTab(tab: ShopUdhaarTab) {
    _currentTab.value = tab
  }

  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  // Form handling
  fun updateCustomerName(name: String) {
    _formState.value = _formState.value.copy(customerName = name, nameError = null)
  }

  fun updatePhoneNumber(phone: String) {
    _formState.value = _formState.value.copy(phoneNumber = phone, phoneError = null)
  }

  fun updateUdhaarAmount(amount: String) {
    val clean = amount.filter { it.isDigit() || it == '.' }
    _formState.value = _formState.value.copy(udhaarAmount = clean, amountError = null)
  }

  fun updateReminderChoice(choice: ReminderChoice) {
    _formState.value = _formState.value.copy(reminderChoice = choice)
  }

  fun updateCustomDateTimestamp(timestamp: Long) {
    _formState.value = _formState.value.copy(customDateTimestamp = timestamp)
  }

  fun resetForm() {
    _formState.value = ShopFormState()
  }

  fun saveRecord(context: Context, onSuccess: () -> Unit) {
    val state = _formState.value
    var hasError = false
    var nameErr: String? = null
    var phoneErr: String? = null
    var amountErr: String? = null

    if (state.customerName.isBlank()) {
      nameErr = "Customer name is required"
      hasError = true
    }
    if (state.phoneNumber.isBlank()) {
      phoneErr = "Phone number is required"
      hasError = true
    }
    val amount = state.udhaarAmount.toDoubleOrNull()
    if (amount == null || amount <= 0) {
      amountErr = "Enter a valid Udhaar amount"
      hasError = true
    }

    if (hasError) {
      _formState.value = state.copy(
        nameError = nameErr,
        phoneError = phoneErr,
        amountError = amountErr
      )
      return
    }

    _formState.value = state.copy(isSaving = true)

    viewModelScope.launch {
      val deadline = state.targetDeadlineMillis
      val newRecord = ShopUdhaarRecord(
        customerName = state.customerName.trim(),
        phoneNumber = state.phoneNumber.trim(),
        udhaarAmount = amount!!,
        createdAt = System.currentTimeMillis(),
        deadlineTimestamp = deadline,
        reminderLabel = state.reminderChoice.label,
        isPaid = false
      )

      val recordId = repository.insert(newRecord)

      // Schedule exact local notification
      NotificationHelper.scheduleReminder(
        context = context,
        recordId = recordId,
        customerName = newRecord.customerName,
        amount = newRecord.udhaarAmount,
        triggerAtMillis = deadline
      )

      _formState.value = ShopFormState()
      _userMessage.value = "Shop Udhaar saved! Reminder notification scheduled."
      onSuccess()
    }
  }

  fun confirmPayment(context: Context, record: ShopUdhaarRecord) {
    viewModelScope.launch {
      val updated = record.copy(
        isPaid = true,
        confirmedPaidAt = System.currentTimeMillis()
      )
      repository.update(updated)

      // Cancel any pending notification for this record
      NotificationHelper.cancelReminder(context, record.id)

      _userMessage.value = "Payment confirmed! Record moved to 7-Day History."
    }
  }

  fun deleteRecord(context: Context, record: ShopUdhaarRecord) {
    viewModelScope.launch {
      repository.delete(record)
      NotificationHelper.cancelReminder(context, record.id)
      _userMessage.value = "Record deleted"
    }
  }

  private fun cleanupExpiredHistory() {
    viewModelScope.launch {
      val cutoff = System.currentTimeMillis() - SEVEN_DAYS_MILLIS
      repository.deleteExpiredHistory(cutoff)
    }
  }
}
