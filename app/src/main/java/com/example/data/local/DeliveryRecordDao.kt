package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DeliveryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryRecordDao {
  @Query("SELECT * FROM delivery_records ORDER BY createdAt DESC")
  fun getAllRecords(): Flow<List<DeliveryRecord>>

  @Query("SELECT * FROM delivery_records WHERE id = :id LIMIT 1")
  fun getRecordById(id: Long): Flow<DeliveryRecord?>

  @Query(
    "SELECT * FROM delivery_records WHERE customerName LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR companyName LIKE '%' || :query || '%' ORDER BY createdAt DESC"
  )
  fun searchRecords(query: String): Flow<List<DeliveryRecord>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecord(record: DeliveryRecord): Long

  @Update
  suspend fun updateRecord(record: DeliveryRecord)

  @Delete
  suspend fun deleteRecord(record: DeliveryRecord)

  @Query("DELETE FROM delivery_records WHERE id = :id")
  suspend fun deleteById(id: Long)
}
