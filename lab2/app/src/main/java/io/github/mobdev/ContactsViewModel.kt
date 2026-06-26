package io.github.mobdev

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactsViewModel : ViewModel() {
    private val _contacts = MutableLiveData<List<Contact>>(emptyList())
    val contacts: LiveData<List<Contact>> = _contacts

    private var loaded = false

    fun loadContactsIfNeeded(context: Context) {
        if (loaded) return
        _contacts.value = context.fetchAllContacts()
        loaded = true
    }

    fun reloadContacts(context: Context) {
        _contacts.value = context.fetchAllContacts()
        loaded = true
    }
}
