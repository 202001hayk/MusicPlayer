package com.example.musicplayer.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.Music
import com.example.musicplayer.checkPlaylist
import com.example.musicplayer.databinding.ActivityFavouriteBinding
import com.example.musicplayer.view.adapter.FavouriteAdapter

class FavouriteActivity : AppCompatActivity() {

    lateinit var binding: ActivityFavouriteBinding
    private lateinit var adapter: FavouriteAdapter

    companion object {
        var favouriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favouriteSongs = checkPlaylist(favouriteSongs)
        binding.btnBack.setOnClickListener { finish() }
        binding.rvFavourite.setHasFixedSize(true)
        binding.rvFavourite.setItemViewCacheSize(13)
        binding.rvFavourite.layoutManager = GridLayoutManager(this, 4)
        adapter = FavouriteAdapter(this, favouriteSongs)
        binding.rvFavourite.adapter = adapter


        if (favouriteSongs.size < 1) binding.btnShuffle.visibility = View.INVISIBLE
        binding.btnShuffle.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavouriteShuffle")
            startActivity(intent)
        }
    }
}