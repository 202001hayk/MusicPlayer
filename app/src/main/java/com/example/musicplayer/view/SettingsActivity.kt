package com.example.musicplayer.view

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.musicplayer.BuildConfig
import com.example.musicplayer.databinding.ActivitySettingsBinding
import com.example.musicplayer.exitApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Settings"

        when (MainActivity.themeIndex) {
            0 -> binding.imgCoolPinkTheme.setBackgroundColor(Color.YELLOW)
            1 -> binding.imgCoolBlueTheme.setBackgroundColor(Color.YELLOW)
            2 -> binding.imgCoolPurpleTheme.setBackgroundColor(Color.YELLOW)
            3 -> binding.imgCoolGreenTheme.setBackgroundColor(Color.YELLOW)
            4 -> binding.imgCoolBlackTheme.setBackgroundColor(Color.YELLOW)
        }

        binding.imgCoolPinkTheme.setOnClickListener { saveTheme(0) }
        binding.imgCoolBlueTheme.setOnClickListener { saveTheme(1) }
        binding.imgCoolPurpleTheme.setOnClickListener { saveTheme(2) }
        binding.imgCoolGreenTheme.setOnClickListener { saveTheme(3) }
        binding.imgCoolBlackTheme.setOnClickListener { saveTheme(4) }

        binding.tvVersionName.text = setVersionDetails()

        binding.btnSort.setOnClickListener {
            val menuList = arrayOf("Recent Added", "Song Title", "File Size")
            var currentSort = MainActivity.sortOrder
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Sorting ")
                .setPositiveButton("OK") { _, _ ->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder", currentSort)
                    editor.apply()
                }
                .setSingleChoiceItems(menuList, currentSort) { _, which ->
                    currentSort = which
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        }
    }

    private fun saveTheme(index: Int) {
        if (MainActivity.themeIndex != index) {
            val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editor.putInt("themeIndex", index)
            editor.apply()
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Apply Theme")
                .setMessage("Do you want to apply theme?")
                .setPositiveButton("Yes") { _, _ ->
                    exitApplication()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    private fun setVersionDetails(): String {
        return "Version Name: ${BuildConfig.VERSION_NAME}"
    }
}