package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AsrDao {
    // Vendor Catalog queries
    @Query("SELECT * FROM vendor_items ORDER BY id DESC")
    fun getAllVendorItems(): Flow<List<VendorItem>>

    @Query("SELECT * FROM vendor_items WHERE category = :category ORDER BY id DESC")
    fun getVendorItemsByCategory(category: String): Flow<List<VendorItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendorItem(item: VendorItem)

    @Update
    suspend fun updateVendorItem(item: VendorItem)

    @Delete
    suspend fun deleteVendorItem(item: VendorItem)

    // Delivery Order queries
    @Query("SELECT * FROM delivery_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<DeliveryOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: DeliveryOrder): Long

    @Update
    suspend fun updateOrder(order: DeliveryOrder)

    @Query("DELETE FROM delivery_orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Int)

    // Utility & Emergency Transactions queries
    @Query("SELECT * FROM utility_transactions ORDER BY timestamp DESC")
    fun getAllUtilityTransactions(): Flow<List<UtilityTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUtilityTransaction(transaction: UtilityTransaction): Long

    // Rider profile queries
    @Query("SELECT * FROM rider_profile ORDER BY id DESC")
    fun getAllRiders(): Flow<List<RiderProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRider(profile: RiderProfile)

    @Update
    suspend fun updateRider(profile: RiderProfile)

    // Customer Subscription queries
    @Query("SELECT * FROM customer_subscriptions ORDER BY timestamp DESC")
    fun getAllSubscriptions(): Flow<List<CustomerSubscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: CustomerSubscription)

    @Update
    suspend fun updateSubscription(sub: CustomerSubscription)

    @Query("DELETE FROM customer_subscriptions WHERE id = :subId")
    suspend fun deleteSubscriptionById(subId: Int)

    @Query("UPDATE delivery_orders SET riderLatitude = :lat, riderLongitude = :lng, riderLocationStatus = :status WHERE id = :orderId")
    suspend fun updateOrderLocation(orderId: Int, lat: Double, lng: Double, status: String)
}
