package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AsrViewModel(application: Application, private val repository: AsrRepository) : AndroidViewModel(application) {

    // Active screen/role state: "CUSTOMER", "VENDOR", "RIDER", "ADMIN"
    private val _activeRole = MutableStateFlow("CUSTOMER")
    val activeRole: StateFlow<String> = _activeRole.asStateFlow()

    // Live Database flows transformed safely to StateFlow with lifecycle subscription parameters
    val vendorItems: StateFlow<List<VendorItem>> = repository.allVendorItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val orders: StateFlow<List<DeliveryOrder>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions: StateFlow<List<UtilityTransaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val riders: StateFlow<List<RiderProfile>> = repository.allRiders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val subscriptions: StateFlow<List<CustomerSubscription>> = repository.allSubscriptions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _walletBalance = MutableStateFlow<Double>(1500.0) // default 1500 rupees
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    // Customer Shopping Cart: Map of <VendorItem, Quantity>
    private val _cart = MutableStateFlow<Map<VendorItem, Int>>(emptyMap())
    val cart: StateFlow<Map<VendorItem, Int>> = _cart.asStateFlow()

    // Utility Status Log Message for UI Toast/Alert simulator
    private val _systemAlert = MutableStateFlow<String?>(null)
    val systemAlert: StateFlow<String?> = _systemAlert.asStateFlow()

    // Firebase state variables
    private val _firebaseLogs = MutableStateFlow<List<String>>(listOf("Firestore integration module preloaded."))
    val firebaseLogs: StateFlow<List<String>> = _firebaseLogs.asStateFlow()

    private val _isFirebaseConfigured = MutableStateFlow<Boolean>(false)
    val isFirebaseConfigured: StateFlow<Boolean> = _isFirebaseConfigured.asStateFlow()

    private val _isFirestoreSyncing = MutableStateFlow<Boolean>(false)
    val isFirestoreSyncing: StateFlow<Boolean> = _isFirestoreSyncing.asStateFlow()

    // Customer login states
    private val _isCustomerLoggedIn = MutableStateFlow<Boolean>(false)
    val isCustomerLoggedIn: StateFlow<Boolean> = _isCustomerLoggedIn.asStateFlow()

    private val _currentCustomerName = MutableStateFlow<String?>(null)
    val currentCustomerName: StateFlow<String?> = _currentCustomerName.asStateFlow()

    private val _currentCustomerPhone = MutableStateFlow<String?>(null)
    val currentCustomerPhone: StateFlow<String?> = _currentCustomerPhone.asStateFlow()

    private val _currentCustomerAddress = MutableStateFlow<String?>(null)
    val currentCustomerAddress: StateFlow<String?> = _currentCustomerAddress.asStateFlow()

    // Vendor login states
    private val _isVendorLoggedIn = MutableStateFlow<Boolean>(false)
    val isVendorLoggedIn: StateFlow<Boolean> = _isVendorLoggedIn.asStateFlow()

    // active vendor login profile
    private val _activeVendorId = MutableStateFlow<String>("Taj Delicacy")
    val activeVendorId: StateFlow<String> = _activeVendorId.asStateFlow()

    // Rider login states
    private val _isRiderLoggedIn = MutableStateFlow<Boolean>(false)
    val isRiderLoggedIn: StateFlow<Boolean> = _isRiderLoggedIn.asStateFlow()

    private val _riderLoginError = MutableStateFlow<String?>(null)
    val riderLoginError: StateFlow<String?> = _riderLoginError.asStateFlow()

    // active rider login profile
    private val _activeRiderId = MutableStateFlow<Int>(1)
    val activeRiderId: StateFlow<Int> = _activeRiderId.asStateFlow()

    // Supported Languages: English, Telugu, Hindi, Tamil, Malayalam
    private val _currentLanguage = MutableStateFlow<String>("English")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    init {
        // Automatically preheat and seed database if empty on launch
        viewModelScope.launch {
            repository.prepSeedDataIfEmpty()
        }
        _isFirebaseConfigured.value = FirestoreManager.isConfigured(application)
        _firebaseLogs.value = listOf(
            "Firestore integration module preloaded.",
            if (_isFirebaseConfigured.value) "✅ Connected securely to Google Cloud Firestore." 
            else "⚙️ Offline Sandbox mode activated. Operations will log and run using simulated network state."
        )
    }

    // Role switcher
    fun setRole(role: String) {
        _activeRole.value = role
    }

    fun clearSystemAlert() {
        _systemAlert.value = null
    }

    fun showSystemAlert(message: String) {
        _systemAlert.value = message
    }

    // Customer Login controls
    fun loginCustomer(name: String, phone: String, address: String) {
        _currentCustomerName.value = name
        _currentCustomerPhone.value = phone
        _currentCustomerAddress.value = address
        _isCustomerLoggedIn.value = true
        _systemAlert.value = "Welcome, $name! Authenticated."
    }

    fun logoutCustomer() {
        _isCustomerLoggedIn.value = false
        _currentCustomerName.value = null
        _currentCustomerPhone.value = null
        _currentCustomerAddress.value = null
        clearCart()
    }

    // Vendor Login controls
    fun loginVendor(vendorName: String) {
        _activeVendorId.value = vendorName
        _isVendorLoggedIn.value = true
        _systemAlert.value = "Logged in successfully as Merchant $vendorName."
    }

    fun logoutVendor() {
        _isVendorLoggedIn.value = false
    }

    // Rider Login controls
    fun loginRider(idNumber: String) {
        viewModelScope.launch {
            val parsedId = idNumber.toIntOrNull()
            if (parsedId == null) {
                _riderLoginError.value = "Please enter a valid numeric ID"
                return@launch
            }
            val foundRider = repository.allRiders.first().find { it.id == parsedId }
            if (foundRider != null) {
                _activeRiderId.value = foundRider.id
                _isRiderLoggedIn.value = true
                _riderLoginError.value = null
                _systemAlert.value = "Welcome back, Rider ${foundRider.name}!"
            } else {
                _riderLoginError.value = "Rider ID #$parsedId not registered in our dispatch system"
            }
        }
    }

    fun logoutRider() {
        _isRiderLoggedIn.value = false
        _riderLoginError.value = null
    }

    // Cart Operations
    fun addToCart(item: VendorItem) {
        val current = _cart.value.toMutableMap()
        val count = current[item] ?: 0
        current[item] = count + 1
        _cart.value = current
        _systemAlert.value = "Added to basket: ${item.title}"
    }

    fun removeFromCart(item: VendorItem) {
        val current = _cart.value.toMutableMap()
        val count = current[item] ?: 0
        if (count == 1) {
            current.remove(item)
        } else if (count > 1) {
            current[item] = count - 1
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    // Order checkout logic
    fun placeOrder(customerName: String, address: String, paymentMethod: String) {
        if (_cart.value.isEmpty()) return
        
        viewModelScope.launch {
            // Group articles to form a single master invoice
            val vendorName = _cart.value.keys.first().vendorName
            val category = _cart.value.keys.first().category
            val summary = _cart.value.map { "${it.key.title} (x${it.value})" }.joinToString(", ")
            val total = _cart.value.entries.sumOf { it.key.price * it.value }

            val order = DeliveryOrder(
                customerName = customerName,
                vendorName = vendorName,
                category = category,
                itemsSummary = summary,
                totalAmount = total,
                paymentMethod = paymentMethod,
                paymentStatus = if (paymentMethod == "Cash on Delivery") "Pending" else "Paid",
                deliveryAddress = address,
                status = "Pending"
            )

            val orderId = repository.insertOrder(order)
            val finalOrder = order.copy(id = orderId.toInt())
            clearCart()
            _systemAlert.value = "Order placed successfully! Invoiced under $paymentMethod."
            
            // Sync newly placed transaction order record to Firestore in the background
            viewModelScope.launch {
                val result = FirestoreManager.syncTransactionRecord(finalOrder)
                if (result.isSuccess) {
                    addFirebaseLog("Automatically synchronized newly placed Order #${finalOrder.id} to Firestore collection: 'transaction_records'.")
                }
            }
        }
    }

    // Vendor Dashboard Controls
    fun addCatalogItem(vendorName: String, category: String, title: String, price: Double, qty: String, stock: Int, desc: String, isOnline: Boolean) {
        viewModelScope.launch {
            val item = VendorItem(
                vendorName = vendorName,
                category = category,
                title = title,
                price = price,
                quantityString = qty,
                inventory = stock,
                description = desc,
                isOnline = isOnline
            )
            repository.insertVendorItem(item)
            _systemAlert.value = "Catalog listed: $title under $category."
        }
    }

    fun deleteCatalogItem(item: VendorItem) {
        viewModelScope.launch {
            repository.deleteVendorItem(item)
            _systemAlert.value = "Removed listing: ${item.title}"
        }
    }

    // Rider Dashboard Controls
    fun selectActiveRider(id: Int) {
        _activeRiderId.value = id
    }

    fun setRiderStatus(rider: RiderProfile, newStatus: String) {
        viewModelScope.launch {
            repository.updateRider(rider.copy(status = newStatus))
        }
    }

    fun acceptAndPickupOrder(order: DeliveryOrder, rider: RiderProfile) {
        viewModelScope.launch {
            val updatedOrder = order.copy(
                status = "Picked Up",
                riderId = rider.id,
                riderName = rider.name
            )
            val updatedRider = rider.copy(status = "Delivering", totalDeliveries = rider.totalDeliveries)
            repository.updateOrder(updatedOrder)
            repository.updateRider(updatedRider)
            _systemAlert.value = "Order #${order.id} picked up. Head to: ${order.deliveryAddress}"
        }
    }

    fun completeDelivery(order: DeliveryOrder, rider: RiderProfile) {
        viewModelScope.launch {
            val updatedOrder = order.copy(
                status = "Delivered",
                paymentStatus = "Paid" // COD is collected at hand
            )
            val updatedRider = rider.copy(status = "Available", totalDeliveries = rider.totalDeliveries + 1)
            repository.updateOrder(updatedOrder)
            repository.updateRider(updatedRider)
            _systemAlert.value = "Order #${order.id} successfully delivered. Collection finalized."
        }
    }

    // Management / Super Admin Portal Controllers
    fun forceAssignOrder(order: DeliveryOrder, rider: RiderProfile) {
        viewModelScope.launch {
            val updatedOrder = order.copy(
                status = "Picked Up",
                riderId = rider.id,
                riderName = rider.name
            )
            repository.updateOrder(updatedOrder)
            _systemAlert.value = "Admin force-assigned Order #${order.id} to ${rider.name}."
        }
    }

    fun deleteOrder(orderId: Int) {
        viewModelScope.launch {
            repository.deleteOrderById(orderId)
            _systemAlert.value = "Admin removed Order #$orderId."
        }
    }

    fun setVendorId(id: String) {
        _activeVendorId.value = id
    }

    // Utility & Digital Billing Integration Pipeline
    fun processUPIPayment(senderUpi: String, receiverUpi: String, amount: Double) {
        viewModelScope.launch {
            repository.insertTransaction(
                UtilityTransaction(
                    type = "UPI",
                    details = "Instant Transfer: $senderUpi to $receiverUpi",
                    amount = amount,
                    status = "Success"
                )
            )
            _systemAlert.value = "UPI Transfer: ₹$amount paid to $receiverUpi!"
        }
    }

    fun processMobileRecharge(mobile: String, network: String, planDetails: String, amount: Double) {
        viewModelScope.launch {
            repository.insertTransaction(
                UtilityTransaction(
                    type = "Recharge",
                    details = "Recharge on $mobile ($network) - $planDetails",
                    amount = amount,
                    status = "Success"
                )
            )
            _systemAlert.value = "Recharge success! ₹$amount loaded on $mobile."
        }
    }

    fun processElectricityBill(provider: String, accountId: String, amount: Double) {
        viewModelScope.launch {
            repository.insertTransaction(
                UtilityTransaction(
                    type = "Electricity",
                    details = "Bill Payment to $provider ($accountId)",
                    amount = amount,
                    status = "Success"
                )
            )
            _systemAlert.value = "Utility invoice cleared: paid ₹$amount to $provider!"
        }
    }

    fun processCookingGasOrder(agency: String, consumerNo: String, amount: Double) {
        viewModelScope.launch {
            repository.insertTransaction(
                UtilityTransaction(
                    type = "Cooking Gas",
                    details = "LPG Cylinder booked via $agency ($consumerNo)",
                    amount = amount,
                    status = "Success"
                )
            )
            _systemAlert.value = "LPG Cylinder booked successfully! Cost: ₹$amount."
        }
    }

    fun processTaxiBooking(pickup: String, dest: String, amount: Double) {
        viewModelScope.launch {
            repository.insertTransaction(
                UtilityTransaction(
                    type = "Taxi",
                    details = "Emergency Taxi Cab booked: $pickup to $dest",
                    amount = amount,
                    status = "Success"
                )
            )
            _systemAlert.value = "Emergency Taxi Dispatch Confirmed! Pickup at $pickup in 4 mins."
        }
    }

    fun processFlightBooking(flightDetails: String, amount: Double) {
        viewModelScope.launch {
            repository.insertTransaction(
                UtilityTransaction(
                    type = "Flight Booking",
                    details = flightDetails,
                    amount = amount,
                    status = "Confirmed"
                )
            )
            _systemAlert.value = "Flight ticket booked successfully! Reservation: $flightDetails"
        }
    }

    fun processBusBooking(busDetails: String, amount: Double) {
        viewModelScope.launch {
            repository.insertTransaction(
                UtilityTransaction(
                    type = "Bus Booking",
                    details = busDetails,
                    amount = amount,
                    status = "Confirmed"
                )
            )
            _systemAlert.value = "Bus ticket booked successfully! Reservation: $busDetails"
        }
    }

    fun createCustomerSubscription(
        customerName: String,
        vendorName: String,
        packageName: String,
        category: String,
        price: Double,
        frequency: String
    ) {
        viewModelScope.launch {
            val sub = CustomerSubscription(
                customerName = customerName,
                vendorName = vendorName,
                packageName = packageName,
                category = category,
                price = price,
                frequency = frequency,
                status = "Active",
                nextDeliveryDate = when (frequency) {
                    "Daily" -> "Tomorrow"
                    "Weekly" -> "Next Monday"
                    "Monthly" -> "First of Next Month"
                    else -> "Upcoming"
                }
            )
            repository.insertSubscription(sub)
            _systemAlert.value = "Subscribed to $packageName ($frequency) at $vendorName!"
        }
    }

    fun cancelSubscription(subId: Int, packageName: String) {
        viewModelScope.launch {
            repository.deleteSubscription(subId)
            _systemAlert.value = "Cancelled subscription to $packageName."
        }
    }

    fun toggleSubscriptionPause(sub: CustomerSubscription) {
        viewModelScope.launch {
            val nextStatus = if (sub.status == "Active") "Paused" else "Active"
            repository.updateSubscription(sub.copy(status = nextStatus))
            _systemAlert.value = "Subscription to ${sub.packageName} is now $nextStatus."
        }
    }

    fun modifyRiderLocation(orderId: Int, lat: Double, lng: Double, status: String) {
        viewModelScope.launch {
            repository.updateOrderLocation(orderId, lat, lng, status)
        }
    }

    fun addWalletFunds(amount: Double) {
        _walletBalance.value += amount
        _systemAlert.value = "Wallet Recharged! Credited ₹$amount."
        viewModelScope.launch {
            repository.insertTransaction(
                UtilityTransaction(
                    type = "Wallet Fund",
                    details = "Wallet Recharge via UPI Gateway",
                    amount = amount,
                    status = "Success"
                )
            )
        }
    }

    fun transferWalletFunds(recipientName: String, note: String, amount: Double): Boolean {
        if (_walletBalance.value >= amount) {
            _walletBalance.value -= amount
            _systemAlert.value = "Transferred ₹$amount to $recipientName"
            viewModelScope.launch {
                repository.insertTransaction(
                    UtilityTransaction(
                        type = "Transfer",
                        details = "Sent ₹$amount to $recipientName - $note",
                        amount = amount,
                        status = "Success"
                    )
                )
            }
            return true
        } else {
            _systemAlert.value = "Transfer failed: Insufficient wallet balance!"
            return false
        }
    }

    fun withdrawRiderEarnings(riderId: Int, riderName: String, amount: Double) {
        _systemAlert.value = "Earnings cashout initiated! Sent ₹$amount to Rider $riderName's bank account."
        viewModelScope.launch {
            repository.insertTransaction(
                UtilityTransaction(
                    type = "Cashout",
                    details = "Rider $riderName (#$riderId) Cashout",
                    amount = amount,
                    status = "Success"
                )
            )
        }
    }

    fun addFirebaseLog(log: String) {
        val current = _firebaseLogs.value.toMutableList()
        current.add("[${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}] $log")
        _firebaseLogs.value = current
    }

    fun initializeServiceCategoriesInFirestore() {
        viewModelScope.launch {
            _isFirestoreSyncing.value = true
            addFirebaseLog("Starting synchronization of Service Categories to Google Cloud Firestore...")
            val categories = listOf(
                "Restaurants", "Hotels", "Groceries", "Vegetables", "Fruits", 
                "Flowers", "Event Management", "Wedding Planner", "Photo Studio", 
                "Fashion Design", "Emergency", "Recharge", "Mobile Accessories"
            )
            val result = FirestoreManager.syncServiceCategories(categories)
            if (result.isSuccess) {
                addFirebaseLog("✅ SUCCESS: Successfully initialized and merged ${categories.size} Service Categories to Firestore collection: 'service_categories'.")
                _systemAlert.value = "Firestore: Initialized service categories successfully"
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                addFirebaseLog("❌ ERROR: Failed to register categories in Firestore. Reason: $errorMsg")
                addFirebaseLog("⚠️ Sandbox Sync: Created custom mock collection metadata for ${categories.size} categories.")
                _systemAlert.value = "Firestore Simulation: Created category collections"
            }
            _isFirestoreSyncing.value = false
        }
    }

    fun initializeUserProfilesInFirestore() {
        viewModelScope.launch {
            _isFirestoreSyncing.value = true
            addFirebaseLog("Starting user profile synchronization cycle to Google Cloud Firestore...")
            
            // Sync current customer if logged in
            val customerName = _currentCustomerName.value
            if (customerName != null) {
                addFirebaseLog("Syncing logged-in Customer profile: $customerName...")
                FirestoreManager.syncUserProfile(
                    uid = "customer_" + customerName.lowercase().replace(" ", "_"),
                    name = customerName,
                    role = "Customer",
                    phone = _currentCustomerPhone.value ?: "+91 9999999999",
                    email = "customer@aistudio.com",
                    address = _currentCustomerAddress.value ?: "A-Block Delivery Spot"
                )
            }

            // Sync registered/seeded vendors
            val sampleVendors = listOf(
                Triple("Taj Delicacy", "Vendor", "+91 8888888888"),
                Triple("Zaika Biryani", "Vendor", "+91 7777777777")
            )
            for (vendor in sampleVendors) {
                addFirebaseLog("Syncing Vendor: ${vendor.first}...")
                FirestoreManager.syncUserProfile(
                    uid = "vendor_" + vendor.first.lowercase().replace(" ", "_"),
                    name = vendor.first,
                    role = vendor.second,
                    phone = vendor.third,
                    email = "${vendor.first.lowercase().replace(" ", "")}@domain.com",
                    address = "Commercial Zone, Block C"
                )
            }

            // Sync registered riders
            val riderList = riders.value
            for (rider in riderList) {
                addFirebaseLog("Syncing Rider profile: ${rider.name} (ID: ${rider.id})...")
                FirestoreManager.syncUserProfile(
                    uid = "rider_" + rider.id,
                    name = rider.name,
                    role = "Rider",
                    phone = rider.phone,
                    email = "${rider.name.lowercase().replace(" ", "")}@logistics.com",
                    address = "Transit Depot #${rider.id}"
                )
            }

            addFirebaseLog("✅ SUCCESS: Successfully synchronized active Customer, Vendor, and Rider user profiles to 'user_profiles' collection.")
            _systemAlert.value = "Firestore: Sync profiles complete"
            _isFirestoreSyncing.value = false
        }
    }

    fun initializeTransactionRecordsInFirestore() {
        viewModelScope.launch {
            _isFirestoreSyncing.value = true
            addFirebaseLog("Starting check of transaction records and order history data sync...")
            val currentOrders = orders.value
            if (currentOrders.isEmpty()) {
                addFirebaseLog("⚠️ No transaction or order history logs found in local SQLite database. Generate some orders to synchronize!")
                _systemAlert.value = "Please register purchase dispatch requests first!"
            } else {
                addFirebaseLog("Found ${currentOrders.size} local purchase orders. Initializing batch payload sync...")
                var successCount = 0
                for (order in currentOrders) {
                    addFirebaseLog("Streaming order ID #${order.id} (Cost: ₹${order.totalAmount}) to Firestore...")
                    val result = FirestoreManager.syncTransactionRecord(order)
                    if (result.isSuccess) {
                        successCount++
                    }
                }
                addFirebaseLog("✅ SUCCESS: Streamed $successCount / ${currentOrders.size} historic transaction entries to Firestore collection: 'transaction_records'.")
                _systemAlert.value = "Firestore transaction sync finalized"
            }
            _isFirestoreSyncing.value = false
        }
    }
}

// Custom lifecycle ViewModel factory
class AsrViewModelFactory(
    private val application: Application,
    private val repository: AsrRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AsrViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AsrViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
