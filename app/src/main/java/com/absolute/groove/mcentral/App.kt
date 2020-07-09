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

package com.absolute.groove.mcentral

import android.widget.Toast
import androidx.multidex.MultiDexApplication
import com.absolute.groove.appthemehelper.ThemeStore
import com.absolute.groove.appthemehelper.util.VersionUtils
import com.absolute.groove.mcentral.appshortcuts.DynamicShortcutManager
import com.absolute.groove.mcentral.dagger.DaggerMusicComponent
import com.absolute.groove.mcentral.dagger.MusicComponent
import com.absolute.groove.mcentral.dagger.module.AppModule
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails

class App : MultiDexApplication() {

    lateinit var billingProcessor: BillingProcessor

    override fun onCreate() {
        super.onCreate()
        instance = this
        musicComponent = initDagger(this)

        // default theme
        if (!ThemeStore.isConfigured(this, 3)) {
            ThemeStore.editTheme(this)
                .accentColorRes(R.color.md_deep_purple_A200)
                .coloredNavigationBar(true)
                .commit()
        }

        if (VersionUtils.hasNougatMR())
            DynamicShortcutManager(this).initDynamicShortcuts()

        // automatically restores purchases
        billingProcessor = BillingProcessor(this, BuildConfig.GOOGLE_PLAY_LICENSING_KEY,
            object : BillingProcessor.IBillingHandler {
                override fun onProductPurchased(productId: String, details: TransactionDetails?) {}

                override fun onPurchaseHistoryRestored() {

                }

                override fun onBillingError(errorCode: Int, error: Throwable?) {}

                override fun onBillingInitialized() {}
            })
    }

    private fun initDagger(app: App): MusicComponent =
        DaggerMusicComponent.builder()
            .appModule(AppModule(app))
            .build()

    override fun onTerminate() {
        super.onTerminate()
        billingProcessor.release()
    }

    companion object {
        private var instance: App? = null

        fun getContext(): App {
            return instance!!
        }

        fun isProVersion(): Boolean {
            return BuildConfig.DEBUG || instance?.billingProcessor!!.isPurchased(
                PRO_VERSION_PRODUCT_ID
            )
        }

        lateinit var musicComponent: MusicComponent

        const val PRO_VERSION_PRODUCT_ID = "pro_version"
    }
}
