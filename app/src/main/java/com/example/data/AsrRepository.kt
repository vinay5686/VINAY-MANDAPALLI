package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AsrRepository(private val asrDao: AsrDao) {

    val allVendorItems: Flow<List<VendorItem>> = asrDao.getAllVendorItems()
    val allOrders: Flow<List<DeliveryOrder>> = asrDao.getAllOrders()
    val allTransactions: Flow<List<UtilityTransaction>> = asrDao.getAllUtilityTransactions()
    val allRiders: Flow<List<RiderProfile>> = asrDao.getAllRiders()
    val allSubscriptions: Flow<List<CustomerSubscription>> = asrDao.getAllSubscriptions()

    fun getItemsByCategory(category: String): Flow<List<VendorItem>> =
        asrDao.getVendorItemsByCategory(category)

    suspend fun insertVendorItem(item: VendorItem) {
        asrDao.insertVendorItem(item)
    }

    suspend fun updateVendorItem(item: VendorItem) {
        asrDao.updateVendorItem(item)
    }

    suspend fun deleteVendorItem(item: VendorItem) {
        asrDao.deleteVendorItem(item)
    }

    suspend fun insertOrder(order: DeliveryOrder): Long {
        return asrDao.insertOrder(order)
    }

    suspend fun updateOrder(order: DeliveryOrder) {
        asrDao.updateOrder(order)
    }

    suspend fun deleteOrderById(orderId: Int) {
        asrDao.deleteOrderById(orderId)
    }

    suspend fun insertTransaction(transaction: UtilityTransaction): Long {
        return asrDao.insertUtilityTransaction(transaction)
    }

    suspend fun insertRider(rider: RiderProfile) {
        asrDao.insertRider(rider)
    }

    suspend fun updateRider(rider: RiderProfile) {
        asrDao.updateRider(rider)
    }

    suspend fun insertSubscription(sub: CustomerSubscription) {
        asrDao.insertSubscription(sub)
    }

    suspend fun updateSubscription(sub: CustomerSubscription) {
        asrDao.updateSubscription(sub)
    }

    suspend fun deleteSubscription(subId: Int) {
        asrDao.deleteSubscriptionById(subId)
    }

    suspend fun updateOrderLocation(orderId: Int, lat: Double, lng: Double, status: String) {
        asrDao.updateOrderLocation(orderId, lat, lng, status)
    }

    // Database Seeder
    suspend fun prepSeedDataIfEmpty() {
        val currentItems = asrDao.getAllVendorItems().first()
        if (currentItems.isEmpty()) {
            val initialItems = listOf(
                // Restaurants
                VendorItem(
                    vendorName = "Taj Delicacy",
                    category = "Restaurants",
                    title = "Premium Veg Thali",
                    price = 250.0,
                    quantityString = "Per Thali",
                    inventory = 100,
                    description = "A complete traditional Indian platter featuring paneer butter masala, dal makhani, layered paratha, butter rice, and gulab jamun dessert."
                ),
                VendorItem(
                    vendorName = "Zaika Biryani",
                    category = "Restaurants",
                    title = "Special Hyderabadi Chicken Biryani",
                    price = 320.0,
                    quantityString = "Portion (Serves 1-2)",
                    inventory = 80,
                    description = "Fragrant long-grain basmati rice cooked in slow steam with tender marinated chicken, authentic spices, served with special raita."
                ),

                // Hotels
                VendorItem(
                    vendorName = "Grand Regency Palace",
                    category = "Hotels",
                    title = "Deluxe Executive King Suite",
                    price = 4500.0,
                    quantityString = "Per Night",
                    inventory = 15,
                    description = "Luxury smart-lighting room featuring premium king-size bed, workstation, complimentary high-speed WiFi, mini-bar, and a panoramic city view balcony."
                ),

                // Groceries
                VendorItem(
                    vendorName = "Reliance Smart Store",
                    category = "Groceries",
                    title = "Premium Basmati Rice",
                    price = 450.0,
                    quantityString = "5 kg bag",
                    inventory = 250,
                    description = "Extra-long grain aged basmati rice perfect for delicious daily biryani and pulav. Cleaned and hygiene-packed."
                ),
                VendorItem(
                    vendorName = "Golden Harvest Co.",
                    category = "Groceries",
                    title = "Pure Cane Sugar",
                    price = 90.0,
                    quantityString = "2 kg pack",
                    inventory = 180,
                    description = "Sourced from finest sugarcane fields. 100% natural, refined white crystals for everyday sweet cooking and beverages."
                ),

                // Vegetables
                VendorItem(
                    vendorName = "Green Basket Co.",
                    category = "Vegetables",
                    title = "Fresh Organic Red Onions",
                    price = 40.0,
                    quantityString = "1 kg pack",
                    inventory = 300,
                    description = "Handpicked farm-fresh crispy red onions, direct from local organic growers. Rich in vitamins and natural sweetness."
                ),
                VendorItem(
                    vendorName = "Green Basket Co.",
                    category = "Vegetables",
                    title = "A-Grade Idaho Potatoes",
                    price = 30.0,
                    quantityString = "1 kg pack",
                    inventory = 200,
                    description = "High-quality, starchy potatoes perfect for baking, boiling, mashing, or crisp golden frying."
                ),

                // Fruits
                VendorItem(
                    vendorName = "Nature's Sweet Fruits",
                    category = "Fruits",
                    title = "Premium Gala Apples",
                    price = 180.0,
                    quantityString = "1 kg pack",
                    inventory = 150,
                    description = "Crispy, juicy sweet gala apples imported fresh. Excellent sweet snack filled with high dietary fiber."
                ),
                VendorItem(
                    vendorName = "Citrus Estates",
                    category = "Fruits",
                    title = "Sweet Sun-Ripened Oranges",
                    price = 120.0,
                    quantityString = "1 kg bag",
                    inventory = 120,
                    description = "Vitamin-C rich juicy seedless sweet oranges, freshly packaged. Ideal for quick morning orange juice squeezing."
                ),

                // Event Management & Wedding Planner
                VendorItem(
                    vendorName = "Royal Celebrations Inc.",
                    category = "Event Management",
                    title = "Corporate Gala Event Setup (Silver)",
                    price = 25000.0,
                    quantityString = "Per Event",
                    inventory = 5,
                    description = "Includes design consulting, stage setup, high-end audio setup, digital screen banners, backdrop design, and coordinate seating arrangements up to 100 guests."
                ),
                VendorItem(
                    vendorName = "Vivaah Wedding Planners",
                    category = "Wedding Planner",
                    title = "Exquisite Floral Entrance Decor",
                    price = 45000.0,
                    quantityString = "Per Wedding",
                    inventory = 3,
                    description = "Magnificent fresh flowers entryway backdrop, custom chandelier lighting, royal red carpet walkway, and twin greeting floral arches."
                ),

                // Photo Studio
                VendorItem(
                    vendorName = "Perfect Pixel Studio",
                    category = "Photo Studio",
                    title = "Pre-Wedding Cinematic Photography Package",
                    price = 15000.0,
                    quantityString = "Package Mode",
                    inventory = 10,
                    description = "2 hours professional session, 1 location. Deliverables include 30 fully edited high-res digital shots and a neat 2-minute short video trailer."
                ),

                // Fashion Design
                VendorItem(
                    vendorName = "Elegance Bridal Couture",
                    category = "Fashion Design",
                    title = "Designer Hand-Embroidered Lehenga",
                    price = 18000.0,
                    quantityString = "Custom Order",
                    inventory = 8,
                    description = "Beautiful custom-tailored heavy georgette bridal lehenga featuring magnificent zari and thread-embroidery, complete with matching master dupatta."
                ),

                // Mobile Accessories
                VendorItem(
                    vendorName = "Gadget World Accessories",
                    category = "Mobile Accessories",
                    title = "45W Superfast Dual Type-C Charger",
                    price = 1299.0,
                    quantityString = "Per Unit",
                    inventory = 95,
                    description = "Universal compact charger with dual smart-charging Type-C outputs, GaN technology preventing overheat, and short circuit safety lock."
                ),
                VendorItem(
                    vendorName = "Gadget World Accessories",
                    category = "Mobile Accessories",
                    title = "Premium Tempered Glass (9H Hardness)",
                    price = 199.0,
                    quantityString = "Per Piece",
                    inventory = 150,
                    description = "Ultimate scratch-resistant ultra-thin screen protector. Bubble-free adhesive, high touch sensitivity, and oil-resistant coating."
                ),

                // Flowers
                VendorItem(
                    vendorName = "Flora Blossom Boutique",
                    category = "Flowers",
                    title = "Premium Red Roses Bouquet",
                    price = 499.0,
                    quantityString = "12 Stems",
                    inventory = 40,
                    description = "Freshly cut premium long-stemmed red roses, beautifully hand-wrapped in dynamic parchment paper with white gypsophila and satin ribbon."
                ),
                VendorItem(
                    vendorName = "Flora Blossom Boutique",
                    category = "Flowers",
                    title = "Traditional Marigold Garland",
                    price = 150.0,
                    quantityString = "Per Meter",
                    inventory = 100,
                    description = "Vibrant orange and yellow fresh marigold flower garland, perfectly woven for holy rituals, temple offerings, or festive wedding decorations."
                ),
                VendorItem(
                    vendorName = "Flora Blossom Boutique",
                    category = "Flowers",
                    title = "Fragrant Jasmine (Mogra) Strings",
                    price = 120.0,
                    quantityString = "Per Pack",
                    inventory = 75,
                    description = "Pure mogra jasmine buds woven with care, offering strong sweet traditional scent. Ideal for adornments (gajras) or sacred purposes."
                )
            )

            for (item in initialItems) {
                asrDao.insertVendorItem(item)
            }
        }

        val currentRiders = asrDao.getAllRiders().first()
        if (currentRiders.isEmpty()) {
            val initialRiders = listOf(
                RiderProfile(name = "Rahul Sharma", status = "Available", phone = "+91 98765 43210", rating = 4.9f, totalDeliveries = 142),
                RiderProfile(name = "Amit Patel", status = "Available", phone = "+91 98765 43211", rating = 4.7f, totalDeliveries = 98),
                RiderProfile(name = "Vikram Singh", status = "Available", phone = "+91 98765 43212", rating = 4.8f, totalDeliveries = 215)
            )
            for (rider in initialRiders) {
                asrDao.insertRider(rider)
            }
        }
    }
}
