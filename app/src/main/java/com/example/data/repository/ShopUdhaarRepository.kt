package com.example.data.repository

import com.example.data.local.ShopUdhaarDao
import com.example.data.model.ShopUdhaarRecord
import kotlinx.coroutines.flow.Flow

class ShopUdhaarRepository(private val dao: ShopUdhaarDao) {

  val activeRecords: Flow<List<ShopUdhaarRecord>> = dao.getActiveRecords()

  fun getHistoryRecords(minPaidTimestamp: Long): Flow<List<ShopUdhaarRecord>> =
    dao.getHistoryRecords(minPaidTimestamp)

  suspend fun deleteExpiredHistory(cutoffTimestamp: Long): Int =
    dao.deleteExpiredHistory(cutoffTimestamp)

  suspend fun insert(record: ShopUdhaarRecord): Long = dao.insertRecord(record)

  suspend fun update(record: ShopUdhaarRecord) = dao.updateRecord(record)

  suspend fun delete(record: ShopUdhaarRecord) = dao.deleteRecord(record)

  suspend fun deleteById(id: Long) = dao.deleteById(id)

  fun getById(id: Long): Flow<ShopUdhaarRecord?> = dao.getById(id)
}
