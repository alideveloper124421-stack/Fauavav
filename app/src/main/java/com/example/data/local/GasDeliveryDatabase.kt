package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CustomerProfile
import com.example.data.model.CustomerRating
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryRecord
import com.example.data.model.ShopUdhaarRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    CustomerProfile::class,
    DeliveryOrder::class,
    DeliveryRecord::class,
    ShopUdhaarRecord::class
  ],
  version = 3,
  exportSchema = false
)
abstract class GasDeliveryDatabase : RoomDatabase() {
  abstract fun customerDao(): CustomerDao
  abstract fun deliveryOrderDao(): DeliveryOrderDao
  abstract fun deliveryRecordDao(): DeliveryRecordDao
  abstract fun shopUdhaarDao(): ShopUdhaarDao

  companion object {
    @Volatile
    private var INSTANCE: GasDeliveryDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): GasDeliveryDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          GasDeliveryDatabase::class.java,
          "gas_delivery_khata_db"
        )
        .fallbackToDestructiveMigration()
        .addCallback(DatabaseCallback(scope))
        .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database.customerDao(), database.deliveryOrderDao())
          }
        }
      }

      suspend fun populateInitialData(
        customerDao: CustomerDao,
        orderDao: DeliveryOrderDao
      ) {
        // 1. Initial permanent Customer Directory profiles (Zero Udhaar!)
        val cust1Id = customerDao.insertCustomer(
          CustomerProfile(
            customerName = "Ali Raza Khan",
            phoneNumber = "+923001234567",
            cylinderNumber = "20",
            companyName = "PSO Gas",
            rating = CustomerRating.EXCELLENT.displayName,
            mapsLink = "https://maps.google.com/?q=31.520400,74.358700"
          )
        )

        val cust2Id = customerDao.insertCustomer(
          CustomerProfile(
            customerName = "Haji Muhammad Aslam",
            phoneNumber = "+923129876543",
            cylinderNumber = "21",
            companyName = "Pak Gas",
            rating = CustomerRating.VERY_GOOD.displayName,
            mapsLink = "https://maps.google.com/?q=31.549700,74.343600"
          )
        )

        // 2. Initial Delivery Orders & Temporary Udhaar
        orderDao.insertOrder(
          DeliveryOrder(
            customerId = cust1Id,
            customerName = "Ali Raza Khan",
            phoneNumber = "+923001234567",
            cylinderNumber = "20",
            companyName = "PSO Gas",
            mapsLink = "https://maps.google.com/?q=31.520400,74.358700",
            realRate = 5000.0,
            amountReceived = 2500.0,
            pendingUdhaar = 2500.0,
            isManualUdhaar = false,
            deadlineTimestamp = System.currentTimeMillis() + 86400000L,
            reminderLabel = "1 Day",
            isPaid = false
          )
        )

        orderDao.insertOrder(
          DeliveryOrder(
            customerId = cust2Id,
            customerName = "Haji Muhammad Aslam",
            phoneNumber = "+923129876543",
            cylinderNumber = "21",
            companyName = "Pak Gas",
            mapsLink = "https://maps.google.com/?q=31.549700,74.343600",
            realRate = 4800.0,
            amountReceived = 4800.0,
            pendingUdhaar = 0.0,
            isManualUdhaar = false,
            deadlineTimestamp = System.currentTimeMillis(),
            reminderLabel = "1 Day",
            isPaid = true,
            confirmedPaidAt = System.currentTimeMillis() - 86400000L * 2
          )
        )
      }
    }
  }
}
