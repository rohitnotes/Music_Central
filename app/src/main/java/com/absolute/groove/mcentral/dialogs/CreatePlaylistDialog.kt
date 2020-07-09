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

package com.absolute.groove.mcentral.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.absolute.groove.appthemehelper.util.MaterialUtil
import com.absolute.groove.mcentral.EXTRA_SONG
import com.absolute.groove.mcentral.R
import com.absolute.groove.mcentral.extensions.extraNotNull
import com.absolute.groove.mcentral.model.Song
import com.absolute.groove.mcentral.util.PlaylistsUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.dialog_playlist.view.*

class CreatePlaylistDialog : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        val view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_playlist, null)
        val playlistView: TextInputEditText = view.actionNewPlaylist
        val playlistContainer: TextInputLayout = view.actionNewPlaylistContainer
        MaterialUtil.setTint(playlistContainer, false)

        return MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
        )
            .setTitle(R.string.new_playlist_title)
            .setView(view)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(
                R.string.create_action
            ) { _, _ ->
                val extra = extraNotNull<ArrayList<Song>>(EXTRA_SONG)
                val playlistName = playlistView.text.toString()
                if (!TextUtils.isEmpty(playlistName)) {
                    val playlistId = PlaylistsUtil.createPlaylist(
                        requireContext(),
                        playlistView.text.toString()
                    )
                    if (playlistId != -1) {
                        PlaylistsUtil.addToPlaylist(requireContext(), extra.value, playlistId, true)
                    }
                }
            }
            .create()
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun create(song: Song? = null): CreatePlaylistDialog {
            val list = ArrayList<Song>()
            if (song != null) {
                list.add(song)
            }
            return create(list)
        }

        @JvmStatic
        fun create(songs: ArrayList<Song>): CreatePlaylistDialog {
            val dialog = CreatePlaylistDialog()
            val args = Bundle()
            args.putParcelableArrayList(EXTRA_SONG, songs)
            dialog.arguments = args
            return dialog
        }
    }
}