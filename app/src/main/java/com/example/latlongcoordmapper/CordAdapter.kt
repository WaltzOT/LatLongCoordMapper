package com.example.latlongcoordmapper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class CordAdapter : RecyclerView.Adapter<CordAdapter.CordViewHolder>(){
    val cords: MutableList<Cord> = mutableListOf()

    override fun getItemCount() = cords.size

    fun addCord(cord: Cord, index: Int = 0){
        cords.add(index,cord)
        notifyItemInserted(index)
    }

    fun deleteCord(position: Int): Cord{
        val cord = cords.removeAt(position)
        notifyItemRemoved(position)
        return cord
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cord_list_item, parent, false)
        return CordViewHolder(view)
    }

    override fun onBindViewHolder(holder: CordViewHolder, position: Int) {
        holder.bind(cords[position])
    }

    class CordViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val cordTextTV: TextView = view.findViewById(R.id.tv_cord_text)
        private var currentCord: Cord? = null

        fun bind(cord: Cord) {
            currentCord = cord
            cordTextTV.text = cord.text
        }
    }

}