package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Account : AppCompatActivity() {

    private lateinit var imgButton : ImageButton

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_account)

        val person: Person? = intent.getParcelableExtra("person", Person::class.java)

        val nameView = findViewById<TextView>(R.id.user_name)
        val onlineView = findViewById<TextView>(R.id.user_online)
        val imageView = findViewById<ImageView>(R.id.user_photo)


        if (person != null) {
            nameView.text = person.fullName
            onlineView.text = person.online
            imageView.setImageResource(person.fullImg)
        }


        imgButton = findViewById(R.id.acc_to_text)
        imgButton.setOnClickListener {
            finish()
        }
    }
}