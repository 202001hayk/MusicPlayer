package com.example.musicplayer.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.view.MainActivity
import com.example.musicplayer.view.PlayerActivity
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentNowPlayingBinding
import com.example.musicplayer.setSongPosition

class NowPlayingFragment : Fragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireContext().theme.applyStyle(MainActivity.currentTheme[MainActivity.themeIndex], true)
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE

        binding.btnPlayPause.setOnClickListener {
            if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
        }

        binding.btnNext.setOnClickListener {
            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(binding.songImg)
            binding.tvSongName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause, 1F)
            playMusic()
        }
        
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "NowPlayingFragment")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.tvSongName.isSelected = true
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(binding.songImg)
            binding.tvSongName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            if (PlayerActivity.isPlaying) binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
            else binding.btnPlayPause.setIconResource(R.drawable.ic_play)
        }
    }

    private fun playMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause, 1F)
        PlayerActivity.binding.btnNext.setIconResource(R.drawable.ic_pause)
        PlayerActivity.isPlaying = true
    }

    private fun pauseMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.btnPlayPause.setIconResource(R.drawable.ic_play)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_play, 0F)
        PlayerActivity.binding.btnNext.setIconResource(R.drawable.ic_play)
        PlayerActivity.isPlaying = false
    }
}