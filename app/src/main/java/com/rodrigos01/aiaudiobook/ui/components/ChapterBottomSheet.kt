package com.rodrigos01.aiaudiobook.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.rodrigos01.aiaudiobook.common.util.FileUtils
import com.rodrigos01.aiaudiobook.common.util.GoogleAuthHelper
import com.rodrigos01.aiaudiobook.data.Chapter
import com.rodrigos01.aiaudiobook.data.Voice
import com.rodrigos01.aiaudiobook.theme.AIAudioBookTheme
import com.rodrigos01.aiaudiobook.theme.BorderColor
import com.rodrigos01.aiaudiobook.theme.DarkSurface
import com.rodrigos01.aiaudiobook.theme.Indigo500
import com.rodrigos01.aiaudiobook.theme.TextPrimary
import com.rodrigos01.aiaudiobook.theme.TextSecondary
import com.rodrigos01.aiaudiobook.theme.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterBottomSheet(
    editingChapter: Chapter?,
    aiCastingEnabled: Boolean,
    voices: List<Voice>,
    isLoadingVoices: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, content: String, voiceId: String, googleDocId: String?, googleAccessToken: String?) -> Unit,
    isSubmitting: Boolean,
    errorMessage: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val isEditMode = editingChapter != null

    var name by remember(editingChapter) { mutableStateOf(editingChapter?.name ?: "") }
    var content by remember(editingChapter) { mutableStateOf(editingChapter?.content ?: "") }
    var selectedVoiceId by remember(editingChapter, voices) {
        mutableStateOf(
            editingChapter?.voice_id ?: voices.firstOrNull()?.id ?: ""
        )
    }
    var googleDocId by remember { mutableStateOf<String?>(null) }
    var googleAccessToken by remember { mutableStateOf<String?>(null) }
    var attachedDocName by remember { mutableStateOf<String?>(null) }
    var voiceDropdownExpanded by remember { mutableStateOf(false) }

    val requiresVoice = !aiCastingEnabled && !isEditMode

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        ChapterBottomSheetContent(
            isEditMode = isEditMode,
            name = name,
            onNameChange = { name = it },
            content = content,
            onContentChange = {
                content = it
                if (it.isNotBlank()) {
                    googleDocId = null
                    googleAccessToken = null
                    attachedDocName = null
                }
            },
            googleDocId = googleDocId,
            googleAccessToken = googleAccessToken,
            attachedDocName = attachedDocName,
            onGoogleDocAttached = { docId, token, docTitle ->
                googleDocId = docId
                googleAccessToken = token
                attachedDocName = docTitle
                if (name.isBlank() && !docTitle.isNullOrBlank()) {
                    name = docTitle
                }
            },
            onClearAttachedDoc = {
                googleDocId = null
                googleAccessToken = null
                attachedDocName = null
            },
            requiresVoice = requiresVoice,
            isLoadingVoices = isLoadingVoices,
            voices = voices,
            selectedVoiceId = selectedVoiceId,
            onVoiceSelected = { selectedVoiceId = it },
            voiceDropdownExpanded = voiceDropdownExpanded,
            onVoiceDropdownExpandedChange = { voiceDropdownExpanded = it },
            isSubmitting = isSubmitting,
            errorMessage = errorMessage,
            onSubmit = {
                onSubmit(name.trim(), content.trim(), selectedVoiceId, googleDocId, googleAccessToken)
            }
        )
    }
}

