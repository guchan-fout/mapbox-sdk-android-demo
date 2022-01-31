package com.example.mapbox.demo

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater.from
import com.example.mapbox.demo.databinding.ActivityMainBinding
import com.example.mapbox.demo.databinding.RecycleviewItemBinding

class RecyclerAdapter(private val customList: Array<String>) :
    RecyclerView.Adapter<RecyclerAdapter.RecyclerAdapterViewHolder>() {

    private lateinit var listener: OnItemClickListener
    private lateinit var binding: RecycleviewItemBinding
    class RecyclerAdapterViewHolder(val binding: RecycleviewItemBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapterViewHolder {
        binding = RecycleviewItemBinding.inflate(from(parent.context),parent,false)

        return RecyclerAdapterViewHolder(
            binding
        )
    }

    override fun getItemCount(): Int {
        return customList.size
    }


    override fun onBindViewHolder(holder: RecyclerAdapterViewHolder, position: Int) {
        binding.itemName.text = customList[position]

        binding.root.setOnClickListener {
            listener.onItemClickListener(it, position, customList[position])
        }
    }

    interface OnItemClickListener {
        fun onItemClickListener(view: View, position: Int, clickedText: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

}