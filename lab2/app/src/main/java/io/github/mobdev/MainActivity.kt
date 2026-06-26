package io.github.mobdev

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val viewModel: ContactsViewModel by viewModels()

    private var hasPermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        if (granted) {
            viewModel.reloadContacts(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updatePermissionState()

        setContent {
            val contacts by viewModel.contacts.observeAsState(emptyList())

            MaterialTheme {
                ContactsScreen(
                    hasPermission = hasPermission,
                    contacts = contacts,
                    onRequestPermission = { requestContactsPermission() },
                    onContactClick = { contact -> openContactDetail(contact) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionState()
        if (hasPermission) {
            viewModel.reloadContacts(this)
        }
    }

    private fun updatePermissionState() {
        hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.loadContactsIfNeeded(this)
        }
    }

    private fun requestContactsPermission() {
        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun openContactDetail(contact: Contact) {
        val intent = Intent(this, ContactDetailActivity::class.java).apply {
            putExtra(ContactDetailActivity.EXTRA_NAME, contact.name)
            putExtra(
                ContactDetailActivity.EXTRA_PHONES,
                contact.phoneNumbers.toTypedArray(),
            )
            putExtra(ContactDetailActivity.EXTRA_EMAIL, contact.email)
        }
        startActivity(intent)
    }
}
