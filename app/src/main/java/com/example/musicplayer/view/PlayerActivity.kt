package com.example.musicplayer.view

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.*
import com.example.musicplayer.model.MusicService
import com.example.musicplayer.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicListPA : ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService : MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var fIndex: Int = -1

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.data?.scheme.contentEquals("content")) {
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
                .into(binding.imgSong)
            binding.tvSongName.text = musicListPA[songPosition].title
        }
        else initialization()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }
        binding.btnPrevious.setOnClickListener { prevNextSong(increment = false) }
        binding.btnNext.setOnClickListener { prevNextSong(increment = true) }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

        binding.btnRepeat.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            } else {
                repeat = false
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(this,
                    R.color.attr_themeColor
                ))
            }
        }

        binding.btnEqualizer.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(this, "Equalizer Feature not supporting", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnFavourite.setOnClickListener {
            if (isFavourite) {
                isFavourite = false
                binding.btnFavourite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                FavouriteActivity.favouriteSongs.removeAt(fIndex)
            }
            else {
                isFavourite = true
                binding.btnFavourite.setImageResource(R.drawable.ic_favorite)
                FavouriteActivity.favouriteSongs.add(musicListPA[songPosition])
            }
        }


        binding.btnTimer.setOnClickListener {
            val timer = min15 || min30 || min60
            if (!timer) showBottomSheetDialog()
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop timer?")
                    .setPositiveButton("Yes") { _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.btnTimer.setColorFilter(ContextCompat.getColor(this,
                            R.color.cool_pink
                        ))
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

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File"))
        }
    }

    private fun setLayout() {
        fIndex = favouriteChecker(musicListPA[songPosition].id)
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.splash_screen).centerCrop())
            .into(binding.imgSong)
        binding.tvSongName.text = musicListPA[songPosition].title
        if (repeat) binding.btnRepeat.setColorFilter(ContextCompat.getColor(this,
            R.color.purple_500
        ))
        if (min15 || min30 || min60) binding.btnTimer.setColorFilter(ContextCompat.getColor(this,
            R.color.purple_500
        ))
        if (isFavourite) binding.btnFavourite.setImageResource(R.drawable.ic_favorite)
        else binding.btnFavourite.setImageResource(R.drawable.ic_baseline_favorite_border_24)

    }
    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause, 1F)
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id
        } catch (e: java.lang.Exception){
            return
        }
    }
    private fun initialization() {
        songPosition = intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){
            "FavouriteAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                setLayout()
            }
            "NowPlayingFragment" -> {
                setLayout()
                binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying) binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
                else binding.btnPlayPause.setIconResource(R.drawable.ic_play)
            }
            "MusicAdapterSearch" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }
            "FavouriteShuffle" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                musicListPA.shuffle()
                setLayout()
            }
            "PlaylistDetailsAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlayListActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                setLayout()
            }
            "PlaylistDetailsShuffle" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlayListActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }
    private fun playMusic() {
        binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause, 1F)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic() {
        binding.btnPlayPause.setIconResource(R.drawable.ic_play)
        musicService!!.showNotification(R.drawable.ic_play, 0F)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment: Boolean) {
        if(increment) {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
        musicService!!.audiomanager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audiomanager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e: Exception) {return}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK)
            return
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min15)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.btnTimer.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min15 = true
            Thread {Thread.sleep((15 * 60000).toLong())
                if (min15) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min30)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.btnTimer.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min30 = true
            Thread {Thread.sleep((30 * 60000).toLong())
                if (min30) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min60)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.btnTimer.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min60 = true
            Thread {Thread.sleep((60 * 60000).toLong())
                if (min60) exitApplication()
            }.start()
            dialog.dismiss()
        }
    }

    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColum = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColum = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColum?.let { cursor.getString(it) }
            val duration = durationColum?.let { cursor.getLong(it) }!!
            return Music(
                id = "Unknown",
                title = path.toString(),
                album = "Unknown",
                artist = "Unknown",
                duration = duration,
                artUri = "Unknown",
                path = path.toString()
            )

        } finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id == "Unknown" && !isPlaying) exitApplication()
    }
}