package com.dev.heenasupplier.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CardSliderAdapter(fa: FragmentActivity, fragments: ArrayList<Fragment>) :
    FragmentStateAdapter(fa) {
    var fragments: ArrayList<Fragment> = fragments;
    override fun getItemCount(): Int {
        return this.fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return this.fragments.get(position)
    }


}