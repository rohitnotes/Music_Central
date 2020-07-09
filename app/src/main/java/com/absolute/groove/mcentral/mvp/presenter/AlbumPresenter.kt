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

package com.absolute.groove.mcentral.mvp.presenter

import com.absolute.groove.mcentral.Result.Error
import com.absolute.groove.mcentral.Result.Success
import com.absolute.groove.mcentral.model.Album
import com.absolute.groove.mcentral.mvp.BaseView
import com.absolute.groove.mcentral.mvp.Presenter
import com.absolute.groove.mcentral.mvp.PresenterImpl
import com.absolute.groove.mcentral.providers.interfaces.Repository
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by hemanths on 12/08/17.
 */
interface AlbumsView : BaseView {

    fun albums(albums: List<Album>)
}

interface AlbumsPresenter : Presenter<AlbumsView> {

    fun loadAlbums()

    class AlbumsPresenterImpl @Inject constructor(
        private val repository: Repository
    ) : PresenterImpl<AlbumsView>(), AlbumsPresenter, CoroutineScope {

        private val job = Job()

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        override fun detachView() {
            super.detachView()
            job.cancel()
        }

        override fun loadAlbums() {
            launch {
                when (val result = repository.allAlbums()) {
                    is Success -> withContext(Dispatchers.Main) { view?.albums(result.data) }
                    is Error -> withContext(Dispatchers.Main) { view?.showEmptyView() }
                }
            }
        }
    }
}