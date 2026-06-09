package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeliveryOrder
import com.example.data.VendorItem
import com.example.ui.viewmodel.AsrViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    viewModel: AsrViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.vendorItems.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val activeVendor by viewModel.activeVendorId.collectAsState()
    val isVendorLoggedIn by viewModel.isVendorLoggedIn.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var selectedStatTab by remember { mutableStateOf("Catalog") } // "Catalog" or "Orders"

    // Set of distinct vendors available in current catalogue for easy toggle simulator
    val registeredVendors = remember(items) {
        (items.map { it.vendorName } + listOf("Taj Delicacy", "Green Basket Co.", "Perfect Pixel Studio")).distinct()
    }

    if (!isVendorLoggedIn) {
        VendorLoginView(
            registeredVendors = registeredVendors,
            onLogin = { name ->
                viewModel.loginVendor(name)
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Vendor Login Selector bar (Simulator)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Merchant Session",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeVendor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Merchant Administration Portal ID",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        TextButton(
                            onClick = { viewModel.logoutVendor() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                            modifier = Modifier.testTag("logout_vendor_button")
                        ) {
                            Text("Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

        // Summary Statistics Row
        val myCatalogEntries = items.filter { it.vendorName.equals(activeVendor, ignoreCase = true) }
        val myOrders = orders.filter { it.vendorName.equals(activeVendor, ignoreCase = true) }
        val myPendingOrders = myOrders.filter { it.status != "Delivered" }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VendorStatCard(
                title = "Total Catalog Listings",
                value = "${myCatalogEntries.size}",
                icon = Icons.Default.Inventory,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            VendorStatCard(
                title = "Pending Order Deliveries",
                value = "${myPendingOrders.size}",
                icon = Icons.Default.PendingActions,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tab Panel inside Dashboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            listOf("Catalog", "Orders", "Subscriptions").forEach { tab ->
                val active = tab == selectedStatTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedStatTab = tab }
                        .background(
                            color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                        .testTag("vendor_tab_$tab"),
                    contentAlignment = Alignment.Center
                ) {
                    val labelText = when (tab) {
                        "Catalog" -> "Inventory (${myCatalogEntries.size})"
                        "Orders" -> "Orders (${myOrders.size})"
                        else -> "Subscribers"
                    }
                    Text(
                        text = labelText,
                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Switch List Content
        if (selectedStatTab == "Catalog") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items listed under $activeVendor",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier.testTag("vendor_add_item_fab"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (myCatalogEntries.isEmpty()) {
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
                            imageVector = Icons.Default.AddBox,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No catalog articles listed for this store.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Click 'Add Item' above to fill database values for price, quantity, category, and items.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
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
                    items(myCatalogEntries) { item ->
                        VendorListItemCard(
                            item = item,
                            onDelete = { viewModel.deleteCatalogItem(item) }
                        )
                    }
                }
            }
        } else if (selectedStatTab == "Orders") {
            // Customer Orders matched to this Store
            Text(
                text = "Orders allocated to $activeVendor",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            if (myOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active customer orders.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "When customers place orders from your catalog, they instantly pop up here.",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(myOrders) { order ->
                        VendorOrderCard(order = order)
                    }
                }
            }
        } else {
            // Subscriptions list!
            val allSubs by viewModel.subscriptions.collectAsState()
            val mySubs = allSubs.filter { it.vendorName.equals(activeVendor, ignoreCase = true) }

            Text(
                text = "Active Multi-Day Subscriptions",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            if (mySubs.isEmpty()) {
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
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active customer subscriptions.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Customers can subscribe to your custom plans using Customer Hub. Subscriptions will list here.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
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
                    items(mySubs) { sub ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Subscriber: ${sub.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Product/Plan: ${sub.packageName}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Subscribed: ${sub.frequency} • Value: ₹${sub.price}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (sub.status == "Active") Color.Green.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = sub.status.uppercase(),
                                        color = if (sub.status == "Active") Color(0xFF047857) else Color.Red,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to Add a Catalog Item ("price item quantity and everything")
    if (showAddItemDialog) {
        AddCatalogItemDialog(
            activeVendor = activeVendor,
            onDismiss = { showAddItemDialog = false },
            onSubmit = { title, category, price, quantity, stock, description, isOnline ->
                viewModel.addCatalogItem(
                    vendorName = activeVendor,
                    category = category,
                    title = title,
                    price = price,
                    qty = quantity,
                    stock = stock,
                    desc = description,
                    isOnline = isOnline
                )
                showAddItemDialog = false
            }
        )
    }
  }
}

// Vendor Stat visual card helper
@Composable
fun VendorStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// Vendor Catalogue Listing Card
@Composable
fun VendorListItemCard(
    item: VendorItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryVector(item.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (item.isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline.copy(0.12f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.isOnline) "ONLINE SHIPPING" else "IN-PERSON ONLY",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Text("Price: $$${item.price}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Qty: ${item.quantityString}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("Stock: ${item.inventory}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

// Vendor Incoming Order match detail card
@Composable
fun VendorOrderCard(order: DeliveryOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Order Record ID #${order.id}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Date: Just now", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = when (order.status) {
                                "Pending" -> Color.Yellow.copy(alpha = 0.2f)
                                "Picked Up" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> Color.Green.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = when (order.status) {
                            "Pending" -> Color(0xFFD97706)
                            "Picked Up" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> Color(0xFF047857)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Summary: ${order.itemsSummary}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Total billed: ₹${String.format("%.2f", order.totalAmount)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Settlement: ${order.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Divider()
            Spacer(modifier = Modifier.height(6.dp))
            Text("Deliver To: ${order.customerName}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Recipient Spot: ${order.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            
            if (order.riderName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeliveryDining, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Allocated Rider: ${order.riderName}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// Master dialog of creating a new service item
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCatalogItemDialog(
    activeVendor: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, category: String, price: Double, quantity: String, stock: Int, description: String, isOnline: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Restaurants") }
    var priceString by remember { mutableStateOf("") }
    var quantityString by remember { mutableStateOf("1 unit") }
    var stockString by remember { mutableStateOf("50") }
    var description by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(true) }

    val categories = listOf(
        "Restaurants", "Hotels", "Groceries", "Vegetables", 
        "Fruits", "Flowers", "Event Management", "Wedding Planner", 
        "Photo Studio", "Fashion Design", "Mobile Accessories"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List Service in Database", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Registering under Merchant ID: $activeVendor", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Product/Service Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_item_title_input")
                    )
                }

                item {
                    Text("Category Sector", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    DropdownCategorySelector(
                        categories = categories,
                        selected = selectedCategory,
                        onSelect = { selectedCategory = it }
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceString,
                            onValueChange = { priceString = it },
                            label = { Text("Price (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("add_item_price_input")
                        )
                        OutlinedTextField(
                            value = quantityString,
                            onValueChange = { quantityString = it },
                            label = { Text("Unit (e.g. 1 kg)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("add_item_unit_input")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = stockString,
                        onValueChange = { stockString = it },
                        label = { Text("Inventory Count Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { isOnline = !isOnline },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isOnline, onCheckedChange = { isOnline = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Deploy Online Delivery Pipeline", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Enables customers to cart and dispatch to local riders.", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && priceString.isNotEmpty()) {
                        onSubmit(
                            title,
                            selectedCategory,
                            priceString.toDoubleOrNull() ?: 1.0,
                            quantityString,
                            stockString.toIntOrNull() ?: 10,
                            description,
                            isOnline
                        )
                    }
                },
                modifier = Modifier.testTag("add_item_submit_button")
            ) {
                Text("Deploy to Live Feed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DropdownCategorySelector(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selected, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat) },
                    onClick = {
                        onSelect(cat)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorLoginView(
    registeredVendors: List<String>,
    onLogin: (String) -> Unit
) {
    var selectedMerchant by remember { mutableStateOf(registeredVendors.firstOrNull() ?: "Taj Delicacy") }
    var pinCode by remember { mutableStateOf("") }
    var gstAccepted by remember { mutableStateOf(false) }
    var isNewMerchant by remember { mutableStateOf(false) }
    var customMerchantName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var otpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var userOtpInput by remember { mutableStateOf("") }
    var loginMethod by remember { mutableStateOf("Phone") } // "Phone" or "Email"
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }

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
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "ASR Merchant Desk",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Authorized merchant portal to manage digital catalogues, process customer orders, and modify pricing lists.",
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
                        text = if (!otpSent) "Establish Merchant Session" else "Enter Security OTP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!otpSent) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isNewMerchant) "Register Custom Estab." else "Select Registered Establish.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            TextButton(
                                onClick = { isNewMerchant = !isNewMerchant },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (isNewMerchant) "Switch to Seeded" else "Add New Shop",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isNewMerchant) {
                            OutlinedTextField(
                                value = customMerchantName,
                                onValueChange = { customMerchantName = it },
                                label = { Text("Shop / Enterprise Name") },
                                placeholder = { Text("e.g. Hyderabad Biryani House") },
                                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("vendor_login_new_name_input")
                            )
                        } else {
                            Text("Choose establishment to log in:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 140.dp)
                            ) {
                                LazyColumn(reverseLayout = false) {
                                    items(registeredVendors) { vendor ->
                                        val isSel = vendor == selectedMerchant
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSel) MaterialTheme.colorScheme.primaryContainer 
                                                    else Color.Transparent
                                                )
                                                .clickable { selectedMerchant = vendor }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Storefront, 
                                                contentDescription = null, 
                                                modifier = Modifier.size(16.dp), 
                                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = vendor, 
                                                fontSize = 12.sp, 
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it },
                            label = { Text("Merchant Login PIN (4-Digit)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            placeholder = { Text("e.g. 1234") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("vendor_login_pin_input")
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
                                modifier = Modifier.fillMaxWidth().testTag("vendor_login_phone_input")
                            )
                        } else {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("E-mail Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("vendor_login_email_input")
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { gstAccepted = !gstAccepted }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = gstAccepted,
                                onCheckedChange = { gstAccepted = it },
                                modifier = Modifier.testTag("vendor_login_terms_checkbox")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "I confirm that this establishment has valid commercial clearance and compliant GST registrations within India tax limits.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        // OTP sent
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📟 SIMULATED MERCHANT GATEWAY CARRIER:",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "[OTP SENT] Secure Merchant code: $generatedOtp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Dispatched " + (if (loginMethod == "Phone") "via SMS to +91 $phoneInput" else "via Mail to $emailInput"),
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
                            modifier = Modifier.fillMaxWidth().testTag("vendor_login_otp_input")
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
                            val merchantName = if (isNewMerchant) customMerchantName else selectedMerchant
                            if (!otpSent) {
                                if (merchantName.isBlank()) {
                                    errorMessage = "Please enter or select a Shop/Enterprise Name"
                                    showError = true
                                } else if (pinCode.length != 4 || pinCode.any { !it.isDigit() }) {
                                    errorMessage = "Merchant Passcode must be a 4-digit PIN (e.g., 1234)"
                                    showError = true
                                } else if (loginMethod == "Phone" && (phoneInput.length != 10 || phoneInput.any { !it.isDigit() })) {
                                    errorMessage = "Please enter a valid 10-digit mobile number"
                                    showError = true
                                } else if (loginMethod == "Email" && (!emailInput.contains("@") || !emailInput.contains("."))) {
                                    errorMessage = "Please enter a valid Email Address"
                                    showError = true
                                } else if (!gstAccepted) {
                                    errorMessage = "Commercial MCA/GST declarations must be accepted"
                                    showError = true
                                } else {
                                    showError = false
                                    generatedOtp = (1000..9999).random().toString()
                                    otpSent = true
                                }
                            } else {
                                if (userOtpInput == generatedOtp) {
                                    showError = false
                                    onLogin(merchantName)
                                } else {
                                    errorMessage = "Invalid login OTP code. Please enter exact PIN shown above."
                                    showError = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("vendor_login_submit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (!otpSent) "Request Merchant OTP" else "Validate OTP & Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
