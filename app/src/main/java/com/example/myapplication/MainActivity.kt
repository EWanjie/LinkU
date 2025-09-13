package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityMainBinding
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
/*
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.purple))
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

 */

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(Chat())

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.chat -> replaceFragment(Chat())
                R.id.call -> replaceFragment(Call())
                R.id.stories -> replaceFragment(Stories())
                R.id.applikation -> replaceFragment(Aplikation())
                R.id.profile -> replaceFragment(Profile())
            }
            true
        }

        ensureNotifPermission()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    private val askNotifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* игнорируем результат*/ }

    private fun ensureNotifPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            askNotifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}