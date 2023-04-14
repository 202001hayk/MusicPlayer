package com.example.musicplayer.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.view.PlayListActivity
import com.example.musicplayer.Playlist
import com.example.musicplayer.view.PlaylistDetails
import com.example.musicplayer.R
import com.example.musicplayer.databinding.PlaylistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistViewAdapter(
    private val context: Context,
    private var playlistList: ArrayList<Playlist>
) : RecyclerView.Adapter<PlaylistViewAdapter.MyHolder>() {
    class MyHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.playlistImg
        val name = binding.tvPlaylistName
        val root = binding.root
        val delete = binding.imgPlaylistDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        holder.delete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playlistList[position].name)
                .setMessage("Do you want to delete playlist?")
                .setPositiveButton("Yes") { dialog, _ ->
                    PlayListActivity.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
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
        holder.root.setOnClickListener {
            val intent = Intent(context, PlaylistDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }
        if (PlayListActivity.musicPlaylist.ref[position].playlist.size > 0) {
            Glide.with(context)
                .load(PlayListActivity.musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(holder.image)
        }
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist() {
        playlistList = ArrayList()
        playlistList.addAll(PlayListActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }
}