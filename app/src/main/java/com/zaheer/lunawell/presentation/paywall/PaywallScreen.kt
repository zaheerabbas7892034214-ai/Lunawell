package com.zaheer.lunawell.presentation.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    navController: NavController,
    viewModel: PaywallViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Upgrade to LunaWell PRO",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Feature Comparison Table
                FeatureComparisonTable()

                Spacer(modifier = Modifier.height(32.dp))

                // Price Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.priceText,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Unlock all premium features",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Subscribe Button
                Button(
                    onClick = {
                        activity?.let { viewModel.launchBillingFlow(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && uiState.productDetails != null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Subscribe Now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Restore Purchases Button
                TextButton(
                    onClick = { viewModel.restorePurchases() },
                    enabled = !uiState.isLoading
                ) {
                    Text("Restore Purchases")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Terms & Privacy Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { navController.navigate("privacy") }) {
                        Text(
                            text = "Terms of Service",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = " • ",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    TextButton(onClick = { navController.navigate("privacy") }) {
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error/Success Messages
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(error)
                }
            }

            uiState.successMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Text(message)
                }
            }

            // Loading Overlay
            if (uiState.isLoading && uiState.productDetails == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun FeatureComparisonTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Features",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "FREE",
                    modifier = Modifier.width(60.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "PRO",
                    modifier = Modifier.width(60.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Feature Rows
            FeatureRow("Basic tracking", hasFree = true, hasPro = true)
            FeatureRow("Last 2 cycles insights", hasFree = true, hasPro = true)
            FeatureRow("Advanced insights", hasFree = false, hasPro = true)
            FeatureRow("Unlimited history", hasFree = false, hasPro = true)
            FeatureRow("PCOS patterns", hasFree = false, hasPro = true)
            FeatureRow("Custom reminders", hasFree = false, hasPro = true)
            FeatureRow("PDF export", hasFree = false, hasPro = true)
            FeatureRow("Encrypted backup", hasFree = false, hasPro = true)
            FeatureRow("App lock", hasFree = false, hasPro = true)
            FeatureRow("Multi-profile", hasFree = false, hasPro = true)
        }
    }
}

@Composable
private fun FeatureRow(
    feature: String,
    hasFree: Boolean,
    hasPro: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Box(
            modifier = Modifier.width(60.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasFree) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Available",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "—",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        Box(
            modifier = Modifier.width(60.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasPro) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Available",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "—",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
