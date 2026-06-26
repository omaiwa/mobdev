package io.github.mobdev

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import androidx.core.database.getStringOrNull

data class Contact(
    val contactId: Long,
    val name: String?,
    val phoneNumbers: List<String>,
    val email: String?,
)

@SuppressLint("Range")
fun Context.fetchAllContacts(): List<Contact> {
    Log.d("FETCH", "fetchAllContacts called")

    val emailsByContactId = fetchContactEmails()
    val contactsById = linkedMapOf<Long, Pair<String?, MutableSet<String>>>()

    contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
    ).use { cursor: Cursor? ->
        if (cursor == null) return emptyList()

        val contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (cursor.moveToNext()) {
            val contactId = cursor.getLong(contactIdIndex)
            val name = cursor.getStringOrNull(nameIndex)
            val phoneNumber = cursor.getStringOrNull(phoneIndex)

            val entry = contactsById.getOrPut(contactId) { name to mutableSetOf() }
            if (phoneNumber != null) {
                entry.second.add(phoneNumber)
            }
        }
    }

    return contactsById.map { (contactId, nameAndPhones) ->
        Contact(
            contactId = contactId,
            name = nameAndPhones.first,
            phoneNumbers = nameAndPhones.second.toList(),
            email = emailsByContactId[contactId],
        )
    }.sortedBy { it.name?.lowercase().orEmpty() }
}

@SuppressLint("Range")
private fun Context.fetchContactEmails(): Map<Long, String> {
    val emails = mutableMapOf<Long, String>()

    contentResolver.query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Email.CONTACT_ID,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
        ),
        null,
        null,
        null,
    ).use { cursor: Cursor? ->
        if (cursor == null) return emails

        val contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
        val emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)

        while (cursor.moveToNext()) {
            val contactId = cursor.getLong(contactIdIndex)
            if (contactId in emails) continue

            val email = cursor.getStringOrNull(emailIndex)
            if (!email.isNullOrBlank()) {
                emails[contactId] = email
            }
        }
    }

    return emails
}
