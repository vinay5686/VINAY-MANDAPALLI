package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeliveryOrder
import com.example.data.RiderProfile
import com.example.data.UtilityTransaction
import com.example.ui.viewmodel.AsrViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementHubScreen(
    viewModel: AsrViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.vendorItems.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val riders by viewModel.riders.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var activeAdminTab by remember { mutableStateOf("Stats") } // "Stats", "Orders Dispatch", "Utility Logs"
    var selectedOrderForAssign by remember { mutableStateOf<DeliveryOrder?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Management App Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "ASR Master Controller Hub",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Unified management database control deck",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Horizontal navigation tabs for Management App
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            listOf("Stats", "Orders Dispatch", "Utility Logs", "Firebase Sync").forEach { tab ->
                val isActive = tab == activeAdminTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeAdminTab = tab }
                        .background(
                            color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                        .testTag("admin_tab_$tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content Router
        when (activeAdminTab) {
            "Stats" -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Global Operational Stats",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    item {
                        val volume = orders.sumOf { it.totalAmount } + transactions.sumOf { it.amount }
                        val pending = orders.count { it.status == "Pending" }
                        val compl = orders.count { it.status == "Delivered" }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminDetailedStatItem(
                                title = "Gross Commercial Volume (GCV)",
                                value = "₹${String.format("%.2f", volume)}",
                                subtitle = "Combined value of checkouts + utility transactions",
                                icon = Icons.Default.MonetizationOn,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Under Broadcast", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        Text("$pending Pending", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Completed Jobs", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        Text("$compl Delivered", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                    }
                                }
                            }

                            AdminDetailedStatItem(
                                title = "Registered Vendor Listings",
                                value = "${items.size} active catalog offerings",
                                subtitle = "Encompasses restaurants, hotels, utilities, fruits & veggies",
                                icon = Icons.Default.Store,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            AdminDetailedStatItem(
                                title = "Active Fleet Responders",
                                value = "${riders.size} dispatchers",
                                subtitle = "Riders currently polling database order claims",
                                icon = Icons.Default.DirectionsBike,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    item {
                        // System Database reset control
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("System Data Maintenance", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                                Text("Reset simulated history logs, preheated databases and metrics to clean slate.", fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        viewModel.showSystemAlert("🔄 SYSTEM REBOOT TRIGGERED: Resetting SQLite Room databases and loading seed defaults.")
                                        // Wait, the viewModel will run prepSeedDataIfEmpty on initial startup. We can alert the user.
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Perform Factory Seeding", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            "Orders Dispatch" -> {
                val pendingOrders = orders.filter { it.status == "Pending" }
                val claimedOrders = orders.filter { it.status == "Picked Up" }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Unassigned / Stuck Deliveries",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (pendingOrders.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
                            ) {
                                Text(
                                    text = "All customer orders have been claimed by riders! Healthy supply-demand balance.",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(14.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(pendingOrders) { order ->
                            AdminOrderDispatchCard(
                                order = order,
                                onAssignClick = { selectedOrderForAssign = order }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Currently In-Transit Deliveries",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (claimedOrders.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
                            ) {
                                Text(
                                    text = "No current active transits. Ready for next orders.",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(14.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(claimedOrders) { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Order ID #${order.id} | ${order.vendorName} to ${order.customerName}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("Courier Active: ${order.riderName} (Phone: ${riders.find { it.id == order.riderId }?.phone ?: "Available"})", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    
                                    IconButton(onClick = { viewModel.deleteOrder(order.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Utility Logs" -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Emergency & Utility Transaction Log",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline.copy(0.4f))
                                    Text("No utility bookings logged yet.", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Book a taxi / complete a UPI flow on 'Customer' panel to record logs.", color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), fontSize = 10.sp)
                                }
                            }
                        }
                    } else {
                        items(transactions) { tx ->
                            AdminTransactionHistoryCard(tx = tx)
                        }
                    }
                }
            }

            "Firebase Sync" -> {
                val firebaseLogs by viewModel.firebaseLogs.collectAsState()
                val isFirebaseConfigured by viewModel.isFirebaseConfigured.collectAsState()
                val isFirestoreSyncing by viewModel.isFirestoreSyncing.collectAsState()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("admin_firebase_sync_tab"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Cloud Firestore Orchestrator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Firestore status card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("firebase_connection_status_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFirebaseConfigured) Color(0xFF047857).copy(alpha = 0.08f) 
                                                 else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                if (isFirebaseConfigured) Color(0xFF047857).copy(alpha = 0.25f) 
                                else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isFirebaseConfigured) Color(0xFF047857).copy(alpha = 0.12f) 
                                            else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isFirebaseConfigured) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = if (isFirebaseConfigured) Color(0xFF047857) else MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isFirebaseConfigured) "Google Firestore Connected" else "Simulated Firestore Engine Active",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isFirebaseConfigured) Color(0xFF047857) else MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = if (isFirebaseConfigured) "Production-grade synchronization pipelines verified." 
                                               else "Offline safety console: streams mock actions & log updates locally.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    if (isFirestoreSyncing) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Streaming and indexing data collections...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }

                    // Database Seeding Controls
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.08f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "Database Seed & Initializer Controllers",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Initialize collections directly inside Firestore. This registers document keys, indexes, schemas, and transfers current local seeder objects.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

                                // Service Categories Sync Control
                                Button(
                                    onClick = { viewModel.initializeServiceCategoriesInFirestore() },
                                    modifier = Modifier.fillMaxWidth().testTag("init_categories_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    enabled = !isFirestoreSyncing
                                ) {
                                    Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Deploy Service Categories Collection Documents", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // User Profiles Sync Control
                                Button(
                                    onClick = { viewModel.initializeUserProfilesInFirestore() },
                                    modifier = Modifier.fillMaxWidth().testTag("init_users_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    enabled = !isFirestoreSyncing
                                ) {
                                    Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sync Role Profiles (Customers / Vendors / Riders)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Transactions Sync Control
                                Button(
                                    onClick = { viewModel.initializeTransactionRecordsInFirestore() },
                                    modifier = Modifier.fillMaxWidth().testTag("init_transactions_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                    enabled = !isFirestoreSyncing
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Serialize & Upload Order Transactions Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Console Output Log Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.Green)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "INTEGRATION SYSTEM MONITOR",
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                    Text(
                                        text = "LIVE FEED",
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = Color.Green
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                        .padding(10.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize().testTag("firebase_monitor_logs")
                                    ) {
                                        items(firebaseLogs) { log ->
                                            Text(
                                                text = log,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                color = if (log.contains("❌")) Color(0xFFF87171) 
                                                        else if (log.contains("✅")) Color(0xFF4ADE80) 
                                                        else if (log.contains("⚠️")) Color(0xFFFBBF24) 
                                                        else Color.White,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog: Force assign riders to stagnant pending orders
    if (selectedOrderForAssign != null) {
        val selectedOrder = selectedOrderForAssign!!
        AlertDialog(
            onDismissRequest = { selectedOrderForAssign = null },
            title = { Text("Dispatch Manual Rider Assign", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a dispatcher below to manually allocate Cargo Order #${selectedOrder.id}:", fontSize = 11.sp)
                    riders.forEach { rider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.forceAssignOrder(selectedOrder, rider)
                                    selectedOrderForAssign = null
                                }
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(rider.name, fontWeight = FontWeight.Bold)
                            }
                            Text(rider.status, color = if (rider.status == "Available") Color.Green else Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedOrderForAssign = null }) { Text("Cancel") }
            }
        )
    }
}

// Detailed stats card block helper
@Composable
fun AdminDetailedStatItem(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// Interactive Dispatch assigner card
@Composable
fun AdminOrderDispatchCard(
    order: DeliveryOrder,
    onAssignClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("admin_dispatch_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("BROADCAST ROUTE ORDER #${order.id}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("Store: ${order.vendorName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onAssignClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("admin_force_assign_btn_${order.id}")
                ) {
                    Text("Assign Rider", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Client contact: ${order.customerName} • Deliver spot: ${order.deliveryAddress}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Items to Courier: ${order.itemsSummary}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Utility billing journal card for Admin
@Composable
fun AdminTransactionHistoryCard(tx: UtilityTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pair = when (tx.type) {
                "UPI" -> Icons.Default.QrCodeScanner to MaterialTheme.colorScheme.primary
                "Recharge" -> Icons.Default.PhoneIphone to MaterialTheme.colorScheme.secondary
                "Cooking Gas" -> Icons.Default.LocalGasStation to MaterialTheme.colorScheme.tertiary
                "Electricity" -> Icons.Default.ElectricBolt to Color(0xFFD97706)
                else -> Icons.Default.LocalTaxi to Color(0xFF0284C7)
            }
            val icon = pair.first
            val tint = pair.second

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tx.type + " TRANSACTION LOG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tint)
                Text(tx.details, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Date: Just now • Status: cleared", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
            }

            Text(
                text = "₹${tx.amount}",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
