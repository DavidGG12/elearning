package com.example.elearning

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage

class VideoPlayer : AppCompatActivity()
{
    //Firebase
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference

    private lateinit var prgsBar: ProgressBar
    private lateinit var videoPlayer: VideoView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        //Firebase
        FirebaseApp.initializeApp(this)

        //Resources
        prgsBar = findViewById(R.id.progressBar2)
        videoPlayer = findViewById(R.id.videoView)

        val ruth = intent.getStringExtra("ruth")

        //Video recover
        val videoRef = storageReference.child(ruth.toString())
        videoRef.downloadUrl
            .addOnSuccessListener { uri ->
                playVideo(uri.toString())
            }
            .addOnFailureListener{
                Toast.makeText(this, "Error al reproducir el vídeo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun playVideo(videoUrl: String)
    {
        val videoUri = Uri.parse(videoUrl)
        videoPlayer.setVideoURI(videoUri)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoPlayer)
        videoPlayer.setMediaController(mediaController)
        videoPlayer.start()
        prgsBar.visibility = View.GONE
    }
}