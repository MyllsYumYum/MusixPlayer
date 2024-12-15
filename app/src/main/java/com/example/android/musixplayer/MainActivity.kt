package com.example.android.musixplayer

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.musixplayer.databinding.IntroBinding
import com.example.android.musixplayer.databinding.ItemlayoutBinding
import com.example.android.musixplayer.databinding.MainLayoutBinding
import com.example.android.musixplayer.databinding.MainMenuBinding
import com.example.android.musixplayer.databinding.QeueListBinding
import com.example.android.musixplayer.ui.theme.MusixPlayerTheme
import java.io.File

import kotlinx.coroutines.*
import java.io.IOException



class MP3Adapter(
    private val mp3List: List<Array<Any>>,
    private val onItemClick: (Array<Any>) -> Unit

) : RecyclerView.Adapter<MP3Adapter.MP3ViewHolder>(),Filterable {

    private var filteredList: List<Array<Any>> = mp3List  // List of filtered items

//    private lateinit var itemView: ItemlayoutBinding Z
    // ViewHolder class to hold the item views
    class MP3ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val pathTextView: TextView = itemView.findViewById(R.id.pathTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MP3ViewHolder {
        // Inflate the layout for each item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemlayout, parent, false)
        return MP3ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MP3ViewHolder, position: Int) {
        // Get the current MP3 item
        val mp3 = filteredList[position]

        // Bind data to views
        holder.titleTextView.text = mp3[1].toString()  // Title
        holder.pathTextView.text = mp3[2].toString()  // Path
        // Set click listener for the item
        holder.itemView.setOnClickListener {
            // When an item is clicked, pass the item data to the onItemClick lambda
            onItemClick(mp3)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString().lowercase()
                filteredList = if (query.isEmpty()) {
                    mp3List  // Show all items if search query is empty
                } else {
                    mp3List.filter {
                        it[1].toString().lowercase().contains(query)  // Filter by title (you can extend it to search by other fields)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.let {
                    filteredList = it.values as List<Array<Any>>
                    notifyDataSetChanged()  // Notify adapter of changes
                }
            }
        }
    }

}


class MainActivity : ComponentActivity() {
    private lateinit var introBinding: IntroBinding
    private lateinit var mainlayBinding: MainLayoutBinding
    private lateinit var menuBinding: MainMenuBinding
    private lateinit var qeueBinding: QeueListBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        // list of views
        introBinding = IntroBinding.inflate(layoutInflater)
        mainlayBinding = MainLayoutBinding.inflate(layoutInflater)
        menuBinding = MainMenuBinding.inflate(layoutInflater)
        qeueBinding = QeueListBinding.inflate(layoutInflater)
        //into procedure
        setContentView(introBinding.root)


        var mp3mutable : MutableList<Array<Any>> = mutableListOf()
        mp3mutable = scanForMP3Files()
        var idTracker = 0
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


        val recyclerView: RecyclerView = qeueBinding.recycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        val recyclerView2: RecyclerView = menuBinding.recycler
        recyclerView.layoutManager = LinearLayoutManager(this)



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
                mediaPlayer.start()

            } else {
                playButton.setImageResource(android.R.drawable.ic_media_play)
                menuBinding.play2.setImageResource(android.R.drawable.ic_media_play)
                qeueBinding.play3.setImageResource(android.R.drawable.ic_media_play)
                mediaPlayer.pause()
            }

        }

        fun mediaSwitch(music : Array<Any>){
            try {

                mediaPlayer.reset()
                val uri : Uri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    music[0] as Long )
                mediaPlayer.setDataSource(this,uri)
                mediaPlayer.prepare()
                playpause()
                menuBinding.titleView2.text = music[1].toString()
                qeueBinding.titleView3.text = music[1].toString()
                mainlayBinding.titleView1.text = music[1].toString()
                idTracker = mp3mutable.indexOf(music)
            } catch (e : Exception){
                Toast.makeText(this,"Media Failed to Load", Toast.LENGTH_SHORT).show()
                Log.d("MEDIAPLAYER","New Media Failed to Load")
            }

        }

        mediaSwitch(mp3mutable[1])

        val adapter = MP3Adapter(mp3mutable) {selectedItem ->
            mediaSwitch(selectedItem)
        }
        qeueBinding.recycler.adapter = adapter
        menuBinding.recycler.adapter = adapter

        val searchView: SearchView = qeueBinding.searchBar
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Optionally handle query submission if needed
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Pass the search query to the adapter's filter method
                adapter.filter.filter(newText)
                return true
            }

        })



        seekBar.max = mediaPlayer.duration
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
//
//        menuBinding.botBar.setOnClickListener {
//            setContentView(mainlayBinding.root)
//        }
//
//        qeueBinding.botBar.setOnClickListener {
//            setContentView(mainlayBinding.root)
//        }

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
            mediaSwitch(mp3mutable[1])
        }

        mainlayBinding.Qeue.setOnClickListener {
            setContentView(qeueBinding.root)
        }

        mainlayBinding.previousbutt.setOnClickListener{
            mediaSwitch(mp3mutable[idTracker - 1])
        }

        mainlayBinding.nextbutt.setOnClickListener {
            mediaSwitch(mp3mutable[idTracker + 1])

        }









        mediaPlayer.setOnCompletionListener {
            playButton.setImageResource(android.R.drawable.ic_media_pause)
            seekBar.progress = 0
            if (true) { // if repeat is on
                mediaPlayer.seekTo(0)

            }
            mediaSwitch(mp3mutable[idTracker + 1])
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


