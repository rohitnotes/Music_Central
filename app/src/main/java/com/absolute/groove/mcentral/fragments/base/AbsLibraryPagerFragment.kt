package com.absolute.groove.mcentral.fragments.base

import android.os.Bundle
import com.absolute.groove.mcentral.activities.MainActivity

open class AbsLibraryPagerFragment : AbsMusicServiceFragment() {

    val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }
}
