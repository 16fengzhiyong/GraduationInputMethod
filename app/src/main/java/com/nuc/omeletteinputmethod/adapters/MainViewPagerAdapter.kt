package com.nuc.omeletteinputmethod.adapters

import java.util.ArrayList

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * Created by zlk on 2017/7/24.
 */

class MainViewPagerAdapter(fm: FragmentManager, fragmentList: List<Fragment>) : FragmentStatePagerAdapter(fm) {


    internal var fragmentList: List<Fragment> = ArrayList()


    init {
        this.fragmentList = fragmentList
    }


    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

}
