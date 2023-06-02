package com.lahsuak.apps.mytask.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.lahsuak.apps.mytask.MyTaskApp
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.databinding.CategoryItemBinding
import com.lahsuak.apps.mytask.model.Category

class CategoryAdapter(
    context: Context,
    categories: List<Category>
) : ArrayAdapter<Category>(context, R.layout.category_item, categories) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val binding =
            CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // It is used to set our custom view.
        val currentItem = getItem(position)
        // It is used the name to the TextView when the
        // current item is not null.
        if (currentItem != null) {
            binding.txtName.text = currentItem.name
            val color = MyTaskApp.categoryTypes[currentItem.order].color
            binding.imgCategory.setBackgroundColor(color)
        }
        return binding.root
    }
}
