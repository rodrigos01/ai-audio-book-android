package com.rodrigos01.aiaudiobook.ui.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.ui.viewmodel.AuthUiState
import com.rodrigos01.aiaudiobook.ui.viewmodel.AuthViewModel

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

    LoginScreen(
        uiState = uiState,
        onLogin = { email, password -> authViewModel.login(email, password) },
        onRegister = { email, password -> authViewModel.register(email, password) },
        onGoogleSignIn = { authViewModel.loginWithGoogle(context) },
        onClearError = { authViewModel.clearError() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    // Handlers for validation
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun validateInputs(): Boolean {
        var isValid = true
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Please enter a valid email address"
            isValid = false
        } else {
            emailError = null
        }

        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }
        return isValid
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background gradient circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-50).dp)
                .background(Brush.radialGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(Brush.radialGradient(colors = listOf(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f), Color.Transparent)))
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "AI Audio Book",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                Text(
                    text = if (isRegisterMode) "Create an account to begin" else "Sign in to access your titles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Error Message
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Email Address", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    isError = emailError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                emailError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
                    )
                }

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) passwordError = null
                    },
                    label = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    isError = passwordError != null,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                passwordError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
                    )
                }

                // Submit Button
                Button(
                    onClick = {
                        if (validateInputs()) {
                            if (isRegisterMode) {
                                onRegister(email, password)
                            } else {
                                onLogin(email, password)
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isRegisterMode) "Register" else "Sign In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Google Sign-In Button
                OutlinedButton(
                    onClick = {
                        onGoogleSignIn()
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Styled Letter 'G' in Google colors
                        Text(
                            text = "G ",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Toggle Mode
                TextButton(
                    onClick = {
                        isRegisterMode = !isRegisterMode
                        onClearError()
                    }
                ) {
                    Text(
                        text = if (isRegisterMode) "Already have an account? Sign In" else "Don't have an account? Register",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun LoginScreenPreview() {
    AIAudioBookTheme {
        LoginScreen(
            uiState = AuthUiState.Unauthenticated,
            onLogin = { _, _ -> },
            onRegister = { _, _ -> },
            onGoogleSignIn = {},
            onClearError = {}
        )
    }
}

@PreviewLightDark
@Composable
fun LoginScreenLoadingPreview() {
    AIAudioBookTheme {
        LoginScreen(
            uiState = AuthUiState.Loading,
            onLogin = { _, _ -> },
            onRegister = { _, _ -> },
            onGoogleSignIn = {},
            onClearError = {}
        )
    }
}

@PreviewLightDark
@Composable
fun LoginScreenErrorPreview() {
    AIAudioBookTheme {
        LoginScreen(
            uiState = AuthUiState.Error("Invalid email or password"),
            onLogin = { _, _ -> },
            onRegister = { _, _ -> },
            onGoogleSignIn = {},
            onClearError = {}
        )
    }
}
