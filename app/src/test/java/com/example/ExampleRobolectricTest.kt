package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CustomerProfile
import com.example.data.model.CustomerRating
import com.example.ui.viewmodel.OrderFormState
import com.example.ui.viewmodel.ReminderChoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Gas Delivery", appName)
  }

  @Test
  fun `auto calculate balance test`() {
    val state = OrderFormState(
      realRate = "5000",
      amountReceived = "2500",
      isManualUdhaarActive = false
    )
    assertEquals(2500.0, state.calculatedBalance, 0.001)
  }

  @Test
  fun `manual udhaar entry overrides auto calculate`() {
    val state = OrderFormState(
      realRate = "5000",
      amountReceived = "2500",
      manualUdhaar = "3000",
      isManualUdhaarActive = true
    )
    assertEquals(3000.0, state.calculatedBalance, 0.001)
  }

  @Test
  fun `customer rating choices mapping`() {
    val excellent = CustomerRating.fromDisplayName("Excellent")
    assertEquals(CustomerRating.EXCELLENT, excellent)
    val bad = CustomerRating.fromDisplayName("Very Bad")
    assertEquals(CustomerRating.VERY_BAD, bad)
  }

  @Test
  fun `google maps url formatting from coordinates`() {
    val url = com.example.ui.util.LocationHelper.buildGoogleMapsUrl(31.520400, 74.358700)
    assertEquals("https://maps.google.com/?q=31.520400,74.358700", url)
  }

  @Test
  fun `shop udhaar reminder choice duration`() {
    assertEquals(86400000L, ReminderChoice.ONE_DAY.durationMillis)
    assertEquals(172800000L, ReminderChoice.TWO_DAYS.durationMillis)
    assertEquals(432000000L, ReminderChoice.FIVE_DAYS.durationMillis)
  }

  @Test
  fun `seven day history cutoff calculation`() {
    val now = 1000000000000L
    val sevenDaysMillis = 7L * 24L * 60L * 60L * 1000L
    val cutoff = now - sevenDaysMillis

    val paid3DaysAgo = now - (3L * 24L * 60L * 60L * 1000L)
    val paid8DaysAgo = now - (8L * 24L * 60L * 60L * 1000L)

    assertTrue("Paid 3 days ago should be kept in 7-day history", paid3DaysAgo >= cutoff)
    assertTrue("Paid 8 days ago should be deleted past cutoff", paid8DaysAgo < cutoff)
  }

  @Test
  fun `customer profile to order prefill test`() {
    val profile = CustomerProfile(
      id = 42L,
      customerName = "Zafar Iqbal",
      phoneNumber = "+923001122334",
      cylinderNumber = "22",
      companyName = "PSO Gas",
      mapsLink = "https://maps.google.com/?q=31.5,74.3"
    )

    val form = OrderFormState(
      customerId = profile.id,
      customerName = profile.customerName,
      phoneNumber = profile.phoneNumber,
      cylinderNumber = profile.cylinderNumber,
      companyName = profile.companyName,
      mapsLink = profile.mapsLink
    )

    assertEquals("Zafar Iqbal", form.customerName)
    assertEquals("22", form.cylinderNumber)
    assertEquals("PSO Gas", form.companyName)
    assertEquals("https://maps.google.com/?q=31.5,74.3", form.mapsLink)
  }
}
