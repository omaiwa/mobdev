package io.github.mobdev

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    name: String?,
    phoneNumbers: List<String>,
    email: String?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val unknown = context.getString(R.string.value_unknown)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = context.getString(R.string.contact_detail_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = context.getString(R.string.back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(
                    label = context.getString(R.string.label_name),
                    value = name ?: unknown,
                )
                DetailRow(
                    label = context.getString(R.string.label_phone),
                    value = phoneNumbers.joinToString("\n").ifBlank { unknown },
                )
                DetailRow(
                    label = context.getString(R.string.label_email),
                    value = email.orEmpty(),
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(top = 12.dp),
    )
    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
    )
}
