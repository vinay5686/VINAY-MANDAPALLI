package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeliveryOrder
import com.example.data.RiderProfile
import com.example.ui.viewmodel.AsrViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderDashboardScreen(
    viewModel: AsrViewModel,
    modifier: Modifier = Modifier
) {
    val riders by viewModel.riders.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val activeRiderId by viewModel.activeRiderId.collectAsState()
    val isRiderLoggedIn by viewModel.isRiderLoggedIn.collectAsState()
    val riderLoginError by viewModel.riderLoginError.collectAsState()

    val currentRider = riders.find { it.id == activeRiderId } ?: riders.firstOrNull() ?: RiderProfile(id = 1, name = "Rahul Sharma", status = "Available", phone = "9876543210")

    var selectedSectionTab by remember { mutableStateOf("Available") } // "Available" or "My Active"

    if (!isRiderLoggedIn) {
        RiderLoginView(
            riders = riders,
            loginError = riderLoginError,
            onLogin = { id ->
                viewModel.loginRider(id)
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Rider Profile Header panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Rider Operations Portal [Rider ID: ${currentRider.id}]",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = currentRider.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        // Duty Status indicator dot
                        val statusColor by animateColorAsState(
                            targetValue = when (currentRider.status) {
                                "Available" -> Color.Green
                                "Delivering" -> Color.Yellow
                                else -> Color.Gray
                            }
                        )

                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentRider.status,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RiderMetricLabel(label = "Tally Delivered", value = "${currentRider.totalDeliveries}")
                        RiderMetricLabel(label = "Reputation rating", value = "★ ${currentRider.rating}")
                        RiderMetricLabel(label = "Hotline Contact", value = currentRider.phone)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.logoutRider() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("logout_rider_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("End Rider Shift & Sign Out", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

        // Live stats & tabs toggles
        val pendingPool = orders.filter { it.status == "Pending" }
        val myActiveDeliveries = orders.filter { it.riderId == currentRider.id && it.status != "Delivered" }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            listOf("Available", "Active").forEach { sec ->
                val active = sec == selectedSectionTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedSectionTab = sec }
                        .background(
                            color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                        .testTag("rider_tab_$sec"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (sec == "Available") "Orders Pool (${pendingPool.size})" else "Active Dispatches (${myActiveDeliveries.size})",
                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display list based on active tab
        if (selectedSectionTab == "Available") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Broadcast Pool of Orders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Auto-refreshed",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            if (pendingPool.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No pending orders in broadcast pool.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Log on to 'Customer' profile, add products to basket and request a delivery setup.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pendingPool) { order ->
                        RiderPoolOrderCard(
                            order = order,
                            onAccept = { viewModel.acceptAndPickupOrder(order, currentRider) }
                        )
                    }
                }
            }
        } else {
            // My Active Deliveries list
            Text(
                text = "Orders Assigned To You",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (myActiveDeliveries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Task,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active dispatches under your profile.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Go to 'Orders Pool' tab to claim and pick up pending deliveries.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
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
                    items(myActiveDeliveries) { order ->
                        RiderActiveOrderCard(
                            order = order,
                            onComplete = { viewModel.completeDelivery(order, currentRider) },
                            onLocationUpdate = { lat, lng, msg ->
                                viewModel.modifyRiderLocation(order.id, lat, lng, msg)
                            }
                        )
                    }
                }
            }
        }
    }
  }
}

@Composable
fun RiderMetricLabel(label: String, value: String) {
    Column {
        Text(label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.75f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// Order displayed in collective pool
@Composable
fun RiderPoolOrderCard(
    order: DeliveryOrder,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("rider_pool_order_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("BROADCAST ROUTE ORDER #${order.id}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Store: ${order.vendorName} (${order.category})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("CLAIMABLE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pick Location: ${order.vendorName} Merchant Outlet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Deliver Location: ${order.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total payout volume:", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                    Text("₹${String.format("%.2f", order.totalAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("rider_accept_button_${order.id}")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept & Pick Up", fontSize = 12.sp)
                }
            }
        }
    }
}

