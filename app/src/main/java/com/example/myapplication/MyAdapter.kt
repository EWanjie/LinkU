package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(
    private var personList: List<Person>,
    private val listener: OnPersonClickListener
) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.person_view, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val person = personList[position]
        holder.nameView.text = person.fullName
        holder.onlineView.text = person.online
        holder.onlineView.setTextColor(
            ContextCompat.getColor(holder.imageView.context,
                if(person.online == "в сети") R.color.blue
            else R.color.gray)
        )

        holder.imageView.setImageResource(person.img)

        holder.itemView.setOnClickListener {
            listener.onPersonClick(person)
        }
    }
    interface OnPersonClickListener {
        fun onPersonClick(person: Person)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilterAdapter(filterList: List<Person>){
        this.personList = filterList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = personList.size

}