package ru.androidlearning.moviesearch.ui.phones_book

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.androidlearning.moviesearch.R

class PhoneBookRecyclerViewAdapter : RecyclerView.Adapter<PhoneBookRecyclerViewAdapter.PhoneBookRecyclerViewHolder>() {
    private var phoneBook: List<PhoneBookEntity> = listOf()

    private var onItemClickListener: OnItemClickListener? = null


    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    fun clearOnItemClickListener() {
        onItemClickListener = null
    }

    fun setData(phoneBook: List<PhoneBookEntity>) {
        this.phoneBook = phoneBook
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneBookRecyclerViewHolder {
        return PhoneBookRecyclerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.phones_book_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PhoneBookRecyclerViewHolder, position: Int) {
        holder.bind(phoneBook[position])
    }

    override fun getItemCount(): Int {
        return phoneBook.size
    }

    interface OnItemClickListener {
        fun onClick(phoneNumber: String?)
    }

    inner class PhoneBookRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(phoneBookEntity: PhoneBookEntity) {
            itemView.findViewById<TextView>(R.id.phone_book_contact_name).text = phoneBookEntity.contactName
            itemView.findViewById<TextView>(R.id.phone_book_phone_number).text = phoneBookEntity.phoneNumber
            itemView.setOnClickListener { onItemClickListener?.onClick(phoneBookEntity.phoneNumber) }
        }
    }
}
