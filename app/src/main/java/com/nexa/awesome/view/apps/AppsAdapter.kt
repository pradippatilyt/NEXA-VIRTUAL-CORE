package com.nexa.awesome.view.apps

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import com.nexa.awesome.R

import com.nexa.awesome.bean.AppInfo
import com.nexa.awesome.databinding.ItemAppBinding


class AppsAdapter : RVHolderFactory() {
    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return AppsVH(inflate(R.layout.item_app, parent))
    }

    class AppsVH(itemView: View) : RVHolder<AppInfo>(itemView) {
        private val binding = ItemAppBinding.bind(itemView)

        override fun setContent(item: AppInfo, isSelected: Boolean, payload: Any?) {
            binding.icon.setImageDrawable(item.icon)
            binding.name.text = item.name

            if (item.isXpModule) {
                binding.cornerLabel.visibility = View.VISIBLE
            } else {
                binding.cornerLabel.visibility = View.INVISIBLE
            }
        }
    }
}
