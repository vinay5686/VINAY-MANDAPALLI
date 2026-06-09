package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object FirestoreManager {
    private const val TAG = "FirestoreManager"

    // Safe retrieval of FirebaseFirestore instance
    val firestoreInstance: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase SDK not configured: ${e.message}")
            null
        }
    }

    // Helper to check configuration
    fun isConfigured(context: Context): Boolean {
        return try {
            val apps = FirebaseApp.getApps(context)
            apps.isNotEmpty() && firestoreInstance != null
        } catch (e: Exception) {
            false
        }
    }

    // Safe wrap of firestore listener tasks to suspend functions
    private suspend fun <T> awaitTask(action: (onSuccess: (T) -> Unit, onFailure: (Exception) -> Unit) -> Unit): T {
        return suspendCancellableCoroutine { continuation ->
            try {
                action(
                    { result -> if (continuation.isActive) continuation.resume(result) },
                    { exception -> if (continuation.isActive) continuation.resumeWith(Result.failure(exception)) }
                )
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resumeWith(Result.failure(e))
            }
        }
    }

    // Sync categories
    suspend fun syncServiceCategories(categories: List<String>): Result<Unit> {
        val db = firestoreInstance ?: return Result.failure(Exception("Firestore not initialized"))
        return try {
            for (category in categories) {
                val data = mapOf(
                    "name" to category,
                    "description" to "Service listings for $category",
                    "id" to category.lowercase().replace(" ", "_"),
                    "updatedAt" to System.currentTimeMillis()
                )
                awaitTask<Void?> { success, failure ->
                    db.collection("service_categories")
                        .document(category)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener { success(null) }
                        .addOnFailureListener { failure(it) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing categories: ${e.message}")
            Result.failure(e)
        }
    }

    // Sync user profiles
    suspend fun syncUserProfile(uid: String, name: String, role: String, phone: String, email: String, address: String): Result<Unit> {
        val db = firestoreInstance ?: return Result.failure(Exception("Firestore not initialized"))
        return try {
            val data = mapOf(
                "uid" to uid,
                "name" to name,
                "role" to role,
                "phone" to phone,
                "email" to email,
                "address" to address,
                "updatedAt" to System.currentTimeMillis()
            )
            awaitTask<Void?> { success, failure ->
                db.collection("user_profiles")
                    .document(uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener { success(null) }
                    .addOnFailureListener { failure(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing profile: ${e.message}")
            Result.failure(e)
        }
    }

    // Sync transaction record
    suspend fun syncTransactionRecord(order: DeliveryOrder): Result<Unit> {
        val db = firestoreInstance ?: return Result.failure(Exception("Firestore not initialized"))
        return try {
            val data = mapOf(
                "orderId" to order.id,
                "customerName" to order.customerName,
                "vendorName" to order.vendorName,
                "category" to order.category,
                "itemsSummary" to order.itemsSummary,
                "totalAmount" to order.totalAmount,
                "paymentMethod" to order.paymentMethod,
                "paymentStatus" to order.paymentStatus,
                "deliveryAddress" to order.deliveryAddress,
                "status" to order.status,
                "riderId" to order.riderId,
                "riderName" to order.riderName,
                "timestamp" to order.timestamp
            )
            awaitTask<Void?> { success, failure ->
                db.collection("transaction_records")
                    .document(order.id.toString())
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener { success(null) }
                    .addOnFailureListener { failure(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing transaction order record: ${e.message}")
            Result.failure(e)
        }
    }
}
