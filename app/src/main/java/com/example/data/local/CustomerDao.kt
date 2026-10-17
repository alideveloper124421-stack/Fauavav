package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

  @Query("SELECT * FROM customer_directory ORDER BY customerName ASC")
  fun getAllCustomers(): Flow<List<CustomerProfile>>

  @Query(
    "SELECT * FROM customer_directory WHERE customerName LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR companyName LIKE '%' || :query || '%' ORDER BY customerName ASC"
  )
  fun searchCustomers(query: String): Flow<List<CustomerProfile>>

  @Query("SELECT * FROM customer_directory WHERE id = :id LIMIT 1")
  fun getCustomerById(id: Long): Flow<CustomerProfile?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomer(customer: CustomerProfile): Long

  @Update
  suspend fun updateCustomer(customer: CustomerProfile)

  @Delete
  suspend fun deleteCustomer(customer: CustomerProfile)

  @Query("DELETE FROM customer_directory WHERE id = :id")
  suspend fun deleteById(id: Long)
}
