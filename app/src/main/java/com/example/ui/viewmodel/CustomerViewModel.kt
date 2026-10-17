package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GasDeliveryDatabase
import com.example.data.model.CustomerProfile
import com.example.data.model.CustomerRating
import com.example.data.repository.CustomerRepository
import com.example.ui.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class CustomerFormState(
  val editingCustomerId: Long? = null,
  val customerName: String = "",
  val phoneNumber: String = "",
  val cylinderNumber: String = "20",
  val customCylinderNumber: String = "",
  val companyName: String = "",
  val rating: CustomerRating = CustomerRating.GOOD,
  val mapsLink: String = "",
  val isFetchingLocation: Boolean = false,
  val detectedCoordinates: String? = null,
  val locationError: String? = null,
  val nameError: String? = null,
  val phoneError: String? = null,
  val companyError: String? = null,
  val isSaving: Boolean = false
) {
  val effectiveCylinderNumber: String
    get() = if (cylinderNumber == "Custom") customCylinderNumber.trim() else cylinderNumber
}

class CustomerViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: CustomerRepository
  val availableCylinderNumbers = listOf("20", "21", "22", "23", "24", "25", "45", "Custom")
  val popularCompanies = listOf("PSO Gas", "Shell Gas", "Bharat Gas", "Indane", "HP Gas", "Pak Gas", "Total Gas")

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _formState = MutableStateFlow(CustomerFormState())
  val formState: StateFlow<CustomerFormState> = _formState.asStateFlow()

  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  init {
    val db = GasDeliveryDatabase.getDatabase(application, viewModelScope)
    repository = CustomerRepository(db.customerDao())
  }

  val allCustomers: StateFlow<List<CustomerProfile>> = repository.allCustomers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredCustomers: StateFlow<List<CustomerProfile>> = combine(
    allCustomers,
    _searchQuery
  ) { customers, query ->
    if (query.isBlank()) customers
    else customers.filter {
      it.customerName.contains(query, ignoreCase = true) ||
        it.phoneNumber.contains(query, ignoreCase = true) ||
        it.companyName.contains(query, ignoreCase = true) ||
        it.cylinderNumber.contains(query, ignoreCase = true)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
  }

  fun clearUserMessage() {
    _userMessage.value = null
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

  fun updateRating(rating: CustomerRating) {
    _formState.value = _formState.value.copy(rating = rating)
  }

  fun updateMapsLink(link: String) {
    _formState.value = _formState.value.copy(mapsLink = link.trim(), locationError = null)
  }

  fun fetchCurrentLocation(context: Context) {
    if (!LocationHelper.hasLocationPermission(context)) {
      _formState.value = _formState.value.copy(
        locationError = "Location permission is required to detect GPS coordinates."
      )
      return
    }

    if (!LocationHelper.isLocationEnabled(context)) {
      _formState.value = _formState.value.copy(
        locationError = "Please turn on device GPS/Location in system settings."
      )
      return
    }

    _formState.value = _formState.value.copy(isFetchingLocation = true, locationError = null)

    viewModelScope.launch {
      val result = LocationHelper.getCurrentLocation(context)
      if (result != null) {
        val coordText = String.format(
          Locale.US,
          "Lat: %.5f, Lng: %.5f (±%.0fm)",
          result.latitude,
          result.longitude,
          result.accuracyMeters
        )
        _formState.value = _formState.value.copy(
          mapsLink = result.mapsUrl,
          detectedCoordinates = coordText,
          isFetchingLocation = false,
          locationError = null
        )
        _userMessage.value = "GPS Location captured successfully!"
      } else {
        _formState.value = _formState.value.copy(
          isFetchingLocation = false,
          locationError = "Could not acquire GPS fix. Please ensure you are outdoors or enter link manually."
        )
      }
    }
  }

  fun populateForEdit(customer: CustomerProfile) {
    _formState.value = CustomerFormState(
      editingCustomerId = customer.id,
      customerName = customer.customerName,
      phoneNumber = customer.phoneNumber,
      cylinderNumber = if (customer.cylinderNumber in listOf("20", "21", "22", "23", "24", "25", "45")) customer.cylinderNumber else "Custom",
      customCylinderNumber = if (customer.cylinderNumber in listOf("20", "21", "22", "23", "24", "25", "45")) "" else customer.cylinderNumber,
      companyName = customer.companyName,
      rating = CustomerRating.fromDisplayName(customer.rating),
      mapsLink = customer.mapsLink
    )
  }

  fun resetForm() {
    _formState.value = CustomerFormState()
  }

  fun saveCustomer(onSuccess: () -> Unit) {
    val state = _formState.value
    var hasError = false
    var nameErr: String? = null
    var phoneErr: String? = null
    var compErr: String? = null

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

    val cylNum = state.effectiveCylinderNumber
    if (cylNum.isBlank()) {
      compErr = "Cylinder number cannot be empty"
      hasError = true
    }

    if (hasError) {
      _formState.value = state.copy(
        nameError = nameErr,
        phoneError = phoneErr,
        companyError = compErr
      )
      return
    }

    _formState.value = state.copy(isSaving = true)

    viewModelScope.launch {
      val profile = CustomerProfile(
        id = state.editingCustomerId ?: 0L,
        customerName = state.customerName.trim(),
        phoneNumber = state.phoneNumber.trim(),
        cylinderNumber = cylNum,
        companyName = state.companyName.trim(),
        rating = state.rating.displayName,
        mapsLink = state.mapsLink.trim()
      )

      if (state.editingCustomerId != null && state.editingCustomerId > 0) {
        repository.update(profile)
        _userMessage.value = "Customer profile updated!"
      } else {
        repository.insert(profile)
        _userMessage.value = "Customer added to Directory!"
      }

      _formState.value = CustomerFormState()
      onSuccess()
    }
  }

  fun deleteCustomer(customer: CustomerProfile) {
    viewModelScope.launch {
      repository.delete(customer)
      _userMessage.value = "Customer removed from Directory"
    }
  }
}
