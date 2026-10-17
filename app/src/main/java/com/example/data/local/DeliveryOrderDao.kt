package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DeliveryOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryOrderDao {

  @Query("SELECT * FROM delivery_orders WHERE isPaid = 0 ORDER BY createdAt DESC")
  fun getActiveOrders(): Flow<List<DeliveryOrder>>

  @Query("SELECT * FROM delivery_orders WHERE isPaid = 1 AND confirmedPaidAt >= :minPaidTimestamp ORDER BY confirmedPaidAt DESC")
  fun getHistoryOrders(minPaidTimestamp: Long): Flow<List<DeliveryOrder>>

  @Query("DELETE FROM delivery_orders WHERE isPaid = 1 AND confirmedPaidAt < :cutoffTimestamp")
  suspend fun deleteExpiredHistory(cutoffTimestamp: Long): Int

  @Query("SELECT * FROM delivery_orders WHERE id = :id LIMIT 1")
  fun getOrderById(id: Long): Flow<DeliveryOrder?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: DeliveryOrder): Long

  @Update
  suspend fun updateOrder(order: DeliveryOrder)

  @Delete
  suspend fun deleteOrder(order: DeliveryOrder)

  @Query("DELETE FROM delivery_orders WHERE id = :id")
  suspend fun deleteById(id: Long)
}
