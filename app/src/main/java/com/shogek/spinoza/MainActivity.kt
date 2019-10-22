package com.shogek.spinoza

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shogek.spinoza.repositories.SmsRepository

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // The only way to ask for permission to read SMS starting from Android M.
        ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_SMS"), 123)
        if (ContextCompat.checkSelfPermission(baseContext, "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            val messages = SmsRepository.getAllSms(this.contentResolver)
        }
    }
}
