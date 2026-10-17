package com.example.data.repository

import com.example.data.local.DeliveryRecordDao
import com.example.data.model.DeliveryRecord
import kotlinx.coroutines.flow.Flow

class DeliveryRepository(private val dao: DeliveryRecordDao) {

  val allRecords: Flow<List<DeliveryRecord>> = dao.getAllRecords()

  fun search(query: String): Flow<List<DeliveryRecord>> = dao.searchRecords(query)

  fun getById(id: Long): Flow<DeliveryRecord?> = dao.getRecordById(id)

  suspend fun insert(record: DeliveryRecord): Long = dao.insertRecord(record)

  suspend fun update(record: DeliveryRecord) = dao.updateRecord(record)

  suspend fun delete(record: DeliveryRecord) = dao.deleteRecord(record)

  suspend fun deleteById(id: Long) = dao.deleteById(id)
}
