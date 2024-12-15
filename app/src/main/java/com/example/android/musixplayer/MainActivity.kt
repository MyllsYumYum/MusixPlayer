package com.example.android.musixplayer

import android.content.ContentResolver
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.android.musixplayer.databinding.IntroBinding
import com.example.android.musixplayer.databinding.MainLayoutBinding
import com.example.android.musixplayer.databinding.MainMenuBinding
import com.example.android.musixplayer.databinding.QeueListBinding
import com.example.android.musixplayer.ui.theme.MusixPlayerTheme
import java.io.File

import kotlinx.coroutines.*
import java.io.IOException


class MainActivity : ComponentActivity() {
    private lateinit var introBinding: IntroBinding
    private lateinit var mainlayBinding: MainLayoutBinding
    private lateinit var menuBinding: MainMenuBinding
    private lateinit var qeueBinding: QeueListBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // list of views
        introBinding = IntroBinding.inflate(layoutInflater)
        mainlayBinding = MainLayoutBinding.inflate(layoutInflater)
        menuBinding = MainMenuBinding.inflate(layoutInflater)
        qeueBinding = QeueListBinding.inflate(layoutInflater)
        //into procedure
        setContentView(introBinding.root)

        val mp3List = arrayOf(
            arrayOf<Any>("id","title","filepath"),
                )
        var mp3mutable = mp3List.toMutableList()
        mp3mutable = scanForMP3Files()
        var titlebar1 = mainlayBinding.titleView1

        var titlebar2 = menuBinding.titleView2
        var titlebar3 = qeueBinding.titleView3
        var menu1 = mainlayBinding.menu1
        var menu2 = menuBinding.menu2
        var menu3 = qeueBinding.menu3
        var currProg = mainlayBinding.currProg //time 1
        var maxProg = mainlayBinding.maxProg //time 2
        var playButton = mainlayBinding.playbutt
        var prevButton = mainlayBinding.nextbutt
        var nextButton = mainlayBinding.previousbutt
        var seekBar = mainlayBinding.seekBar1
        var prog1 = menuBinding.progressBar1
        var prog2 = qeueBinding.progressBar2
        var coroutinepause = false
        var mediaPlayer = MediaPlayer.create(this, R.raw.boi)
//        mediaPlayer.setDataSource("/storage/emulated/0/Samsung/Music/Over_the_Horizon.mp3")
//        // Check for permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {

            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1001
                )
            }
        } else {
             // Directly scan on older versions
        }


        fun playpause() {
            if (!mediaPlayer.isPlaying) {
                playButton.setImageResource(android.R.drawable.ic_media_pause)
                menuBinding.play2.setImageResource(android.R.drawable.ic_media_pause)
                qeueBinding.play3.setImageResource(android.R.drawable.ic_media_pause)
                //handler.post(updateSeekBar)
                mediaPlayer.start()

            } else {
                playButton.setImageResource(android.R.drawable.ic_media_play)
                menuBinding.play2.setImageResource(android.R.drawable.ic_media_play)
                qeueBinding.play3.setImageResource(android.R.drawable.ic_media_play)
                mediaPlayer.pause()
            }

        }

        seekBar.max = mediaPlayer.duration
