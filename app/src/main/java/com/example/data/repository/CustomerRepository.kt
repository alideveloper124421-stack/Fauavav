package com.example.data.repository

import com.example.data.local.CustomerDao
import com.example.data.model.CustomerProfile
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val dao: CustomerDao) {

  val allCustomers: Flow<List<CustomerProfile>> = dao.getAllCustomers()

  fun search(query: String): Flow<List<CustomerProfile>> = dao.searchCustomers(query)

  fun getById(id: Long): Flow<CustomerProfile?> = dao.getCustomerById(id)

  suspend fun insert(customer: CustomerProfile): Long = dao.insertCustomer(customer)

  suspend fun update(customer: CustomerProfile) = dao.updateCustomer(customer)

  suspend fun delete(customer: CustomerProfile) = dao.deleteCustomer(customer)

  suspend fun deleteById(id: Long) = dao.deleteById(id)
}
