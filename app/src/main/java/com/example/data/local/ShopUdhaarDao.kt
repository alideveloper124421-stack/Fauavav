package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ShopUdhaarRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopUdhaarDao {

  @Query("SELECT * FROM shop_udhaar_records WHERE isPaid = 0 ORDER BY deadlineTimestamp ASC")
  fun getActiveRecords(): Flow<List<ShopUdhaarRecord>>

  @Query("SELECT * FROM shop_udhaar_records WHERE isPaid = 1 AND confirmedPaidAt >= :minPaidTimestamp ORDER BY confirmedPaidAt DESC")
  fun getHistoryRecords(minPaidTimestamp: Long): Flow<List<ShopUdhaarRecord>>

  @Query("DELETE FROM shop_udhaar_records WHERE isPaid = 1 AND confirmedPaidAt < :cutoffTimestamp")
  suspend fun deleteExpiredHistory(cutoffTimestamp: Long): Int

  @Query("SELECT * FROM shop_udhaar_records WHERE id = :id LIMIT 1")
  fun getById(id: Long): Flow<ShopUdhaarRecord?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecord(record: ShopUdhaarRecord): Long

  @Update
  suspend fun updateRecord(record: ShopUdhaarRecord)

  @Delete
  suspend fun deleteRecord(record: ShopUdhaarRecord)

  @Query("DELETE FROM shop_udhaar_records WHERE id = :id")
  suspend fun deleteById(id: Long)
}
