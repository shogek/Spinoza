package com.shogek.spinoza

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shogek.spinoza.repositories.SmsRepository

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show a modal asking for permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS),
            Build.VERSION.SDK_INT
        )

        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.i("2", "Access to read SMS not granted.")
            return
        }

        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.i("2", "Access to read contacts not granted.")
            return
        }

        val messages = SmsRepository.getAllSms(this.contentResolver)
        val contacts = SmsRepository.getAllContacts(this.contentResolver)
    }
}
