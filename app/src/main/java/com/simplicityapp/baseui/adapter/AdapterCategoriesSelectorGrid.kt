package com.simplicityapp.baseui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.simplicityapp.baseui.utils.UITools
import com.simplicityapp.modules.categories.model.Category
import com.simplicityapp.R

class AdapterCategoriesSelectorGrid(
    private val categories: List<Category>,
    private val listener: (Category) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        Log.d("LOG-", "onCreateViewHolder")
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("LOG-", "onBindViewHolder")
        val category: Category = categories[position]
        val categoryViewHolder = holder as CategoryViewHolder
        holder.bindView(category)
        holder.itemView.setOnClickListener {
            listener(category) }
    }

    override fun getItemCount(): Int = categories.size

}

class CategoryViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    private var layoutParent: View? = null
    private var background: ImageView? = null

    init {
        Log.d("LOG-", "init")
        layoutParent = itemView.findViewById(R.id.lyt_parent)
        background = itemView.findViewById(R.id.image)
    }

    fun bindView(category: Category) {
        background?.let {
            Log.d("LOG-", "load image")
            UITools.displayImage(itemView.context, it, category.backgroundResource) }
    }
}