package com.rodrigos01.aiaudiobook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigos01.aiaudiobook.data.Title
import com.rodrigos01.aiaudiobook.theme.AccentGreen
import com.rodrigos01.aiaudiobook.theme.BorderColor
import com.rodrigos01.aiaudiobook.theme.DarkSurface
import com.rodrigos01.aiaudiobook.theme.Indigo500
import com.rodrigos01.aiaudiobook.theme.TextPrimary
import com.rodrigos01.aiaudiobook.theme.TextSecondary
import com.rodrigos01.aiaudiobook.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleBottomSheet(
    editingTitle: Title?,
    onDismiss: () -> Unit,
    onSubmit: (name: String, aiCastingEnabled: Boolean, ttsTier: String) -> Unit,
    isSubmitting: Boolean,
    errorMessage: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val isEditMode = editingTitle != null

    var name by remember(editingTitle) { mutableStateOf(editingTitle?.name ?: "") }
    var aiCastingEnabled by remember(editingTitle) {
        mutableStateOf(editingTitle?.ai_casting_enabled ?: false)
    }
    var ttsTier by remember(editingTitle) {
        mutableStateOf(editingTitle?.tts_tier ?: "basic")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditMode) "Edit Title" else "Create New Title",
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (!errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = Typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Title Name", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo500,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // AI Casting switch (only editable during creation)
            if (!isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Casting",
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Automatically cast voices for characters using Gemini AI",
                            style = Typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = aiCastingEnabled,
                        onCheckedChange = { aiCastingEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentGreen,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BorderColor
                        )
                    )
                }
            }

            // Voice quality tier (only editable during creation)
            if (!isEditMode) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Voice Quality",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = ttsTier == "basic",
                            onClick = { ttsTier = "basic" },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Indigo500,
                                activeContentColor = Color.White,
                                inactiveContentColor = TextSecondary
                            )
                        ) {
                            Text("Basic")
                        }
                        SegmentedButton(
                            selected = ttsTier == "pro",
                            onClick = { ttsTier = "pro" },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Indigo500,
                                activeContentColor = Color.White,
                                inactiveContentColor = TextSecondary
                            )
                        ) {
                            Text("Pro")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && !isSubmitting) {
                        onSubmit(name.trim(), aiCastingEnabled, ttsTier)
                    }
                },
                enabled = name.isNotBlank() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Indigo500,
                    disabledContainerColor = Indigo500.copy(alpha = 0.5f)
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Save Changes" else "Create Title",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
