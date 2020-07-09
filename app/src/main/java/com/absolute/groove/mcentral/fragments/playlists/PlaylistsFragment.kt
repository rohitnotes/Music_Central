package com.absolute.groove.mcentral.fragments.playlists

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.absolute.groove.mcentral.R
import com.absolute.groove.mcentral.adapter.playlist.PlaylistAdapter
import com.absolute.groove.mcentral.fragments.base.AbsLibraryPagerRecyclerViewFragment
import com.absolute.groove.mcentral.interfaces.MainActivityFragmentCallbacks

class PlaylistsFragment :
    AbsLibraryPagerRecyclerViewFragment<PlaylistAdapter, GridLayoutManager>(),
    MainActivityFragmentCallbacks {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.libraryViewModel.allPlaylisits()
            .observe(viewLifecycleOwner, Observer { playlists ->
                if (playlists.isNotEmpty()) {
                    adapter?.swapDataSet(playlists)
                } else {
                    adapter?.swapDataSet(listOf())
                }
            })
    }

    override fun handleBackPress(): Boolean {
        return false
    }

    override val emptyMessage: Int
        get() = R.string.no_playlists

    override fun createLayoutManager(): GridLayoutManager {
        return GridLayoutManager(requireContext(), 1)
    }

    override fun createAdapter(): PlaylistAdapter {
        return PlaylistAdapter(
            mainActivity,
            ArrayList(),
            R.layout.item_list,
            mainActivity
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.apply {
            removeItem(R.id.action_sort_order)
            removeItem(R.id.action_grid_size)
        }
    }

    companion object {
        @JvmField
        val TAG: String = PlaylistsFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(): PlaylistsFragment {
            return PlaylistsFragment()
        }
    }
}
