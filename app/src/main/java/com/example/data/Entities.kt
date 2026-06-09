package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendor_items")
data class VendorItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vendorName: String,
    val category: String, // Restaurants, Hotels, Groceries, Vegetables, Fruits, Flowers, Event Management, Wedding Planner, Photo Studio, Fashion Design, Emergency, Recharge, Mobile Accessories
    val title: String,
    val price: Double,
    val quantityString: String, // e.g. "1 kg", "Per night", "Service fee"
    val inventory: Int, // stock count
    val description: String,
    val isOnline: Boolean = true // True = online delivery, False = offline location
)

@Entity(tableName = "delivery_orders")
data class DeliveryOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val vendorName: String,
    val category: String,
    val itemsSummary: String, // e.g. "Veg Thali x2, Apple x1"
    val totalAmount: Double,
    val paymentMethod: String, // "Cash on Delivery", "Online Payment"
    val paymentStatus: String, // "Pending", "Paid"
    val deliveryAddress: String,
    val status: String, // "Pending", "Picked Up", "Delivered"
    val riderId: Int = 0,
    val riderName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val riderLatitude: Double = 0.0,
    val riderLongitude: Double = 0.0,
    val riderLocationStatus: String = "Resting"
)

@Entity(tableName = "customer_subscriptions")
data class CustomerSubscription(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val vendorName: String,
    val packageName: String,
    val category: String,
    val price: Double,
    val frequency: String, // e.g., "Daily", "Weekly", "Monthly"
    val status: String = "Active", // "Active", "Paused", "Cancelled"
    val nextDeliveryDate: String = "Tomorrow",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "utility_transactions")
data class UtilityTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "UPI", "Recharge", "Taxi", "Electricity", "Cooking Gas"
    val details: String,
    val amount: Double,
    val status: String, // "Success", "Processing", "Failed"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "rider_profile")
data class RiderProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val status: String, // "Available", "Delivering", "Offline"
    val phone: String,
    val rating: Float = 4.8f,
    val totalDeliveries: Int = 0
)
