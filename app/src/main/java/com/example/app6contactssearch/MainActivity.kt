package com.example.app6contactssearch

import Contact
import ContactsAdapter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextWatcher
import android.text.Editable

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ContactsAdapter
    private var allContacts = listOf<Contact>()
    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        checkPermissions()
        setupSearch()
        loadContactsFromAssets()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.contactsRecyclerView)
        adapter = ContactsAdapter(emptyList()) { contact ->
            makePhoneCall(contact.phoneNumber)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.searchEditText).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                val filteredContacts = if (query.isEmpty()) {
                    allContacts
                } else {
                    allContacts.filter { contact ->
                        contact.name.lowercase().contains(query) ||
                        contact.phoneNumber.contains(query)
                    }
                }
                adapter.updateContacts(filteredContacts)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE

        )
        
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            loadContacts()
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun loadContacts() {
        val contacts = mutableListOf<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID))
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val photoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                
                contacts.add(Contact(id, name, phoneNumber, photoUri))
            }
        }

        allContacts = contacts
        adapter.updateContacts(contacts)
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$phoneNumber")
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun loadContactsFromAssets() {
        val contacts = mutableListOf<Contact>()
        try {
            assets.open("sample_contacts.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
                    if (parts.size >= 4) {
                        contacts.add(Contact(
                            id = parts[0],
                            name = parts[0],
                            phoneNumber = parts[2],
                            photoUri = parts[3]
                        ))
                    }
                }
            }
            allContacts = contacts
            adapter.updateContacts(contacts)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            loadContacts()
        }
    }
}