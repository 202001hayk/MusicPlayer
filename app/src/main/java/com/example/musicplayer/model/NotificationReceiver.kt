package com.example.musicplayer.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.*
import com.example.musicplayer.view.fragment.NowPlayingFragment
import com.example.musicplayer.view.PlayerActivity

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = context!!)
            ApplicationClass.PLAY -> if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> prevNextSong(increment = true, context = context!!)
            ApplicationClass.EXIT -> {
                exitApplication()
            }
        }
    }
    private fun playMusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause, 1F)
        PlayerActivity.binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
        NowPlayingFragment.binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_play,0F)
        PlayerActivity.binding.btnPlayPause.setIconResource(R.drawable.ic_play)
        NowPlayingFragment.binding.btnPlayPause.setIconResource(R.drawable.ic_play)

    }

    private fun prevNextSong(increment : Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
            .into(PlayerActivity.binding.imgSong)
        PlayerActivity.binding.tvSongName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
            .into(NowPlayingFragment.binding.songImg)
        NowPlayingFragment.binding.tvSongName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavourite) PlayerActivity.binding.btnFavourite.setImageResource(R.drawable.ic_favorite)
        else PlayerActivity.binding.btnFavourite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
    }
}