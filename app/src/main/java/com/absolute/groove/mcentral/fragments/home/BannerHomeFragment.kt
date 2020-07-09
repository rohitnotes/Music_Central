/*
 * Copyright (c) 2020 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.absolute.groove.mcentral.fragments.home

import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.absolute.groove.mcentral.R
import com.absolute.groove.mcentral.adapter.HomeAdapter
import com.absolute.groove.mcentral.fragments.base.AbsMainActivityFragment
import com.absolute.groove.mcentral.glide.ProfileBannerGlideRequest
import com.absolute.groove.mcentral.glide.UserProfileGlideRequest
import com.absolute.groove.mcentral.helper.MusicPlayerRemote
import com.absolute.groove.mcentral.interfaces.MainActivityFragmentCallbacks
import com.absolute.groove.mcentral.loaders.SongLoader
import com.absolute.groove.mcentral.model.smartplaylist.HistoryPlaylist
import com.absolute.groove.mcentral.model.smartplaylist.LastAddedPlaylist
import com.absolute.groove.mcentral.model.smartplaylist.MyTopTracksPlaylist
import com.absolute.groove.mcentral.util.NavigationUtil
import com.absolute.groove.mcentral.util.PreferenceUtilKT
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.abs_playlists.*
import kotlinx.android.synthetic.main.fragment_banner_home.*
import kotlinx.android.synthetic.main.home_content.*


class BannerHomeFragment : AbsMainActivityFragment(), MainActivityFragmentCallbacks {
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            if (PreferenceUtilKT.isHomeBanner) R.layout.fragment_banner_home else R.layout.fragment_home,
            viewGroup,
            false
        )
    }

    private fun loadImageFromStorage() {
        UserProfileGlideRequest.Builder.from(
            Glide.with(requireActivity()),
            UserProfileGlideRequest.getUserModel()
        ).build().into(userImage)
    }

    private val displayMetrics: DisplayMetrics
        get() {
            val display = mainActivity.windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            return metrics
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setStatusBarColorAuto(view)

        bannerImage?.setOnClickListener {
            val options = ActivityOptions.makeSceneTransitionAnimation(
                mainActivity,
                userImage,
                getString(R.string.transition_user_image)
            )
            NavigationUtil.goToUserInfo(requireActivity(), options)
        }
        empty_list.setOnClickListener(View.OnClickListener {
            val appPackageName: String =
                mainActivity.getPackageName() // getPackageName() from Context or Activity object

            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
        })
        lastAdded.setOnClickListener {
            NavigationUtil.goToPlaylistNew(requireActivity(), LastAddedPlaylist(requireActivity()))
        }

        topPlayed.setOnClickListener {
            NavigationUtil.goToPlaylistNew(
                requireActivity(),
                MyTopTracksPlaylist(requireActivity())
            )
        }

        actionShuffle.setOnClickListener {
            MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(requireActivity()), true)
        }

        history.setOnClickListener {
            NavigationUtil.goToPlaylistNew(requireActivity(), HistoryPlaylist(requireActivity()))
        }

        userImage.setOnClickListener {
            val options = ActivityOptions.makeSceneTransitionAnimation(
                mainActivity,
                userImage,
                getString(R.string.transition_user_image)
            )
            NavigationUtil.goToUserInfo(requireActivity(), options)
        }
        titleWelcome?.text =
            String.format("%s", PreferenceUtilKT.userName)

        homeAdapter = HomeAdapter(mainActivity, displayMetrics)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(mainActivity)
            adapter = homeAdapter
        }

        mainActivity.libraryViewModel.homeSections()
            .observe(viewLifecycleOwner, Observer { sections ->
                homeAdapter.swapData(sections)
            })

        loadProfile()
    }

    override fun handleBackPress(): Boolean {
        return false
    }

    private fun loadProfile() {
        bannerImage?.let {
            ProfileBannerGlideRequest.Builder.from(
                Glide.with(requireContext()),
                ProfileBannerGlideRequest.getBannerModel()
            ).build().into(it)
        }
        UserProfileGlideRequest.Builder.from(
            Glide.with(requireActivity()),
            UserProfileGlideRequest.getUserModel()
        ).build().into(userImage)
    }

    companion object {

        const val TAG: String = "BannerHomeFragment"

        @JvmStatic
        fun newInstance(): BannerHomeFragment {
            return BannerHomeFragment()
        }
    }
}