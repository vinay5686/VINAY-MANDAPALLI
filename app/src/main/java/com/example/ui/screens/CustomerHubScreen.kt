package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VendorItem
import com.example.ui.viewmodel.AsrViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHubScreen(
    viewModel: AsrViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.vendorItems.collectAsState()
    val cart by viewModel.cart.collectAsState()
    
    val isCustomerLoggedIn by viewModel.isCustomerLoggedIn.collectAsState()
    val currentCustomerName by viewModel.currentCustomerName.collectAsState()
    val currentCustomerPhone by viewModel.currentCustomerPhone.collectAsState()
    val currentCustomerAddress by viewModel.currentCustomerAddress.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    // UI state variables
    var selectedCategory by remember { mutableStateOf("Restaurants") }
    var currentCustomerTab by remember { mutableStateOf("MARKET") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Simulator sheet controls
    var showUpiSheet by remember { mutableStateOf(false) }
    var showRechargeSheet by remember { mutableStateOf(false) }
    var showGasSheet by remember { mutableStateOf(false) }
    var showElectricitySheet by remember { mutableStateOf(false) }
    var showTaxiSheet by remember { mutableStateOf(false) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showEmergencyGuideSheet by remember { mutableStateOf(false) }
    var showFlightSheet by remember { mutableStateOf(false) }
    var showBusSheet by remember { mutableStateOf(false) }

    val categories = listOf(
        "Restaurants", "Hotels", "Groceries", "Vegetables", 
        "Fruits", "Flowers", "Event Management", "Wedding Planner", 
        "Photo Studio", "Fashion Design", "Mobile Accessories"
    )

    val cartCount = cart.values.sum()
    val cartTotal = cart.entries.sumOf { it.key.price * it.value }

    if (!isCustomerLoggedIn) {
        CustomerLoginView(
            onLogin = { name, phone, address ->
                viewModel.loginCustomer(name, phone, address)
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Language Selection Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌐 Language:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                val languagesList = listOf(
                    "English" to "EN",
                    "Telugu" to "తెలుగు",
                    "Hindi" to "हिन्दी",
                    "Tamil" to "தமிழ்",
                    "Malayalam" to "മലയാളം"
                )
                languagesList.forEach { (fullLang, shortLang) ->
                    val isSelected = currentLanguage == fullLang
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                            .clickable { viewModel.setLanguage(fullLang) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("lang_btn_$fullLang"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortLang,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Customer Local Profile Status Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentCustomerName ?: "C").take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Logged in as: ${currentCustomerName ?: "Guest"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Phone: ${currentCustomerPhone ?: ""} • Place: ${currentCustomerAddress ?: ""}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    TextButton(
                        onClick = { viewModel.logoutCustomer() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                        modifier = Modifier.testTag("logout_customer_button")
                    ) {
                        Text(loc("sign_out", currentLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // Modern Tab row here below profile bar
            TabRow(
                selectedTabIndex = when (currentCustomerTab) {
                    "MARKET" -> 0
                    "SUBSCRIPTIONS" -> 1
                    "PAYMENTS" -> 2
                    "TRACKING" -> 3
                    else -> 0
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                Tab(
                    selected = currentCustomerTab == "MARKET",
                    onClick = { currentCustomerTab = "MARKET" },
                    text = { Text(loc("market", currentLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("customer_tab_market")
                )
                Tab(
                    selected = currentCustomerTab == "SUBSCRIPTIONS",
                    onClick = { currentCustomerTab = "SUBSCRIPTIONS" },
                    text = { Text(loc("subscriptions", currentLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("customer_tab_subscriptions")
                )
                Tab(
                    selected = currentCustomerTab == "PAYMENTS",
                    onClick = { currentCustomerTab = "PAYMENTS" },
                    text = { Text(loc("payments", currentLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("customer_tab_payments")
                )
                Tab(
                    selected = currentCustomerTab == "TRACKING",
                    onClick = { currentCustomerTab = "TRACKING" },
                    text = { Text(loc("tracking", currentLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("customer_tab_tracking")
                )
            }

            when (currentCustomerTab) {
                "MARKET" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        // Aesthetic Gradient Banner for Utility Shortcuts
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary
                                            )
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = loc("hub_title", currentLanguage),
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = loc("hub_subtitle", currentLanguage),
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 11.sp
                                            )
                                        }
                                        
                                        // Critical Emergency SOS Button
                                        Button(
                                            onClick = { showEmergencyGuideSheet = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("emergency_sos_button")
                                        ) {
                                            Icon(Icons.Default.Warning, contentDescription = "SOS", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(loc("sos", currentLanguage), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Shortcuts row (horizontally scrollable for seamless UX)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        UtilityShortcutItem(icon = Icons.Default.QrCodeScanner, label = loc("upi_pay", currentLanguage)) { showUpiSheet = true }
                                        UtilityShortcutItem(icon = Icons.Default.PhoneIphone, label = loc("recharge", currentLanguage)) { showRechargeSheet = true }
                                        UtilityShortcutItem(icon = Icons.Default.LocalGasStation, label = loc("cooking_gas", currentLanguage)) { showGasSheet = true }
                                        UtilityShortcutItem(icon = Icons.Default.ElectricBolt, label = loc("power_bill", currentLanguage)) { showElectricitySheet = true }
                                        UtilityShortcutItem(icon = Icons.Default.AirplanemodeActive, label = loc("flights", currentLanguage)) { showFlightSheet = true }
                                        UtilityShortcutItem(icon = Icons.Default.DirectionsBus, label = loc("bus_booking", currentLanguage)) { showBusSheet = true }
                                        UtilityShortcutItem(icon = Icons.Default.LocalTaxi, label = loc("taxicab", currentLanguage)) { showTaxiSheet = true }
                                    }
                                }
                            }
                        }

                        // Searching/Filtering area
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .testTag("customer_search_input"),
                            placeholder = { Text(loc("search_placeholder", currentLanguage)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal Category Row
                        Text(
                            text = loc("directory", currentLanguage),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { category ->
                                    val isSelected = category == selectedCategory
                                    Card(
                                        onClick = { selectedCategory = category },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                val catIcon = getCategoryVector(category)
                                                Icon(
                                                    imageVector = catIcon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = category,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Product List title
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$selectedCategory Offerings",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Interactive Local Database",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Live list loaded dynamically from Room
                        val filteredList = items.filter {
                            it.category.equals(selectedCategory, ignoreCase = true) &&
                                    (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.vendorName.contains(searchQuery, ignoreCase = true))
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Inbox,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No catalog registrations listed yet.",
                                        color = MaterialTheme.colorScheme.outline,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Switch to 'Vendor' profile to list new products or services to this database category.",
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredList) { item ->
                                    ServiceItemCard(
                                        item = item,
                                        quantityInCart = cart[item] ?: 0,
                                        onAdd = { viewModel.addToCart(item) },
                                        onRemove = { viewModel.removeFromCart(item) }
                                    )
                                }
                            }
                        }

                        // Mini Cart floating summary bar
                        AnimatedVisibility(
                            visible = cartCount > 0,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCartSheet = true },
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 10.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = "Cart", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "$cartCount items selected",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "From: ${cart.keys.firstOrNull()?.vendorName ?: ""}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹${String.format("%.2f", cartTotal)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Button(
                                            onClick = { showCartSheet = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text("View Basket", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "SUBSCRIPTIONS" -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        SubscriptionsTab(viewModel = viewModel, customerName = currentCustomerName ?: "")
                    }
                }
                "PAYMENTS" -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        PaymentsTab(viewModel = viewModel)
                    }
                }
                "TRACKING" -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        TrackingTab(viewModel = viewModel, customerName = currentCustomerName ?: "")
                    }
                }
            }
    }

    // Modal Sheet: UPI payment
    if (showUpiSheet) {
        UpiPaymentSimulatorDialog(
            onDismiss = { showUpiSheet = false },
            onSubmit = { amount, send, recv ->
                viewModel.processUPIPayment(send, recv, amount)
                showUpiSheet = false
            }
        )
    }

    // Modal Sheet: Mobile Recharge
    if (showRechargeSheet) {
        MobileRechargeSimulatorDialog(
            onDismiss = { showRechargeSheet = false },
            onSubmit = { mobile, net, plan, cost ->
                viewModel.processMobileRecharge(mobile, net, plan, cost)
                showRechargeSheet = false
            }
        )
    }

    // Modal Sheet: Cooking Gas Booking
    if (showGasSheet) {
        CookingGasSimulatorDialog(
            onDismiss = { showGasSheet = false },
            onSubmit = { agency, consumer, price ->
                viewModel.processCookingGasOrder(agency, consumer, price)
                showGasSheet = false
            }
        )
    }

    // Modal Sheet: Electricity bill clearing
    if (showElectricitySheet) {
        ElectricityBillSimulatorDialog(
            onDismiss = { showElectricitySheet = false },
            onSubmit = { board, consumerId, price ->
                viewModel.processElectricityBill(board, consumerId, price)
                showElectricitySheet = false
            }
        )
    }

    // Modal Sheet: Emergency Taxi Dispatch
    if (showTaxiSheet) {
        TaxiCabSimulatorDialog(
            onDismiss = { showTaxiSheet = false },
            onSubmit = { pickup, dest, cost ->
                viewModel.processTaxiBooking(pickup, dest, cost)
                showTaxiSheet = false
            }
        )
    }

    // Modal Sheet: Flight Booking Simulator
    if (showFlightSheet) {
        FlightBookingSimulatorDialog(
            onDismiss = { showFlightSheet = false },
            onSubmit = { details, cost ->
                viewModel.processFlightBooking(details, cost)
                showFlightSheet = false
            }
        )
    }

    // Modal Sheet: State Bus Booking Simulator
    if (showBusSheet) {
        BusBookingSimulatorDialog(
            onDismiss = { showBusSheet = false },
            onSubmit = { details, cost ->
                viewModel.processBusBooking(details, cost)
                showBusSheet = false
            }
        )
    }

    // Modal Sheet: Customer Cart checkout list
    if (showCartSheet) {
        CheckoutCartDialog(
            cart = cart,
            cartTotal = cartTotal,
            customerName = currentCustomerName ?: "",
            address = currentCustomerAddress ?: "",
            onDismiss = { showCartSheet = false },
            onCheckout = { custName, loc, payType ->
                viewModel.placeOrder(custName, loc, payType)
                showCartSheet = false
            },
            onAdd = { viewModel.addToCart(it) },
            onRemove = { viewModel.removeFromCart(it) }
        )
    }

    // Modal Sheet: Emergency GuidanceSOS
    if (showEmergencyGuideSheet) {
        EmergencyGuideDialog(
            onDismiss = { showEmergencyGuideSheet = false },
            onTriggerPanic = { department ->
                viewModel.showSystemAlert("🚨 DISPATCH PROTOCOL TRIGGERED: Emergency services dialed for $department. SIMULATOR: Help is en-route.")
                showEmergencyGuideSheet = false
            }
        )
    }
  }
}

// Utility Shortcut composable
@Composable
fun UtilityShortcutItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.23f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

// Service Catalogue Item Card
@Composable
fun ServiceItemCard(
    item: VendorItem,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("service_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual representative category badge/bullet block
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryVector(item.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.vendorName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (item.isOnline) MaterialTheme.colorScheme.tertiaryContainer 
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.isOnline) "DELIVERABLE" else "OFFLINE STATION",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isOnline) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${String.format("%.2f", item.price)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Unit: ${item.quantityString} • Stock: ${item.inventory}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Add to Basket Control Block
                    if (item.isOnline) {
                        if (quantityInCart == 0) {
                            Button(
                                onClick = onAdd,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_item_button_${item.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Remove, contentDescription = "Less", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "$quantityInCart",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "Add More", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    } else {
                        // Offline catalog - Directions button
                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Directions", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// SIMULATOR DIALOGS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiPaymentSimulatorDialog(
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, sendUpi: String, recvUpi: String) -> Unit
) {
    var amount by remember { mutableStateOf("15.00") }
    var sender by remember { mutableStateOf("mywallet@okasr") }
    var receiver by remember { mutableStateOf("tajdelicacy@okhdfc") }
    var pinCode by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ASR Pay - Instant UPI Simulator", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Transfer Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("upi_amount_input")
                )
                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text("My UPI Handle ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = receiver,
                    onValueChange = { receiver = it },
                    label = { Text("Receiver Merchant ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pinCode,
                    onValueChange = { 
                        if (it.length <= 4) {
                            pinCode = it
                            pinError = false
                        }
                    },
                    label = { Text("Enter 4-Digit Security PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    isError = pinError,
                    supportingText = { if (pinError) Text("Please type any 4-digit numeric code") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinCode.length == 4) {
                        onSubmit(amount.toDoubleOrNull() ?: 15.00, sender, receiver)
                    } else {
                        pinError = true
                    }
                },
                modifier = Modifier.testTag("upi_confirm_button")
            ) {
                Text("Authorize Transaction")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileRechargeSimulatorDialog(
    onDismiss: () -> Unit,
    onSubmit: (mobile: String, network: String, plan: String, cost: Double) -> Unit
) {
    var phone by remember { mutableStateOf("9876543210") }
    val networkOperators = listOf("Airtel Super", "Jio Hyper", "Vodafone Ultra", "BSNL Smart")
    var selectedOperator by remember { mutableStateOf("Jio Hyper") }
    
    val planMap = mapOf(
        "Standard Unlimited (₹149.00): Unlimited SMS/Calls & 1.5GB/day - 28 Days" to 149.00,
        "Premium Fast Stream (₹199.00): Unlimited Calls, 2.5GB/day & Disney+ - 28 Days" to 199.00,
        "Mega Storage (₹249.00): 100GB Bulk high-speed Internet data - 56 Days" to 249.00,
        "Economy Talk (₹99.00): Full talktime values and 2GB overall - 14 Days" to 99.00
    )
    var selectedPlanKey by remember { mutableStateOf(planMap.keys.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Mobile Recharge", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Enter 10-Digit Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("recharge_phone_input")
                )
                
                Text("Choose Operator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    networkOperators.forEach { op ->
                        val isSelected = op == selectedOperator
                        Card(
                            onClick = { selectedOperator = op },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = op.split(" ")[0],
                                fontSize = 10.sp,
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Select Recharge Bundle Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                planMap.keys.forEach { key ->
                    val isSelected = key == selectedPlanKey
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlanKey = key }
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = { selectedPlanKey = key })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = key, fontSize = 10.sp, lineHeight = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val cost = planMap[selectedPlanKey] ?: 149.00
                    onSubmit(phone, selectedOperator, selectedPlanKey.split(":")[0], cost)
                },
                modifier = Modifier.testTag("recharge_confirm_button")
            ) {
                Text("Process Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingGasSimulatorDialog(
    onDismiss: () -> Unit,
    onSubmit: (agency: String, consumer: String, cost: Double) -> Unit
) {
    var consumerNo by remember { mutableStateOf("4500129845") }
    var selectedAgency by remember { mutableStateOf("IndoGas Petroleum") }
    val agencies = listOf("IndoGas Petroleum", "HP Cylinder Services", "Bharat Gas LPG")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("LPG Cooking Gas booking", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Gas Provider Agency", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                agencies.forEach { agency ->
                    val selected = agency == selectedAgency
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAgency = agency }
                            .padding(6.dp)
                    ) {
                        RadioButton(selected = selected, onClick = { selectedAgency = agency })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(agency, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = consumerNo,
                    onValueChange = { consumerNo = it },
                    label = { Text("10-Digit Consumer Connection No.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("gas_consumer_input")
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Government Standard LPG Subsidized Price:", fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text("₹800.00", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedAgency, consumerNo, 800.00) },
                modifier = Modifier.testTag("gas_confirm_button")
            ) {
                Text("Book & Pay Cylinder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricityBillSimulatorDialog(
    onDismiss: () -> Unit,
    onSubmit: (board: String, accountId: String, cost: Double) -> Unit
) {
    var selectedState by remember { mutableStateOf("Andhra Pradesh") }
    val states = listOf(
        "Andhra Pradesh", "Telangana", "Tamil Nadu", "Kerala", "Karnataka",
        "Maharashtra", "Delhi", "Uttar Pradesh", "Gujarat", "West Bengal"
    )
    
    val boardsMap = mapOf(
        "Andhra Pradesh" to listOf("APSPDCL - Southern Power", "APEPDCL - Eastern Power"),
        "Telangana" to listOf("TSSPDCL - Southern Power", "TSNPDCL - Northern Power"),
        "Tamil Nadu" to listOf("TANGEDCO - Tamil Nadu Generation"),
        "Kerala" to listOf("KSEB - Kerala State Electricity Board"),
        "Karnataka" to listOf("BESCOM - Bangalore Electricity", "HESCOM - Hubli Electricity", "GESCOM - Gulbarga Electricity"),
        "Maharashtra" to listOf("MSEDCL - Mahadiscom Pvt Ltd"),
        "Delhi" to listOf("BSES Rajdhani Power", "BSES Yamuna Power", "Tata Power DDL"),
        "Uttar Pradesh" to listOf("UPPCL - Uttar Pradesh Corporation"),
        "Gujarat" to listOf("DGVCL - Dakshin Gujarat", "MGVCL - Madhya Gujarat"),
        "West Bengal" to listOf("WBSEDCL - West Bengal State Distribution")
    )
    
    var selectedBoard by remember { mutableStateOf(boardsMap["Andhra Pradesh"]!![0]) }
    
    // Auto reset selected board when state changes
    LaunchedEffect(selectedState) {
        selectedBoard = boardsMap[selectedState]?.firstOrNull() ?: "General State Power"
    }

    var accountId by remember { mutableStateOf("109847193") }
    var calculatedBill by remember { mutableStateOf(485.50) }
    var isFetched by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bharat BillPay (BBPS)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text(
                    text = "🇮🇳 Ministry of Power • National Electricity Gateway",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Dropdown or list of states
                Text("Select Indian State", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                // Horizontal scrolling state selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    states.forEach { state ->
                        val isSel = state == selectedState
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { selectedState = state }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = state,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "DISCOM Distribution Board ($selectedState)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Boards List
                val availableBoards = boardsMap[selectedState] ?: listOf("General Board")
                availableBoards.forEach { board ->
                    val selected = board == selectedBoard
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBoard = board }
                            .padding(4.dp)
                    ) {
                        RadioButton(selected = selected, onClick = { selectedBoard = board })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(board, fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("Enter Consumer ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("electricity_consumer_input")
                )

                Button(
                    onClick = { 
                        isFetched = true
                        calculatedBill = (350..2200).random().toDouble() + 0.45
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Fetch Outstanding Invoice (Govt Portal)", fontSize = 10.sp)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFetched) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f) 
                                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isFetched) "Outstanding Amount:" else "Last Month Cleared Bill:",
                                fontSize = 11.sp,
                                color = if (isFetched) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Verified via National Gateway Portal",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = "₹${String.format("%.2f", calculatedBill)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = if (isFetched) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedBoard, accountId, calculatedBill) },
                modifier = Modifier.testTag("electricity_confirm_button")
            ) {
                Text("Approve & Pay Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightBookingSimulatorDialog(
    onDismiss: () -> Unit,
    onSubmit: (details: String, cost: Double) -> Unit
) {
    var fromAirport by remember { mutableStateOf("DEL - New Delhi") }
    var toAirport by remember { mutableStateOf("HYD - Hyderabad") }
    val airports = listOf("DEL - New Delhi", "BOM - Mumbai", "HYD - Hyderabad", "BLR - Bengaluru", "MAA - Chennai", "CCU - Kolkata")
    val airlines = listOf("IndiGo (Fly6E)", "Air India (AI)", "Akasa Air (QP)", "Vistara (UK)", "Star Air (S5)")
    var selectedAirline by remember { mutableStateOf("IndiGo (Fly6E)") }
    var passengerName by remember { mutableStateOf("Amit Kumar") }
    
    val flightCost = when (selectedAirline) {
        "Air India (AI)" -> 5499.0
        "Vistara (UK)" -> 6199.0
        "Akasa Air (QP)" -> 4299.0
        "Star Air (S5)" -> 3999.0
        else -> 4599.0 // IndiGo
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AirplanemodeActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Domestic Flight Booking", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = passengerName,
                    onValueChange = { passengerName = it },
                    label = { Text("Lead Passenger Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("From Airport", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        airports.filter { it != toAirport }.forEach { port ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { fromAirport = port }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(selected = (fromAirport == port), onClick = { fromAirport = port })
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(port.split(" - ")[0], fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("To Airport", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        airports.filter { it != fromAirport }.forEach { port ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { toAirport = port }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(selected = (toAirport == port), onClick = { toAirport = port })
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(port.split(" - ")[0], fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text("Choose Airline Operator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    airlines.forEach { line ->
                        val isSel = line == selectedAirline
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primaryContainer 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedAirline = line }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(line, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Base Fare & Taxes:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text("₹${String.format("%.2f", flightCost)}", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                        Text("Includes 15 kg Check-in & 7 kg Cabin Baggage", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val code = "ASR" + (100..999).random()
                    onSubmit("$selectedAirline Flight ($code) from $fromAirport to $toAirport for Passenger $passengerName", flightCost)
                }
            ) {
                Text("Confirm Boarding Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusBookingSimulatorDialog(
    onDismiss: () -> Unit,
    onSubmit: (details: String, cost: Double) -> Unit
) {
    var selectedState by remember { mutableStateOf("Andhra Pradesh") }
    val statesList = listOf("Andhra Pradesh", "Telangana", "Tamil Nadu", "Karnataka", "Maharashtra", "Uttar Pradesh", "Kerala")
    
    val routesByState = mapOf(
        "Andhra Pradesh" to listOf("Vijayawada ⇄ Tirupati (APSRTC)", "Visakhapatnam ⇄ Hyderabad (APSRTC)", "Nellore ⇄ Bangalore (APSRTC)"),
        "Telangana" to listOf("Hyderabad ⇄ Warangal (TSRTC)", "Nizamabad ⇄ Hyderabad (TSRTC)", "Khammam ⇄ Vijayawada (TSRTC)"),
        "Tamil Nadu" to listOf("Chennai ⇄ Madurai (SETC)", "Coimbatore ⇄ Chennai (SETC)", "Trichy ⇄ Bangalore (SETC)"),
        "Karnataka" to listOf("Bangalore ⇄ Mysore (KSRTC Airavat)", "Mangalore ⇄ Bangalore (KSRTC)", "Hubli ⇄ Pune (KSRTC)"),
        "Maharashtra" to listOf("Mumbai ⇄ Pune (MSRTC Shivneri)", "Nagpur ⇄ Aurangabad (MSRTC)", "Nashik ⇄ Mumbai (MSRTC)"),
        "Uttar Pradesh" to listOf("Lucknow ⇄ Delhi (UPSRTC Janrath)", "Agra ⇄ Varanasi (UPSRTC)", "Kanpur ⇄ Gorakhpur (UPSRTC)"),
        "Kerala" to listOf("Trivandrum ⇄ Kochi (KSRTC Swift)", "Kozhikode ⇄ Bangalore (KSRTC)", "Palakkad ⇄ Coimbatore (KSRTC)")
    )
    
    var selectedRoute by remember { mutableStateOf(routesByState["Andhra Pradesh"]!![0]) }
    
    LaunchedEffect(selectedState) {
        selectedRoute = routesByState[selectedState]?.firstOrNull() ?: "State Express Router"
    }

    val busTypes = listOf("Rajadhani Super Luxury", "Garuda Plus Multi-Axle", "Palle Velugu")
    var selectedType by remember { mutableStateOf("Garuda Plus Multi-Axle") }
    
    // Choose seat list state
    var selectedSeats by remember { mutableStateOf(setOf("10A", "10B")) }
    
    val baseSeatCost = when (selectedType) {
        "Garuda Plus Multi-Axle" -> 850.0
        "Rajadhani Super Luxury" -> 550.0
        else -> 280.0
    }
    
    val totalCost = baseSeatCost * selectedSeats.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("State RTC Bus Booking", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Indian State Corporation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statesList.forEach { state ->
                        val isSel = state == selectedState
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { selectedState = state }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = state,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text("Select Available State-wise Route", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                val routes = routesByState[selectedState] ?: listOf("Route Delta")
                routes.forEach { r ->
                    val isSel = r == selectedRoute
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRoute = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSel, onClick = { selectedRoute = r })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(r, fontSize = 11.sp)
                    }
                }

                Text("Coach Comfort Class", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    busTypes.forEach { bt ->
                        val isSel = bt == selectedType
                        Card(
                            onClick = { selectedType = bt },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = bt,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text("Select Seats (${selectedSeats.size} chosen)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                // Seat Matrix representation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("08A", "08B", "09A", "09B", "10A", "10B", "11A", "11B").forEach { seat ->
                        val isChosen = selectedSeats.contains(seat)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isChosen) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    val current = selectedSeats.toMutableSet()
                                    if (current.contains(seat)) {
                                        current.remove(seat)
                                    } else {
                                        current.add(seat)
                                    }
                                    selectedSeats = current
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                seat, 
                                fontSize = 9.sp, 
                                color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Subtotal State-RTC Fare:", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            Text("₹$baseSeatCost per seat Ticket", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text("₹${String.format("%.2f", totalCost)}", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedSeats.isEmpty()) {
                        onSubmit("$selectedRoute • Coach: $selectedType • Seats: General Standee", totalCost)
                    } else {
                        onSubmit("$selectedRoute • Coach: $selectedType • Seats: ${selectedSeats.joinToString(", ")}", totalCost)
                    }
                }
            ) {
                Text("Confirm Reserved Seats")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiCabSimulatorDialog(
    onDismiss: () -> Unit,
    onSubmit: (pickup: String, dest: String, cost: Double) -> Unit
) {
    var pickup by remember { mutableStateOf("12 Baker Street, Central Plaza") }
    var destination by remember { mutableStateOf("State General Hospital Group") }
    var selectedType by remember { mutableStateOf("Standard Taxi") }
    var simulatedProgress by remember { mutableFloatStateOf(0.0f) }
    var isSearching by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    val options = mapOf(
        "Standard Taxi (₹150.0)" to 150.0,
        "Premium Comfort Cab (₹250.0)" to 250.0,
        "Emergency Hospital Taxi (₹120.0)" to 120.0
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ASR Emergency Taxi dispatch", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = pickup,
                    onValueChange = { pickup = it },
                    label = { Text("Enter Pickup Address") },
                    modifier = Modifier.fillMaxWidth().testTag("taxi_pickup_input")
                )
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Enter Drop Destination") },
                    modifier = Modifier.fillMaxWidth().testTag("taxi_dest_input")
                )

                Text("Pick Vehicle Type", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                options.keys.forEach { op ->
                    val selected = op.startsWith(selectedType)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = op.split(" (₹")[0] }
                            .padding(4.dp)
                    ) {
                        RadioButton(selected = selected, onClick = { selectedType = op.split(" (₹")[0] })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(op, fontSize = 12.sp)
                    }
                }

                if (isSearching) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Finding nearest driver...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(progress = { simulatedProgress }, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    // Minimal decorative visual GPS line simulator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outline)
                            Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color.Red)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isSearching) {
                        isSearching = true
                        scope.launch {
                            for (i in 1..10) {
                                delay(200)
                                simulatedProgress = i / 10f
                            }
                            isSearching = false
                            val finalCost = options.entries.firstOrNull { it.key.startsWith(selectedType) }?.value ?: 150.0
                            onSubmit(pickup, destination, finalCost)
                        }
                    }
                },
                modifier = Modifier.testTag("taxi_confirm_button")
            ) {
                Text(if (isSearching) "Searching..." else "Book Taxi Cab")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSearching) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutCartDialog(
    cart: Map<VendorItem, Int>,
    cartTotal: Double,
    customerName: String,
    address: String,
    onDismiss: () -> Unit,
    onCheckout: (customerName: String, address: String, paymentMethod: String) -> Unit,
    onAdd: (VendorItem) -> Unit,
    onRemove: (VendorItem) -> Unit
) {
    var name by remember { mutableStateOf(customerName) }
    var address by remember { mutableStateOf(address) }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Service Checkout", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Items from: ${cart.keys.firstOrNull()?.vendorName ?: "Vendor"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Scrollable summary of products
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    LazyColumn(contentPadding = PaddingValues(8.dp)) {
                        items(cart.entries.toList()) { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.key.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text("₹${entry.key.price} x ${entry.value}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onRemove(entry.key) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Text("${entry.value}", fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                    IconButton(onClick = { onAdd(entry.key) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Divider()

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("checkout_name_input")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Address Details") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("checkout_address_input")
                )

                Text("Payment Settlement Method", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val methods = listOf(
                        "UPI" to "UPI",
                        "Cash" to "Cash on Delivery",
                        "Wallet" to "Wallet"
                    )
                    methods.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { paymentMethod = value }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == value,
                                onClick = { paymentMethod = value },
                                modifier = Modifier.testTag("checkout_payment_${label.lowercase()}")
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Bill Invoiced:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("₹${String.format("%.2f", cartTotal)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCheckout(name, address, paymentMethod) },
                modifier = Modifier.testTag("checkout_submit_button")
            ) {
                Text("Confirm & Disperse Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyGuideDialog(
    onDismiss: () -> Unit,
    onTriggerPanic: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Hotline Center", color = Color.Red, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Press any department below to simulate an instantaneous cellular hotline dial and automatic GPS coordinate sharing for responders.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                EmergencyDialRow(title = "Police Department (Security Control)", phone = "100 / 911", color = Color(0xFF1E3A8A)) {
                    onTriggerPanic("Police department")
                }
                EmergencyDialRow(title = "Fire & Rescue Station Services", phone = "101 / 911", color = Color(0xFFB91C1C)) {
                    onTriggerPanic("Fire services")
                }
                EmergencyDialRow(title = "City Hospital & Ambulatory Medical", phone = "102 / 911", color = Color(0xFF047857)) {
                    onTriggerPanic("General hospital rescue")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)) {
                Text("Close Protocol")
            }
        }
    )
}

@Composable
fun EmergencyDialRow(
    title: String,
    phone: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                Text("Emergency Hotline Dial: $phone", fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
            }
            Icon(Icons.Default.Phone, contentDescription = null, tint = color)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SubscriptionsTab(viewModel: AsrViewModel, customerName: String) {
    val activeSubs by viewModel.subscriptions.collectAsState()
    val mySubs = activeSubs.filter { it.customerName == customerName }
    
    val premiumPlans = remember {
        listOf(
            Triple("Flora Blossom Boutique", "Daily Mogra & Jasmine Gajra Pack", 150.0),
            Triple("Flora Blossom Boutique", "Weekly Red Roses Premium Bouquet", 450.0),
            Triple("Taj Delicacy", "Daily North/South Executive Luncheon", 1499.0),
            Triple("Green Basket Co.", "Daily Fresh Cut Fruits Basket", 299.0),
            Triple("Reliance Smart Store", "Daily Dairy, Milk & Bread Staples", 399.0),
            Triple("Golden Harvest Co.", "Weekly Premium Bakery Snack Box", 249.0)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explainer banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Multi-Day Subscriptions", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Subscribe once and receive fresh deliveries automatically. Perfect for milk, lunch meals, flowers, or fresh organic fruit packs!", fontSize = 11.sp)
                    }
                }
            }
        }

        // Available subscription packages
        item {
            Text("Browse Subscription Offerings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(premiumPlans) { p ->
                        Card(
                            modifier = Modifier.width(190.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(p.first, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(p.second, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("₹${p.third}", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    Button(
                                        onClick = {
                                            viewModel.createCustomerSubscription(
                                                customerName = customerName,
                                                vendorName = p.first,
                                                packageName = p.second,
                                                category = "Subscription",
                                                price = p.third,
                                                frequency = "Weekly"
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Subscribe", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Customer's current subscription plans
        item {
            Text("My Active Subscriptions (${mySubs.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (mySubs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No active subscriptions currently. Purchase a package above to begin your service!", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(mySubs) { sub ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(sub.vendorName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(sub.packageName, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (sub.status == "Active") Color.Green.copy(alpha = 0.15f) else Color.Yellow.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(sub.status.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (sub.status == "Active") Color(0xFF047857) else Color(0xFFD97706))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Delivery: ${sub.frequency} • Cost: ₹${sub.price}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text("Next Parcel: ${sub.nextDeliveryDate}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleSubscriptionPause(sub) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("subscription_pause_${sub.id}")
                                ) {
                                    Text(if (sub.status == "Active") "Pause" else "Resume", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.cancelSubscription(sub.id, sub.packageName) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("subscription_cancel_${sub.id}")
                                ) {
                                    Text("Unsubscribe", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsTab(viewModel: AsrViewModel) {
    val walletBalance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    
    var recipientPhone by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Glowing Wallet balance card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("ASR Unified Wallet Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("₹${String.format("%.2f", walletBalance)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text("Fast Top-Up Gateway Connection:", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(100.0, 500.0, 1000.0).forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.addWalletFunds(amt) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+₹${amt.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // P2P Money Transfer Hub
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("PEER-TO-PEER INSTANT TRANSFER PORTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        modifier = Modifier.fillMaxWidth().testTag("transfer_recipient_input"),
                        label = { Text("Recipient UPI ID, Mobile No, or A/C") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = transferAmount,
                            onValueChange = { transferAmount = it },
                            modifier = Modifier.weight(1f).testTag("transfer_amount_input"),
                            label = { Text("Amount (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            modifier = Modifier.weight(1.5f).testTag("transfer_memo_input"),
                            label = { Text("Remark/Note") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            val amtValue = transferAmount.toDoubleOrNull()
                            if (amtValue != null && recipientPhone.isNotEmpty()) {
                                val ok = viewModel.transferWalletFunds(recipientPhone, memo, amtValue)
                                if (ok) {
                                    recipientPhone = ""
                                    transferAmount = ""
                                    memo = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("transfer_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.SendToMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Initiate Wallet Instant Transfer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Ledger List
        item {
            Text("Payments Transaction Ledger", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (transactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No billing records in current ledger pool.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            items(transactions) { tr ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = when (tr.type) {
                                    "Wallet Fund", "Transfer" -> Icons.Default.AccountBalanceWallet
                                    "UPI" -> Icons.Default.QrCodeScanner
                                    "Recharge" -> Icons.Default.PhoneIphone
                                    "Electricity", "Power" -> Icons.Default.ElectricBolt
                                    "Cooking Gas" -> Icons.Default.LocalGasStation
                                    "Taxi" -> Icons.Default.LocalTaxi
                                    else -> Icons.Default.ReceiptLong
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(tr.type.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                Text(tr.details, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        
                        Text(
                            text = "₹${String.format("%.2f", tr.amount)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (tr.type == "Wallet Fund") Color(0xFF047857) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingTab(viewModel: AsrViewModel, customerName: String) {
    val orders by viewModel.orders.collectAsState()
    val myActiveOrders = orders.filter { it.customerName == customerName && it.status != "Delivered" }
    val myAllOrders = orders.filter { it.customerName == customerName }.sortedByDescending { it.timestamp }
    val sdf = remember { java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("customer_tracking_and_history_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Shipments Header & Section
        item {
            Text(
                text = "Track In-Transit Shipments",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (myActiveOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Shipments currently in transit",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Place a new shopping dispatch request in the 'Market' tab to see live delivery tracking here.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            val order = myActiveOrders.first()
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("live_tracking_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Rider Live Coordinates Pipeline",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    order.status.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(order.itemsSummary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Shipping From: ${order.vendorName} • To: ${order.deliveryAddress}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            
                            drawCircle(color = Color(0xFFCBD5E1), radius = 8f, center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.5f))
                            drawCircle(color = Color(0xFF94A3B8), radius = 8f, center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.5f))
                            
                            val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            drawContext.canvas.drawLine(
                                p1 = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.5f),
                                p2 = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.5f),
                                paint = androidx.compose.ui.graphics.Paint().apply {
                                    this.color = Color(0xFF64748B)
                                    this.strokeWidth = 6f
                                    this.pathEffect = pathEffect
                                }
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 20.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(24.dp))
                                Text("Store", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 20.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(24.dp))
                                Text("Home", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val fraction = when (order.riderLocationStatus) {
                            "At Vendor Store" -> 0.15f
                            "In Transit (Near Highway)" -> 0.45f
                            "Arrived at Layout" -> 0.72f
                            "Ringing Doorbell" -> 0.88f
                            else -> 0.15f
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                        ) {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val riderX = maxWidth * fraction - 16.dp
                                Box(
                                    modifier = Modifier
                                        .absoluteOffset(x = riderX, y = maxHeight / 2 - 24.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBike,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text("Rider", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (order.riderLatitude == 0.0) "Waiting for Rider GPS beacon broadcast..."
                                           else "GPS Location: (${String.format("%.4f", order.riderLatitude)}, ${String.format("%.4f", order.riderLongitude)})",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val trackingEta = when (order.riderLocationStatus) {
                        "At Vendor Store" -> "14 mins"
                        "In Transit (Near Highway)" -> "8 mins"
                        "Arrived at Layout" -> "3 mins"
                        "Ringing Doorbell" -> "Now"
                        else -> "Calibrating..."
                    }
                    val trackingDistance = when (order.riderLocationStatus) {
                        "At Vendor Store" -> "3.4 km left"
                        "In Transit (Near Highway)" -> "1.8 km left"
                        "Arrived at Layout" -> "0.4 km left"
                        "Ringing Doorbell" -> "At doorstep"
                        else -> "Pending"
                    }
                    
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RIDER ETA", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                            Text(trackingEta, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DISTANCE REMAINING", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                            Text(trackingDistance, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsBike, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Assigned Delivery Courier", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                                Text(order.riderName.ifEmpty { "Rahul (Courier)" }, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Status: ${order.riderLocationStatus.ifEmpty { "En Route" }}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Rider", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Customer Purchase History Section Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📜 My Order & Purchase History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${myAllOrders.size} Orders Total",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (myAllOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "You haven't placed any purchases yet! Start shopping items from local merchants in the 'Market' tab.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(myAllOrders) { histOrder ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("historical_order_item_${histOrder.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val categoryIcon = getCategoryVector(histOrder.category)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = categoryIcon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = histOrder.vendorName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = sdf.format(java.util.Date(histOrder.timestamp)),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            // Order Code Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#ASR-${1000 + histOrder.id}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                        )

                        Text(
                            text = histOrder.itemsSummary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Total Paid: ",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "₹${String.format("%.2f", histOrder.totalAmount)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "paid via ${histOrder.paymentMethod} (${histOrder.paymentStatus})",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Delivery Status indicator
                            val statusBgColor = when (histOrder.status) {
                                "Delivered" -> Color(0xFF047857).copy(alpha = 0.12f)
                                "Picked Up" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            }
                            val statusTextColor = when (histOrder.status) {
                                "Delivered" -> Color(0xFF047857)
                                "Picked Up" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusBgColor)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val statusIcon = when (histOrder.status) {
                                        "Delivered" -> Icons.Default.CheckCircle
                                        "Picked Up" -> Icons.Default.DirectionsBike
                                        else -> Icons.Default.Schedule
                                    }
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = null,
                                        tint = statusTextColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = histOrder.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusTextColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Delivered Spot: ${histOrder.deliveryAddress}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// Global category icons mapper helper
fun getCategoryVector(category: String): ImageVector {
    return when (category) {
        "Restaurants" -> Icons.Default.Restaurant
        "Hotels" -> Icons.Default.Hotel
        "Groceries" -> Icons.Default.LocalGroceryStore
        "Vegetables" -> Icons.Default.Grass
        "Fruits" -> Icons.Default.ShoppingBag
        "Flowers" -> Icons.Default.LocalFlorist
        "Event Management" -> Icons.Default.SettingsSuggest
        "Wedding Planner" -> Icons.Default.Celebration
        "Photo Studio" -> Icons.Default.PhotoCamera
        "Fashion Design" -> Icons.Default.Checkroom
        "Emergency" -> Icons.Default.Emergency
        "Recharge" -> Icons.Default.PhoneAndroid
        "Mobile Accessories" -> Icons.Default.Headphones
        else -> Icons.Default.MiscellaneousServices
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLoginView(
    onLogin: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var loginMethod by remember { mutableStateOf("Phone") } // "Phone" or "Email"
    
    var otpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var userOtpInput by remember { mutableStateOf("") }
    
    var termsAccepted by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "Namaste!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Sign in as customer to browse offerings, book service dispatches, and pay bills.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (!otpSent) "Customer Registration" else "Enter Security OTP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!otpSent) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("customer_login_name_input")
                        )

                        // Choice of Login via Phone or Mail
                        Text(
                            text = "Get verification OTP via:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (loginMethod == "Phone") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable { loginMethod = "Phone" }
                                    .padding(8.dp)
                            ) {
                                RadioButton(
                                    selected = loginMethod == "Phone",
                                    onClick = { loginMethod = "Phone" }
                                )
                                Text("Phone (+91)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (loginMethod == "Email") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable { loginMethod = "Email" }
                                    .padding(8.dp)
                            ) {
                                RadioButton(
                                    selected = loginMethod == "Email",
                                    onClick = { loginMethod = "Email" }
                                )
                                Text("Email (Mail)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (loginMethod == "Phone") {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Mobile Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                prefix = { Text("+91 ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("customer_login_phone_input")
                            )
                        } else {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-mail Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("customer_login_email_input")
                            )
                        }

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Delivery Address / Spot") },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("customer_login_address_input")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { termsAccepted = !termsAccepted }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = termsAccepted,
                                onCheckedChange = { termsAccepted = it },
                                modifier = Modifier.testTag("customer_login_terms_checkbox")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "I confirm my details under IT Act 2000 and Consumer Protection Rules.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        // OTP is sent!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📟 SIMULATED TELECOM NETWORK OUT-OF-BAND CARRIER:",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "[OTP SENT] Verification PIN: $generatedOtp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Delivered " + (if (loginMethod == "Phone") "via SMS to +91 $phone" else "via Secure Mail to $email"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = userOtpInput,
                            onValueChange = { userOtpInput = it },
                            label = { Text("Enter 4-Digit Security Code") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("customer_login_otp_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    generatedOtp = (1000..9999).random().toString()
                                    userOtpInput = ""
                                }
                            ) {
                                Text("Resend OTP", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = {
                                    otpSent = false
                                    userOtpInput = ""
                                }
                            ) {
                                Text("Change Details", fontSize = 12.sp)
                            }
                        }
                    }

                    if (showError) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (!otpSent) {
                                if (name.isBlank()) {
                                    errorMessage = "Please enter your Full Name"
                                    showError = true
                                } else if (loginMethod == "Phone" && (phone.length != 10 || phone.any { !it.isDigit() })) {
                                    errorMessage = "Please enter a valid 10-digit mobile number"
                                    showError = true
                                } else if (loginMethod == "Email" && (!email.contains("@") || !email.contains("."))) {
                                    errorMessage = "Please enter a valid Email Address"
                                    showError = true
                                } else if (address.isBlank()) {
                                    errorMessage = "Please enter your Delivery Address"
                                    showError = true
                                } else if (!termsAccepted) {
                                    errorMessage = "Please accept the verification terms"
                                    showError = true
                                } else {
                                    showError = false
                                    generatedOtp = (1000..9999).random().toString()
                                    otpSent = true
                                }
                            } else {
                                if (userOtpInput == generatedOtp) {
                                    showError = false
                                    val contactInfo = if (loginMethod == "Phone") "+91 $phone" else email
                                    onLogin(name, contactInfo, address)
                                } else {
                                    errorMessage = "Invalid verification code. Please input exact PIN shown in network carrier banner."
                                    showError = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("customer_login_submit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (!otpSent) "Request Secure OTP" else "Validate OTP & Begin Session",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// Localization dictionary for languages: English, Telugu, Hindi, Tamil, Malayalam
fun loc(key: String, lang: String): String {
    return when (lang) {
        "Telugu" -> when (key) {
            "market" -> "మార్కెట్"
            "subscriptions" -> "సందాలు"
            "payments" -> "చెల్లింపులు"
            "tracking" -> "ట్రాకింగ్"
            "hub_title" -> "అత్యవసర & యుటిలిటీ హబ్"
            "hub_subtitle" -> "అన్ని క్లిష్టమైన సేవలు తక్షణమే పరిష్కరించబడతాయి"
            "sos" -> "అత్యవసర SOS"
            "upi_pay" -> "యూపీఐ పే"
            "recharge" -> "రీఛార్జ్"
            "cooking_gas" -> "వంట గ్యాస్"
            "power_bill" -> "కరెంట్ బిల్లు"
            "flights" -> "విమాన టికెట్"
            "bus_booking" -> "బస్సు టికెట్"
            "taxicab" -> "టాక్సీ క్యాబ్"
            "directory" -> "సేవా విభాగం"
            "search_placeholder" -> "ఉత్పత్తులు లేదా సేవలను వెతకండి..."
            "sign_out" -> "లాగ్ అవుట్"
            "outstanding_due" -> "బకాయి ఉన్న బిల్లు:"
            else -> key
        }
        "Hindi" -> when (key) {
            "market" -> "बाजार"
            "subscriptions" -> "सदस्यता"
            "payments" -> "भुगतान"
            "tracking" -> "ट्रैकिंग"
            "hub_title" -> "आपातकालीन और उपयोगिता हब"
            "hub_subtitle" -> "सभी महत्वपूर्ण सेवाएं तुरंत हल होती हैं"
            "sos" -> "आपातकालीन एसओएस"
            "upi_pay" -> "यूपीआई भुगतान"
            "recharge" -> "रिचार्ज"
            "cooking_gas" -> "रसोई गैस"
            "power_bill" -> "बिजली बिल"
            "flights" -> "फ्लाइट बुकिंग"
            "bus_booking" -> "बस बुकिंग"
            "taxicab" -> "टैक्सी कैब"
            "directory" -> "सेवा निर्देशिका"
            "search_placeholder" -> "उत्पादों या सेवाओं की खोज करें..."
            "sign_out" -> "लॉग आउट"
            "outstanding_due" -> "बकाया बिजली बिल:"
            else -> key
        }
        "Tamil" -> when (key) {
            "market" -> "சந்தை"
            "subscriptions" -> "சந்தாக்கள்"
            "payments" -> "பணம் செலுத்துதல்"
            "tracking" -> "டிராக்கிங்"
            "hub_title" -> "அவசர & ஜுடிலிட்டி மையம்"
            "hub_subtitle" -> "அனைத்து முக்கிய சேவைகளும் உடனடியாக தீர்க்கப்படும்"
            "sos" -> "அவசர SOS"
            "upi_pay" -> "யுபிஐ கட்டணம்"
            "recharge" -> "ரீசார்ஜ்"
            "cooking_gas" -> "சமையல் எரிவாயு"
            "power_bill" -> "மின்சார கட்டணம்"
            "flights" -> "விமான முன்பதிவு"
            "bus_booking" -> "பேருந்து முன்பதிவு"
            "taxicab" -> "டாக்ஸி"
            "directory" -> "சேவை அடைவு"
            "search_placeholder" -> "தயாரிப்புகள் அல்லது சேவைகளைத் தேடுங்கள்..."
            "sign_out" -> "வெளியேறு"
            "outstanding_due" -> "நிலுவையில் உள்ள மின் கட்டணம்:"
            else -> key
        }
        "Malayalam" -> when (key) {
            "market" -> "മാർക്കറ്റ്"
            "subscriptions" -> "വരിക്കാരൻ"
            "payments" -> "പേയ്മെന്റുകൾ"
            "tracking" -> "ട്രാക്കിംഗ്"
            "hub_title" -> "എമർജൻസി & യൂട്ടിലിറ്റി ഹബ്"
            "hub_subtitle" -> "എല്ലാ നിർണായക സേവനങ്ങളും ഉടനടി പരിഹരിക്കും"
            "sos" -> "എമർജൻസി SOS"
            "upi_pay" -> "യുപിഐ പേ"
            "recharge" -> "റീചാർജ്ജ്"
            "cooking_gas" -> "പാചക വാതകം"
            "power_bill" -> "വൈദ്യുതി ബിൽ"
            "flights" -> "ഫ്ലൈറ്റ് ബുക്കിംഗ്"
            "bus_booking" -> "ബസ് ബുക്കിംഗ്"
            "taxicab" -> "ടാക്സി"
            "directory" -> "സേവന ഡയറക്ടറി"
            "search_placeholder" -> "ഉൽപ്പന്നങ്ങളോ സേവനങ്ങളോ തിരയുക..."
            "sign_out" -> "ലോഗ് ഔട്ട്"
            "outstanding_due" -> "വൈദ്യുതി ബിൽ കുടിശ്ശിക:"
            else -> key
        }
        else -> when (key) { // English defaults
            "market" -> "Market"
            "subscriptions" -> "Subscriptions"
            "payments" -> "Payments"
            "tracking" -> "Tracking"
            "hub_title" -> "Emergency & Utility Hub"
            "hub_subtitle" -> "All critical services resolved instantly"
            "sos" -> "SOS DIAL"
            "upi_pay" -> "UPI PAY"
            "recharge" -> "RECHARGE"
            "cooking_gas" -> "COOKING GAS"
            "power_bill" -> "POWER BILL"
            "flights" -> "FLIGHTS"
            "bus_booking" -> "BUS BOOKING"
            "taxicab" -> "TAXICAB"
            "directory" -> "Service Directory"
            "search_placeholder" -> "Search products or services..."
            "sign_out" -> "Sign Out"
            "outstanding_due" -> "Outstanding Electric Bill Due:"
            else -> key
        }
    }
}

