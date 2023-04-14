package com.example.musicplayer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.R
import com.example.musicplayer.view.adapter.MusicAdapter
import com.example.musicplayer.checkPlaylist
import com.example.musicplayer.databinding.ActivityPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistDetails : AppCompatActivity() {

    lateinit var binding: ActivityPlaylistDetailsBinding
    lateinit var adapter: MusicAdapter

    companion object {
        var currentPlaylistPos: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            currentPlaylistPos = intent.extras?.get("index") as Int
            PlayListActivity.musicPlaylist.ref[currentPlaylistPos].playlist =
                checkPlaylist(playlist = PlayListActivity.musicPlaylist.ref[currentPlaylistPos].playlist)
        } catch (_: Exception) {
        }
        binding.rvPlaylistDetails.setItemViewCacheSize(10)
        binding.rvPlaylistDetails.setHasFixedSize(true)
        binding.rvPlaylistDetails.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(
            this,
            PlayListActivity.musicPlaylist.ref[currentPlaylistPos].playlist,
            playlistDetails = true
        )
        binding.rvPlaylistDetails.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        binding.btnShuffleDetails.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            startActivity(intent)
        }

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }

        binding.btnRemove.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove")
                .setMessage("Do you want to remove all songs from playlist?")
                .setPositiveButton("Yes") { dialog, _ ->
                    PlayListActivity.musicPlaylist.ref[currentPlaylistPos].playlist.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()
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

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.tvPlaylistName.text = PlayListActivity.musicPlaylist.ref[currentPlaylistPos].name
        binding.tvMoreInfo.text = "Total ${adapter.itemCount} Songs.\n\n" +
                "Created On:\n${PlayListActivity.musicPlaylist.ref[currentPlaylistPos].createdOn}\n\n" +
                " -- ${PlayListActivity.musicPlaylist.ref[currentPlaylistPos].createdBy}"
        if (adapter.itemCount > 0) {
            Glide.with(this)
                .load(PlayListActivity.musicPlaylist.ref[currentPlaylistPos].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(binding.imgPlaylist)
            binding.btnShuffleDetails.visibility = View.VISIBLE
        }

        adapter.notifyDataSetChanged()

        //for storing favourites data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlayListActivity.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }
}