// Active dispatch detail card for Rider
@Composable
fun RiderActiveOrderCard(
    order: DeliveryOrder,
    onComplete: () -> Unit,
    onLocationUpdate: (Double, Double, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ACTIVE SHIPMENT #${order.id}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary)
                
                // payment method confirmation visual tag
                Box(
                    modifier = Modifier
                        .background(
                            color = if (order.paymentMethod == "Cash on Delivery") Color.Yellow.copy(alpha = 0.23f) 
                                    else Color.Green.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (order.paymentMethod == "Cash on Delivery") "COLLECT CASH: ₹${String.format("%.2f", order.totalAmount)}" 
                               else "PREPAID ONLINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (order.paymentMethod == "Cash on Delivery") Color(0xFFD97706) else Color(0xFF047857)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Items to deliver: ${order.itemsSummary}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("RECIPIENT CONTACT CHECKS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Text("Name: ${order.customerName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Spot: ${order.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation map graphic simulator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Routing Navigation Active: Delivering to ${order.customerName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "DISPATCH GPS TRACKER PORTAL", 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text("Tap a leg to simulate sending live GPS location broadcasts:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val stepsList = listOf(
                        Triple(13.0827, 80.2707, "At Vendor Store"),
                        Triple(13.0845, 80.2750, "In Transit (Near Highway)"),
                        Triple(13.0872, 80.2818, "Arrived at Layout"),
                        Triple(13.0890, 80.2858, "Ringing Doorbell")
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        stepsList.forEachIndexed { sIdx, step ->
                            val isSelected = order.riderLocationStatus == step.third
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { onLocationUpdate(step.first, step.second, step.third) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when (sIdx) {
                                            0 -> Icons.Default.Storefront
                                            1 -> Icons.Default.DirectionsBike
                                            2 -> Icons.Default.LocationOn
                                            else -> Icons.Default.Home
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Leg ${sIdx + 1}",
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    if (order.riderLocationStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Broadcasting: (${order.riderLatitude}, ${order.riderLongitude}) • ${order.riderLocationStatus}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth().testTag("rider_complete_button_${order.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.TaskAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (order.paymentMethod == "Cash on Delivery") "Confirm Payment Received & Complete" 
                           else "Confirm Order Handover & Complete",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderLoginView(
    riders: List<RiderProfile>,
    loginError: String?,
    onLogin: (String) -> Unit
) {
    var idInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var userOtpInput by remember { mutableStateOf("") }
    var loginMethod by remember { mutableStateOf("Phone") } // "Phone" or "Email"
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

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
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "ASR Rider Fleet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Log in with your official Registered Rider Identification Number to access delivery routing and complete order pickups.",
                fontSize = 12.sp,
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
                        text = if (!otpSent) "Rider Credentials" else "Enter Security OTP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!otpSent) {
                        OutlinedTextField(
                            value = idInput,
                            onValueChange = { idInput = it },
                            label = { Text("Enter Rider ID Number") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            placeholder = { Text("e.g. 1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("rider_login_id_input")
                        )

                        // Choice of OTP Method
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
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Mobile Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                prefix = { Text("+91 ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("rider_login_phone_input")
                            )
                        } else {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("E-mail Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("rider_login_email_input")
                            )
                        }

                        // Provide Demo Guide for Easy Evaluation
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Registered Rider Roster List:", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (riders.isEmpty()) {
                                    Text("Rahul Sharma [ID: 1]\nAmit Patel [ID: 2]\nVikram Singh [ID: 3]", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                } else {
                                    riders.forEach { profile ->
                                        Text(
                                            text = "• ${profile.name} (Rider ID: ${profile.id})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // OTP is sent
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📟 SIMULATED RIDER TRANSMISSION GATEWAY:",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "[OTP SENT] Secure Rider PIN: $generatedOtp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Dispatched " + (if (loginMethod == "Phone") "via SMS to +91 $phoneInput" else "via Secure Mail to $emailInput"),
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
                            modifier = Modifier.fillMaxWidth().testTag("rider_login_otp_input")
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

                    val displayError = localError ?: loginError
                    if (displayError != null) {
                        Text(
                            text = displayError,
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (!otpSent) {
                                if (idInput.isBlank()) {
                                    localError = "Please enter your Rider ID Number"
                                } else if (loginMethod == "Phone" && (phoneInput.length != 10 || phoneInput.any { !it.isDigit() })) {
                                    localError = "Please enter a valid 10-digit mobile number"
                                } else if (loginMethod == "Email" && (!emailInput.contains("@") || !emailInput.contains("."))) {
                                    localError = "Please enter a valid Email Address"
                                } else {
                                    localError = null
                                    generatedOtp = (1000..9999).random().toString()
                                    otpSent = true
                                }
                            } else {
                                if (userOtpInput == generatedOtp) {
                                    localError = null
                                    onLogin(idInput)
                                } else {
                                    localError = "Invalid rider login verification code. Please input exact PIN shown above."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("rider_login_submit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (!otpSent) "Request Secure Rider OTP" else "Validate OTP & Connect",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
