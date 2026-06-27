package io.github.mobdev.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mobdev.R

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onDismissError: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.login_title))
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.username,
            onValueChange = onUsernameChange,
            label = { Text(stringResource(R.string.login_username_hint)) },
            singleLine = true,
            enabled = !uiState.isLoading,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.login_password_hint)) },
            singleLine = true,
            enabled = !uiState.isLoading,
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLoginClick,
            ) {
                Text(stringResource(R.string.login_button))
            }
        }
    }

    if (uiState.showErrorDialog) {
        val messageRes = uiState.errorMessageRes ?: R.string.login_error_generic
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text(stringResource(R.string.login_title)) },
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text(stringResource(R.string.error_dialog_ok))
                }
            },
        )
    }
}
