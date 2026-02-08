package com.nexa.awesome.view.list

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import com.nexa.awesome.R
import com.nexa.awesome.bean.InstalledAppBean
import com.nexa.awesome.databinding.ItemPackageBinding

class ListAdapter : RVHolderFactory() {
    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return ListVH(inflate(R.layout.item_package, parent))
    }

    class ListVH(itemView: View) : RVHolder<InstalledAppBean>(itemView) {
        private val binding = ItemPackageBinding.bind(itemView)

        override fun setContent(item: InstalledAppBean, isSelected: Boolean, payload: Any?) {
            binding.icon.setImageDrawable(item.icon)
            binding.name.text = item.name
            binding.packageName.text = item.packageName
            binding.cornerLabel.visibility = if (item.isInstall) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}
