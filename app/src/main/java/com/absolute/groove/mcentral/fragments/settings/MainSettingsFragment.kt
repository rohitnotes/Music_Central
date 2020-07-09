/*
 * Copyright (c) 2019 Hemanth Savarala.
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

package com.absolute.groove.mcentral.fragments.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.absolute.groove.appthemehelper.ThemeStore
import com.absolute.groove.mcentral.App
import com.absolute.groove.mcentral.R
import com.absolute.groove.mcentral.activities.SettingsActivity
import com.absolute.groove.mcentral.extensions.hide
import com.absolute.groove.mcentral.extensions.show
import com.absolute.groove.mcentral.util.NavigationUtil
import kotlinx.android.synthetic.main.fragment_main_settings.*

class MainSettingsFragment : Fragment(), View.OnClickListener {
    override fun onClick(view: View) {
        when (view.id) {
            R.id.generalSettings -> inflateFragment(
                ThemeSettingsFragment(),
                R.string.general_settings_title
            )
            R.id.audioSettings -> inflateFragment(AudioSettings(), R.string.pref_header_audio)
            R.id.nowPlayingSettings -> inflateFragment(
                NowPlayingSettingsFragment(),
                R.string.now_playing
            )
            R.id.personalizeSettings -> inflateFragment(
                PersonalizeSettingsFragment(),
                R.string.personalize
            )
            R.id.imageSettings -> inflateFragment(
                ImageSettingFragment(),
                R.string.pref_header_images
            )
            R.id.notificationSettings -> inflateFragment(
                NotificationSettingsFragment(),
                R.string.notification
            )
            R.id.otherSettings -> inflateFragment(OtherSettingsFragment(), R.string.others)
            R.id.aboutSettings -> NavigationUtil.goToAbout(requireActivity())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generalSettings.setOnClickListener(this)
        audioSettings.setOnClickListener(this)
        nowPlayingSettings.setOnClickListener(this)
        personalizeSettings.setOnClickListener(this)
        imageSettings.setOnClickListener(this)
        notificationSettings.setOnClickListener(this)
        otherSettings.setOnClickListener(this)
        aboutSettings.setOnClickListener(this)

        buyProContainer.apply {
            if (!App.isProVersion()) show() else hide()
            setOnClickListener {
                NavigationUtil.goToProVersion(requireContext())
            }
        }
        buyPremium.setOnClickListener {
            NavigationUtil.goToProVersion(requireContext())
        }
        ThemeStore.accentColor(requireContext()).let {
            buyPremium.setTextColor(it)
            diamondIcon.imageTintList = ColorStateList.valueOf(it)
        }
    }

    companion object {

    }

    private fun inflateFragment(fragment: Fragment, @StringRes title: Int) {
        (requireActivity() as SettingsActivity).setupFragment(fragment, title)
    }
}