package com.liana.accounting

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var emptyText: TextView
    private lateinit var messageList: ListView
    private lateinit var permissionButton: Button
    private lateinit var refreshButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        emptyText = findViewById(R.id.emptyText)
        messageList = findViewById(R.id.messageList)
        permissionButton = findViewById(R.id.permissionButton)
        refreshButton = findViewById(R.id.refreshButton)

        permissionButton.setOnClickListener { requestSmsPermission() }
        refreshButton.setOnClickListener { loadSmsMessages() }

        renderPermissionState()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_SMS) {
            renderPermissionState()
        }
    }

    private fun renderPermissionState() {
        val granted = checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        permissionButton.visibility = if (granted) View.GONE else View.VISIBLE
        refreshButton.visibility = if (granted) View.VISIBLE else View.GONE

        if (granted) {
            statusText.text = getString(R.string.permission_granted)
            loadSmsMessages()
        } else {
            statusText.text = getString(R.string.permission_explanation)
            messageList.adapter = null
            emptyText.visibility = View.VISIBLE
            emptyText.text = getString(R.string.permission_required)
        }
    }

    private fun requestSmsPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_SMS), REQUEST_READ_SMS)
    }

    private fun loadSmsMessages() {
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            renderPermissionState()
            return
        }

        statusText.text = getString(R.string.loading)

        val messages = runCatching { querySms() }
            .onFailure { statusText.text = getString(R.string.read_error, it.localizedMessage ?: "Unknown error") }
            .getOrDefault(emptyList())

        messageList.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            messages
        )

        emptyText.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
        emptyText.text = getString(R.string.no_messages)
        statusText.text = resources.getQuantityString(
            R.plurals.message_count,
            messages.size,
            messages.size
        )
    }

    private fun querySms(): List<String> {
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val results = mutableListOf<String>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI

        contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (cursor.moveToNext() && results.size < MAX_MESSAGES) {
                results += formatMessage(cursor, addressIndex, bodyIndex, dateIndex)
            }
        }

        return results
    }

    private fun formatMessage(
        cursor: Cursor,
        addressIndex: Int,
        bodyIndex: Int,
        dateIndex: Int
    ): String {
        val sender = cursor.getString(addressIndex).orEmpty().ifBlank { getString(R.string.unknown_sender) }
        val body = cursor.getString(bodyIndex).orEmpty()
        val timestamp = cursor.getLong(dateIndex)
        val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(Date(timestamp))

        return "$sender  •  $date\n$body"
    }

    companion object {
        private const val REQUEST_READ_SMS = 1001
        private const val MAX_MESSAGES = 250
    }
}
