package com.example.musicplayer.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.MusicPlaylist
import com.example.musicplayer.Playlist
import com.example.musicplayer.R
import com.example.musicplayer.view.adapter.PlaylistViewAdapter
import com.example.musicplayer.databinding.ActivityPlayListBinding
import com.example.musicplayer.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayListActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlayListBinding
    lateinit var adapter: PlaylistViewAdapter

    companion object {
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityPlayListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvPlaylist.setHasFixedSize(true)
        binding.rvPlaylist.setItemViewCacheSize(13)
        binding.rvPlaylist.layoutManager = GridLayoutManager(this@PlayListActivity, 2)
        adapter = PlaylistViewAdapter(this, playlistList = musicPlaylist.ref)
        binding.rvPlaylist.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddPlaylist.setOnClickListener { customAlertDialog() }
    }

    private fun customAlertDialog() {
        val customDialog = LayoutInflater.from(this@PlayListActivity)
            .inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Playlist Details")
            .setPositiveButton("ADD") { dialog, _ ->
                val playlistName = binder.playlistName.text
                val createdBy = binder.yourName.text
                if (playlistName != null && createdBy != null)
                    if (playlistName.isNotEmpty() && createdBy.isNotEmpty()) {
                        addPlaylist(playlistName.toString(), createdBy.toString())
                    }
                dialog.dismiss()
            }.show()
    }

    private fun addPlaylist(name: String, createdBy: String) {
        var playlistExits = false
        for (i in musicPlaylist.ref) {
            if (name.equals(i.name)) {
                playlistExits = true
                break
            }
        }
        if (playlistExits) Toast.makeText(this, "Playlist Exist!!", Toast.LENGTH_SHORT).show()
        else {
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createdBy = createdBy
            val calendar = java.util.Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)
            adapter.refreshPlaylist()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}