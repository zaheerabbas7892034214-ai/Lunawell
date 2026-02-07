package com.zaheer.lunawell.presentation.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockScreen(
    navController: NavController,
    viewModel: AppLockViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App Lock",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isSetupMode) {
                    // Setup Mode
                    SetupModeContent(
                        uiState = uiState,
                        onPinChange = { viewModel.updatePinInput(it) },
                        onConfirmPinChange = { viewModel.updateConfirmPinInput(it) },
                        onProceed = { viewModel.proceedToConfirmPin() },
                        onGoBack = { viewModel.goBackToEnterPin() },
                        onSave = { viewModel.savePin() }
                    )
                } else {
                    // Settings Mode
                    SettingsModeContent(
                        uiState = uiState,
                        onToggleAppLock = { viewModel.toggleAppLock() },
                        onClearPin = { viewModel.clearPin() },
                        onPinChange = { viewModel.updatePinInput(it) },
                        onVerify = { viewModel.verifyPin { navController.popBackStack() } },
                        onBiometricAuth = {
                            activity?.let {
                                viewModel.authenticateWithBiometric(it) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    )
                }
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
        }
    }
}

@Composable
private fun SetupModeContent(
    uiState: AppLockUiState,
    onPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit,
    onProceed: () -> Unit,
    onGoBack: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = when (uiState.step) {
                SetupStep.ENTER_PIN -> "Set Your PIN"
                SetupStep.CONFIRM_PIN -> "Confirm Your PIN"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (uiState.step) {
                SetupStep.ENTER_PIN -> "Enter a 4-6 digit PIN"
                SetupStep.CONFIRM_PIN -> "Re-enter your PIN to confirm"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // PIN Display
        when (uiState.step) {
            SetupStep.ENTER_PIN -> {
                PinDisplay(uiState.pinInput)
                Spacer(modifier = Modifier.height(24.dp))
                PinKeypad(
                    onNumberClick = { num -> onPinChange(uiState.pinInput + num) },
                    onBackspace = {
                        if (uiState.pinInput.isNotEmpty()) {
                            onPinChange(uiState.pinInput.dropLast(1))
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onProceed,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.pinInput.length >= 4
                ) {
                    Text("Continue")
                }
            }
            SetupStep.CONFIRM_PIN -> {
                PinDisplay(uiState.confirmPinInput)
                Spacer(modifier = Modifier.height(24.dp))
                PinKeypad(
                    onNumberClick = { num -> onConfirmPinChange(uiState.confirmPinInput + num) },
                    onBackspace = {
                        if (uiState.confirmPinInput.isNotEmpty()) {
                            onConfirmPinChange(uiState.confirmPinInput.dropLast(1))
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onGoBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = uiState.confirmPinInput.length >= 4
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsModeContent(
    uiState: AppLockUiState,
    onToggleAppLock: () -> Unit,
    onClearPin: () -> Unit,
    onPinChange: (String) -> Unit,
    onVerify: () -> Unit,
    onBiometricAuth: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Enable/Disable Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable App Lock",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Require PIN or biometric to open app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isAppLockEnabled,
                    onCheckedChange = { onToggleAppLock() }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clear PIN Button
        OutlinedButton(
            onClick = { showClearDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear PIN")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Divider()

        Spacer(modifier = Modifier.height(32.dp))

        // Test Lock Section
        Text(
            text = "Test Your Lock",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enter PIN to Unlock",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        PinDisplay(uiState.pinInput)

        Spacer(modifier = Modifier.height(24.dp))

        PinKeypad(
            onNumberClick = { num -> onPinChange(uiState.pinInput + num) },
            onBackspace = {
                if (uiState.pinInput.isNotEmpty()) {
                    onPinChange(uiState.pinInput.dropLast(1))
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.pinInput.length >= 4
        ) {
            Text("Verify PIN")
        }

        if (uiState.isBiometricAvailable) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onBiometricAuth) {
                Icon(Icons.Default.Fingerprint, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Use Biometric")
            }
        }
    }

    // Clear PIN Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Clear PIN?") },
            text = { Text("This will remove your PIN and disable app lock. You can set a new PIN anytime.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDialog = false
                        onClearPin()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PinDisplay(pin: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        repeat(6) { index ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (index < pin.length) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .background(
                        if (index < pin.length) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (index < pin.length) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PinKeypad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KeypadButton("1", Modifier.weight(1f)) { onNumberClick("1") }
            KeypadButton("2", Modifier.weight(1f)) { onNumberClick("2") }
            KeypadButton("3", Modifier.weight(1f)) { onNumberClick("3") }
        }
        // Row 2: 4, 5, 6
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KeypadButton("4", Modifier.weight(1f)) { onNumberClick("4") }
            KeypadButton("5", Modifier.weight(1f)) { onNumberClick("5") }
            KeypadButton("6", Modifier.weight(1f)) { onNumberClick("6") }
        }
        // Row 3: 7, 8, 9
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KeypadButton("7", Modifier.weight(1f)) { onNumberClick("7") }
            KeypadButton("8", Modifier.weight(1f)) { onNumberClick("8") }
            KeypadButton("9", Modifier.weight(1f)) { onNumberClick("9") }
        }
        // Row 4: Empty, 0, Backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            KeypadButton("0", Modifier.weight(1f)) { onNumberClick("0") }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onBackspace),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
    }
}
