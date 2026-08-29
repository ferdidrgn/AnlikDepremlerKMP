package com.ferdidrgn.anlikdepremler.ui.screen

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.ferdidrgn.anlikdepremler.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentScreen(
    documentType: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var htmlContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val isPrivacy = documentType.equals("privacy_policy", ignoreCase = true)
    val title =
        if (isPrivacy) stringResource(R.string.privacy_policy) else stringResource(R.string.terms_conditions)
    val offlineErrorText = stringResource(R.string.error_offline_text)
    val loadErrorText = stringResource(R.string.error_load_text)

    LaunchedEffect(documentType) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }

            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings

            db.collection("AppTools")
                .orderBy(documentType)
                .addSnapshotListener { snapshot, error ->
                    isLoading = false
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        htmlContent =
                            snapshot.documents.firstOrNull()?.getString(documentType) ?: ""
                    } else {
                        if (htmlContent.isEmpty()) {
                            htmlContent = "<p>$offlineErrorText</p>"
                        }
                    }
                }
        } catch (e: Exception) {
            isLoading = false
            htmlContent = "<p>$loadErrorText</p>"
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPrivacy) Icons.Default.Shield else Icons.Default.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPrivacy) stringResource(R.string.privacy_header_title) else stringResource(
                                R.string.terms_header_title
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(R.string.last_update),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                    } else {
                        val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

                        AndroidView(
                            factory = { ctx ->
                                TextView(ctx).apply {
                                    setTextColor(textColor)
                                    textSize = 14f
                                    setLineSpacing(6f, 1.1f)
                                }
                            },
                            update = { textView ->
                                textView.text = HtmlCompat.fromHtml(
                                    htmlContent,
                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}