package com.example.musicplayer.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.musicplayer.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "About"
        binding.tvAbout.text = aboutText()
    }

    private fun aboutText(): String {
        return "Developed By: Hayk Vardazaryan" +
                "\n\nIf you want to provide feedback, I will love to hear that"
    }
}