//        titlebar1.text = getString(R.string.title)
        titlebar1.text = mp3mutable[0][1].toString()

        var curtrack = mediaPlayer.currentPosition
        var maxtrack = mediaPlayer.duration
        //////////////////////////////////////////////////////////////////////////////////////
        //   COROUTINE

        var job = CoroutineScope(Dispatchers.Main).launch {
            //fake loading
            delay(100)
            introBinding.introBar.progress = 1
            delay(2000)
            introBinding.introBar.progress = 40
            delay(500)
            introBinding.introBar.progress = 45
            delay(100)
            introBinding.introBar.progress = 80
            delay(2000)
            introBinding.introBar.progress = 100
            setContentView(mainlayBinding.root)



            var seekPause = false
            var seekNew = 500 //changes alot
            // Co Routines
            maxtrack = mediaPlayer.duration
            prog1.max = maxtrack
            prog2.max = maxtrack
            prog1.progress = curtrack
            prog2.progress = curtrack
// Set up the listener
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    seekNew = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    seekPause = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    mediaPlayer.seekTo(seekNew)
                    seekPause = false
                }
            })
            while (true) {
                while (!seekPause && !coroutinepause) {
                    curtrack = mediaPlayer.currentPosition
                    prog1.max = mediaPlayer.duration
                    prog2.max = mediaPlayer.duration
                    maxtrack = mediaPlayer.duration
                    prog1.progress = curtrack
                    prog2.progress = curtrack
                    seekBar.progress = curtrack
                    currProg.text =
                        String.format("%02d:%02d", (curtrack / 1000) / 60, (curtrack / 1000) % 60)
                    maxProg.text =
                        String.format("%02d:%02d", (maxtrack / 1000) / 60, (maxtrack / 1000) % 60)
                    delay(1000)
                }
                currProg.text =
                    String.format("%02d:%02d", (seekNew / 1000) / 60, (seekNew / 1000) % 60)
                delay(100)
            }
        }

        ///////////////////////////////////////////////////////////////////


        titlebar2.setOnClickListener {
            setContentView(mainlayBinding.root)
        }

        titlebar3.setOnClickListener {
            setContentView(mainlayBinding.root)
        }

        menuBinding.botBar.setOnClickListener {
            setContentView(mainlayBinding.root)
        }

        qeueBinding.botBar.setOnClickListener {
            setContentView(mainlayBinding.root)
        }

        menu1.setOnClickListener {
            setContentView(menuBinding.root)
        }

        menu2.setOnClickListener {
            setContentView(menuBinding.root)
        }

        menu3.setOnClickListener {
            setContentView(menuBinding.root)
        }

        playButton.setOnClickListener {
            playpause()
        }
        menuBinding.play2.setOnClickListener {
            playpause()
        }
        qeueBinding.play3.setOnClickListener {
            playpause()
        }

        menuBinding.imageButton8.setOnClickListener{
            coroutinepause = true
            Toast.makeText(this, mp3mutable[1][0].toString() +" titled "+mp3mutable[1][1], Toast.LENGTH_SHORT).show()
            Toast.makeText(this, mp3mutable[1][2].toString(), Toast.LENGTH_SHORT).show()
            try {
//                val path = File(mp3mutable[1][1])
//                val uri: Uri = Uri.fromFile(path)
                val uri: Uri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mp3mutable[1][0] as Long
                ) /////
                Toast.makeText(this, "success 1", Toast.LENGTH_SHORT).show()
//                Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show()
//                Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show()

                val cursor = contentResolver.query(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Media.DATA), // Get the file path
                    "${MediaStore.Audio.Media._ID} = ?",
                    arrayOf("15"), // Replace "15" with your ID
                    null
                )

                if (cursor != null && cursor.moveToFirst()) {
                    val filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show()
                    println("File path: $filePath")
                    mainlayBinding.titleView1.text = uri.toString()
                    Log.d("MediaPlayer", "URI: $uri")
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(this, uri)
                    mediaPlayer.prepare()
                    Toast.makeText(this, "this worked", Toast.LENGTH_SHORT).show()


                } else {
                    println("No file found with ID 15 in MediaStore")
                    Toast.makeText(this, "No file found with ID 15 in MediaStore", Toast.LENGTH_SHORT).show()
                }
                cursor?.close()
//                mediaPlayer.setDataSource(this , uri)
//                ^^^ this is broken ^^^


                Toast.makeText(this, "success 2", Toast.LENGTH_SHORT).show()
            } catch (e : Exception) {
                e.printStackTrace()
//                mainlayBinding.titleView1.text = e.message.toString()

            }
            coroutinepause = false

        }

        mainlayBinding.Qeue.setOnClickListener {
            setContentView(qeueBinding.root)
        }








        mediaPlayer.setOnCompletionListener {
            playButton.setImageResource(android.R.drawable.ic_media_pause)
            seekBar.progress = 0
            if (true) { // if repeat is on
                mediaPlayer.seekTo(0)
            }
        }
    }

    // Function to scan for MP3 files
    //make this into the fetch function
    private fun scanForMP3Files(): MutableList<Array<Any>> {
        val array1 = arrayOf(arrayOf<Any>("id","title","path"))
        val mp3mutable : MutableList<Array<Any>> = array1.toMutableList()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,           // File ID
            MediaStore.Audio.Media.TITLE,         // File title
            MediaStore.Audio.Media.ARTIST,        // Artist
            MediaStore.Audio.Media.DATA           // File path
        )

        val selection = "${MediaStore.Audio.Media.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("audio/mpeg")

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null // Sort order
        )

        if (cursor != null && cursor.moveToFirst()) {
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

            do {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val path = cursor.getString(dataColumn)
                // You can display or store the MP3 file data (title, artist, path)
//                Toast.makeText(this, "Found MP3: " + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString(), Toast.LENGTH_SHORT).show()
                mp3mutable.add(arrayOf(id,"$title","$path"))

                // Handle the file path (e.g., you can add it to a list or play it)
            } while (cursor.moveToNext())
            cursor.close()
            return mp3mutable
        } else {
            Toast.makeText(this, "No MP3 files found", Toast.LENGTH_SHORT).show()
            return mp3mutable
        }
    } //////////////////////////


    /////////////////////////////////


}





fun getMediaTitle(filePath: String): String? {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(filePath) // Set the file path or URI of the media
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        retriever.release() // Release resources
    }
    return null // Return null if title couldn't be retrieved
}


    //deprecate
    fun waitSeconds(seconds: Int, onComplete: () -> Unit) {
        GlobalScope.launch {
            delay(seconds * 1000L) // Wait for specified seconds
            withContext(Dispatchers.Main) { // Execute on main thread
                onComplete()
            }
        }
    }


