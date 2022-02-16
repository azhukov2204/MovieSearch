package ru.androidlearning.moviesearch.ui.movie_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.model.web.ActorItem
import ru.androidlearning.moviesearch.ui.search.POSTERS_BASE_URL

class ActorsRecyclerViewAdapter : RecyclerView.Adapter<ActorsRecyclerViewAdapter.ActorsRecyclerViewHolder>() {
    private var actors: List<ActorItem> = listOf()
    private var onActorClickListener: OnActorClickListener? = null

    fun setData(actors: List<ActorItem>) {
        this.actors = actors
        notifyDataSetChanged()
    }

    fun setOnClickListener(onActorClickListener: OnActorClickListener) {
        this.onActorClickListener = onActorClickListener
    }

    fun removeListener() {
        onActorClickListener = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorsRecyclerViewHolder {
        return ActorsRecyclerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.actor_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ActorsRecyclerViewHolder, position: Int) {
        holder.bind(actors[position])
    }

    override fun getItemCount(): Int {
        return actors.size
    }

    inner class ActorsRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(actorItem: ActorItem) {
            itemView.findViewById<TextView>(R.id.actor_name).text = actorItem.name
            Picasso.get().load("$POSTERS_BASE_URL${actorItem.actorPhotoURL}")
                .into(itemView.findViewById<AppCompatImageView>(R.id.actorPhoto))
            itemView.setOnClickListener { onActorClickListener?.onClick(actorItem) }
        }
    }

    interface OnActorClickListener {
        fun onClick(actor: ActorItem)
    }
}
