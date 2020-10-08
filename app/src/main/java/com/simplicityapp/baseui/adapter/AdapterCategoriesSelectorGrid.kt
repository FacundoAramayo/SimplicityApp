package com.simplicityapp.baseui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simplicityapp.baseui.utils.UITools
import com.simplicityapp.modules.categories.model.Category
import com.simplicityapp.R
import com.simplicityapp.base.config.AppConfig.SHOW_CATEGORY_NAME
import com.simplicityapp.base.config.AppConfig.getWebURL
import com.simplicityapp.base.utils.hide
import com.simplicityapp.base.utils.show

class AdapterCategoriesSelectorGrid(
    private val categories: List<Category>,
    private val listener: (Category) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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
    private var title: TextView? = null

    init {
        layoutParent = itemView.findViewById(R.id.lyt_parent)
        background = itemView.findViewById(R.id.image)
        title = itemView.findViewById(R.id.textView_category_name)
    }

    fun bindView(category: Category) {
        background?.let {
            val imageUrl = getWebURL() + category.backgroundResource
            UITools.displayImage(itemView.context, it, imageUrl)
        }
        if (SHOW_CATEGORY_NAME) {
            title?.apply {
                show()
                text = category.title
            }
        } else {
            title?.hide()
        }
    }
}