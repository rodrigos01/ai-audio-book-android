package com.rodrigos01.aiaudiobook.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigos01.aiaudiobook.theme.BorderColor
import com.rodrigos01.aiaudiobook.theme.DarkSurface
import com.rodrigos01.aiaudiobook.theme.Indigo500
import com.rodrigos01.aiaudiobook.theme.TextPrimary
import com.rodrigos01.aiaudiobook.theme.TextSecondary
import com.rodrigos01.aiaudiobook.theme.Typography
import com.rodrigos01.aiaudiobook.theme.Violet500

enum class ImportSource {
    FILE,
    GOOGLE_DRIVE
}

@Composable
fun ImportSourceDialog(
    onDismiss: () -> Unit,
    onSelectSource: (ImportSource) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import Chapter Content",
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select a source to import your chapter text:",
                    style = Typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                ImportOptionCard(
                    title = "File (.txt)",
                    description = "Import plain text file from your device",
                    icon = Icons.Default.Description,
                    onClick = {
                        onSelectSource(ImportSource.FILE)
                        onDismiss()
                    }
                )

                ImportOptionCard(
                    title = "Google Drive",
                    description = "Import directly using Google Drive picker",
                    icon = Icons.Default.CloudDownload,
                    onClick = {
                        onSelectSource(ImportSource.GOOGLE_DRIVE)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ImportOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Indigo500,
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = Typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
