//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//class MP3Adapter(private val mp3List: List<Array<Any>>) : RecyclerView.Adapter<MP3Adapter.MP3ViewHolder>() {
//
//    // ViewHolder class to hold the item views
//    class MP3ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
//        val pathTextView: TextView = itemView.findViewById(R.id.pathTextView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MP3ViewHolder {
//        // Inflate the layout for each item
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
//        return MP3ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: MP3ViewHolder, position: Int) {
//        // Get the current MP3 item
//        val mp3 = mp3List[position]
//
//        // Bind data to views
//        holder.titleTextView.text = mp3[1].toString()  // Title
//        holder.pathTextView.text = mp3[2].toString()  // Path
//    }
//
//    override fun getItemCount(): Int {
//        return mp3List.size
//    }
//}
