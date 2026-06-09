package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.AsrRepository
import com.example.ui.screens.CustomerHubScreen
import com.example.ui.screens.ManagementHubScreen
import com.example.ui.screens.RiderDashboardScreen
import com.example.ui.screens.VendorDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AsrViewModel
import com.example.ui.viewmodel.AsrViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite local storage
        val database = AppDatabase.getDatabase(this)
        val repository = AsrRepository(database.asrDao())

        // Provision VM via factory so state is retained across rotated configuration boundaries
        val appViewModel: AsrViewModel = ViewModelProvider(
            this,
            AsrViewModelFactory(application, repository)
        )[AsrViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = appViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: AsrViewModel) {
    val activeRole by viewModel.activeRole.collectAsState()
    val systemAlert by viewModel.systemAlert.collectAsState()

    // Slide and fade alert toasts locally
    LaunchedEffect(systemAlert) {
        if (systemAlert != null) {
            delay(4000)
            viewModel.clearSystemAlert()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ASR",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "All Services Received",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_navigation"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Navigation items corresponding to our role portals
                NavigationBarItem(
                    selected = activeRole == "CUSTOMER",
                    onClick = { viewModel.setRole("CUSTOMER") },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Customer") },
                    label = { Text("Customer") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_customer_tab")
                )

                NavigationBarItem(
                    selected = activeRole == "VENDOR",
                    onClick = { viewModel.setRole("VENDOR") },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Vendor") },
                    label = { Text("Vendor") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_vendor_tab")
                )

                NavigationBarItem(
                    selected = activeRole == "RIDER",
                    onClick = { viewModel.setRole("RIDER") },
                    icon = { Icon(Icons.Default.DeliveryDining, contentDescription = "Rider") },
                    label = { Text("Rider") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_rider_tab")
                )

                NavigationBarItem(
                    selected = activeRole == "ADMIN",
                    onClick = { viewModel.setRole("ADMIN") },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Admin") },
                    label = { Text("Admin") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_admin_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Display corresponding portal state based on state selector
            Crossfade(targetState = activeRole, label = "role_crossfade") { role ->
                when (role) {
                    "CUSTOMER" -> CustomerHubScreen(viewModel = viewModel)
                    "VENDOR" -> VendorDashboardScreen(viewModel = viewModel)
                    "RIDER" -> RiderDashboardScreen(viewModel = viewModel)
                    "ADMIN" -> ManagementHubScreen(viewModel = viewModel)
                }
            }

            // High priority floating HUD toast/alerts notify
            AnimatedVisibility(
                visible = systemAlert != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = systemAlert ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearSystemAlert() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "CloseAlert",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