@Composable
fun ChapterBottomSheetContent(
    isEditMode: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    googleDocId: String? = null,
    googleAccessToken: String? = null,
    attachedDocName: String? = null,
    onGoogleDocAttached: (docId: String, token: String, title: String?) -> Unit = { _, _, _ -> },
    onClearAttachedDoc: () -> Unit = {},
    requiresVoice: Boolean,
    isLoadingVoices: Boolean,
    voices: List<Voice>,
    selectedVoiceId: String,
    onVoiceSelected: (String) -> Unit,
    voiceDropdownExpanded: Boolean,
    onVoiceDropdownExpandedChange: (Boolean) -> Unit,
    isSubmitting: Boolean,
    errorMessage: String? = null,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showImportDialog by remember { mutableStateOf(false) }
    var isAuthorizing by remember { mutableStateOf(false) }
    var pendingDocId by remember { mutableStateOf<String?>(null) }
    var pendingDocTitle by remember { mutableStateOf<String?>(null) }

    // Launcher for Google OAuth consent dialog
    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val authResult = GoogleAuthHelper.getAuthorizationResultFromIntent(context, result.data)
            val token = authResult?.accessToken
            val title = pendingDocTitle ?: ""
            coroutineScope.launch {
                var docId = pendingDocId
                if (token != null) {
                    if (docId == null && title.isNotBlank()) {
                        docId = GoogleAuthHelper.resolveDriveFileId(title, token)
                    }
                    isAuthorizing = false
                    val resolvedDocId = docId
                    if (resolvedDocId != null) {
                        onGoogleDocAttached(resolvedDocId, token, title)
                    } else {
                        Toast.makeText(context, "Could not resolve Google Doc ID.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    isAuthorizing = false
                    Toast.makeText(context, "Failed to get Google access token.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            isAuthorizing = false
            Toast.makeText(context, "Google authorization was not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper to process imported plain text Uri
    val handleImportedUri: (android.net.Uri?) -> Unit = { uri ->
        if (uri != null) {
            val textResult = FileUtils.readTextFromUri(context, uri)
            textResult.onSuccess { text ->
                onContentChange(text)
                val fileName = FileUtils.getFileNameFromUri(context, uri)
                if (name.isBlank() && !fileName.isNullOrBlank()) {
                    onNameChange(fileName)
                }
                Toast.makeText(
                    context,
                    "Imported ${fileName ?: "file"} successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    "Failed to read file: ${error.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Helper to process Drive URI (either Google Doc or regular text file)
    val handleDriveUri: (android.net.Uri?) -> Unit = { uri ->
        if (uri != null) {
            val rawDocTitle = FileUtils.getFileNameFromUri(context, uri) ?: ""
            var docId = GoogleAuthHelper.extractGoogleDocId(uri)
            pendingDocId = docId
            pendingDocTitle = rawDocTitle
            isAuthorizing = true

            coroutineScope.launch {
                try {
                    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                    val authResult = GoogleAuthHelper.requestDocsAuthorization(context, currentUserEmail)
                    if (authResult.hasResolution()) {
                        val pendingIntent = authResult.pendingIntent
                        if (pendingIntent != null) {
                            consentLauncher.launch(
                                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                            )
                        } else {
                            isAuthorizing = false
                        }
                    } else {
                        val token = authResult.accessToken
                        if (token != null) {
                            var resolvedDocId = docId
                            if (resolvedDocId == null && rawDocTitle.isNotBlank()) {
                                resolvedDocId = GoogleAuthHelper.resolveDriveFileId(rawDocTitle, token)
                            }
                            isAuthorizing = false
                            val finalDocId = resolvedDocId
                            if (finalDocId != null) {
                                onGoogleDocAttached(finalDocId, token, rawDocTitle)
                            } else {
                                // Fallback to plain text import if it's a plain text file in Drive
                                handleImportedUri(uri)
                            }
                        } else {
                            isAuthorizing = false
                            Toast.makeText(context, "Could not obtain Google access token.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    isAuthorizing = false
                    Toast.makeText(context, "Google authorization error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Launcher for local plain text files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        handleImportedUri(uri)
    }

    // Launcher for Google Drive picker
    val drivePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            handleDriveUri(uri)
        }
    }

    if (showImportDialog) {
        ImportSourceDialog(
            onDismiss = { showImportDialog = false },
            onSelectSource = { source ->
                when (source) {
                    ImportSource.FILE -> {
                        try {
                            filePickerLauncher.launch(arrayOf("text/plain", "text/*"))
                        } catch (_: Exception) {
                            Toast.makeText(context, "Could not open file picker", Toast.LENGTH_SHORT).show()
                        }
                    }
                    ImportSource.GOOGLE_DRIVE -> {
                        try {
                            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                            val intent = FileUtils.createGoogleDrivePickerIntent(context, currentUserEmail)
                            drivePickerLauncher.launch(intent)
                        } catch (_: Exception) {
                            try {
                                val fallbackIntent = FileUtils.createGeneralTextFilePickerIntent()
                                drivePickerLauncher.launch(fallbackIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Could not open document picker", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditMode) "Edit Chapter" else "Add New Chapter",
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (!isEditMode) {
                OutlinedButton(
                    onClick = { showImportDialog = true },
                    enabled = !isAuthorizing && !isSubmitting,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Indigo500
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (isAuthorizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Indigo500,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Authorizing...",
                            style = Typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Import Chapter",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Import",
                            style = Typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = Typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Chapter Name", color = TextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Indigo500,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Attached Google Doc indicator card if user imported via Google Docs
        if (!googleDocId.isNullOrEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Indigo500.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Google Doc",
                            tint = Indigo500,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Attached Google Doc",
                                style = Typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Indigo500
                            )
                            Text(
                                text = attachedDocName ?: "ID: $googleDocId",
                                style = Typography.bodySmall,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(
                        onClick = onClearAttachedDoc,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Google Doc",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text("Chapter Content / Text", color = TextSecondary) },
                minLines = 5,
                maxLines = 10,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo500,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Voice Selector (Required only when title has solo voice / ai_casting_enabled == false)
        if (requiresVoice) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Narrator Voice",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                if (isLoadingVoices) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterHorizontally),
                        color = Indigo500
                    )
                } else {
                    val selectedVoice = voices.find { it.id == selectedVoiceId }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedVoice?.let { "${it.name} (${it.gender})" } ?: selectedVoiceId.ifEmpty { "Select a voice" },
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVoiceDropdownExpandedChange(true) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = BorderColor,
                                disabledTextColor = TextPrimary,
                                disabledLabelColor = TextSecondary
                            )
                        )

                        DropdownMenu(
                            expanded = voiceDropdownExpanded,
                            onDismissRequest = { onVoiceDropdownExpandedChange(false) },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            voices.forEach { voice ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = voice.name,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${voice.gender} - ${voice.lang}",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        onVoiceSelected(voice.id)
                                        onVoiceDropdownExpandedChange(false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val hasValidContent = content.isNotBlank() || (!googleDocId.isNullOrBlank() && !googleAccessToken.isNullOrBlank())
        val isFormValid = name.isNotBlank() && hasValidContent && (!requiresVoice || selectedVoiceId.isNotBlank())

        Button(
            onClick = {
                if (isFormValid && !isSubmitting && !isAuthorizing) {
                    onSubmit()
                }
            },
            enabled = isFormValid && !isSubmitting && !isAuthorizing,
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
                    text = if (isEditMode) "Save Changes" else "Create Chapter",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ChapterBottomSheetAddPreview() {
    val sampleVoices = listOf(
        Voice("1", "John", "Male", "en-US", ""),
        Voice("2", "Jane", "Female", "en-US", "")
    )
    AIAudioBookTheme {
        Surface(color = DarkSurface) {
            ChapterBottomSheetContent(
                isEditMode = false,
                name = "",
                onNameChange = {},
                content = "",
                onContentChange = {},
                requiresVoice = true,
                isLoadingVoices = false,
                voices = sampleVoices,
                selectedVoiceId = "",
                onVoiceSelected = {},
                voiceDropdownExpanded = false,
                onVoiceDropdownExpandedChange = {},
                isSubmitting = false,
                onSubmit = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChapterBottomSheetEditPreview() {
    val sampleVoices = listOf(
        Voice("1", "John", "Male", "en-US", "")
    )
    AIAudioBookTheme {
        Surface(color = DarkSurface) {
            ChapterBottomSheetContent(
                isEditMode = true,
                name = "Chapter 1: The Beginning",
                onNameChange = {},
                content = "Once upon a time in a land far away...",
                onContentChange = {},
                requiresVoice = false,
                isLoadingVoices = false,
                voices = sampleVoices,
                selectedVoiceId = "1",
                onVoiceSelected = {},
                voiceDropdownExpanded = false,
                onVoiceDropdownExpandedChange = {},
                isSubmitting = false,
                onSubmit = {}
            )
        }
    }
}
