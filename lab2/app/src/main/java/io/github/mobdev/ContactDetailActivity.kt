package io.github.mobdev

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme

class ContactDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra(EXTRA_NAME)
        val phones = intent.getStringArrayExtra(EXTRA_PHONES)?.toList().orEmpty()
        val email = intent.getStringExtra(EXTRA_EMAIL)

        setContent {
            MaterialTheme {
                ContactDetailScreen(
                    name = name,
                    phoneNumbers = phones,
                    email = email,
                    onBack = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_PHONES = "extra_phones"
        const val EXTRA_EMAIL = "extra_email"
    }
}
