package com.example.data.repository

import com.example.data.local.DeliveryOrderDao
import com.example.data.model.DeliveryOrder
import kotlinx.coroutines.flow.Flow

class DeliveryOrderRepository(private val dao: DeliveryOrderDao) {

  val activeOrders: Flow<List<DeliveryOrder>> = dao.getActiveOrders()

  fun getHistoryOrders(minPaidTimestamp: Long): Flow<List<DeliveryOrder>> =
    dao.getHistoryOrders(minPaidTimestamp)

  suspend fun deleteExpiredHistory(cutoffTimestamp: Long): Int =
    dao.deleteExpiredHistory(cutoffTimestamp)

  fun getById(id: Long): Flow<DeliveryOrder?> = dao.getOrderById(id)

  suspend fun insert(order: DeliveryOrder): Long = dao.insertOrder(order)

  suspend fun update(order: DeliveryOrder) = dao.updateOrder(order)

  suspend fun delete(order: DeliveryOrder) = dao.deleteOrder(order)

  suspend fun deleteById(id: Long) = dao.deleteById(id)
}
