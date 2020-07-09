package com.absolute.groove.mcentral.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.ShareCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.absolute.groove.mcentral.App
import com.absolute.groove.mcentral.Constants.APP_INSTAGRAM_LINK
import com.absolute.groove.mcentral.Constants.APP_TELEGRAM_LINK
import com.absolute.groove.mcentral.Constants.APP_TWITTER_LINK
import com.absolute.groove.mcentral.Constants.FAQ_LINK
import com.absolute.groove.mcentral.Constants.GITHUB_PROJECT
import com.absolute.groove.mcentral.Constants.PINTEREST
import com.absolute.groove.mcentral.Constants.RATE_ON_GOOGLE_PLAY
import com.absolute.groove.mcentral.Constants.TELEGRAM_CHANGE_LOG
import com.absolute.groove.mcentral.Constants.TRANSLATE
import com.absolute.groove.mcentral.R
import com.absolute.groove.mcentral.activities.base.AbsBaseActivity
import com.absolute.groove.mcentral.adapter.ContributorAdapter
import com.absolute.groove.mcentral.extensions.applyToolbar
import com.absolute.groove.mcentral.model.Contributor
import com.absolute.groove.mcentral.util.NavigationUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.card_credit.*
import kotlinx.android.synthetic.main.card_other.*
import kotlinx.android.synthetic.main.card_retro_info.*
import kotlinx.android.synthetic.main.card_social.*
import java.io.IOException
import java.nio.charset.StandardCharsets

class AboutActivity : AbsBaseActivity(), View.OnClickListener {

    private val contributorsJson: String?
        get() {
            val json: String
            try {
                val inputStream = assets.open("contributors.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer, StandardCharsets.UTF_8)
            } catch (ex: IOException) {
                ex.printStackTrace()
                return null
            }
            return json
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setDrawUnderStatusBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setStatusbarColorAuto()
        setNavigationbarColorAuto()
        setLightNavigationBar(true)

        applyToolbar(toolbar)
        version.setSummary(getAppVersion())
        setUpView()
        loadContributors()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openUrl(url: String) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun setUpView() {
        appGithub.setOnClickListener(this)
        faqLink.setOnClickListener(this)
        telegramLink.setOnClickListener(this)
        appRate.setOnClickListener(this)
        appTranslation.setOnClickListener(this)
        appShare.setOnClickListener(this)
        donateLink.setOnClickListener(this)
        instagramLink.setOnClickListener(this)
        twitterLink.setOnClickListener(this)
        changelog.setOnClickListener(this)
        openSource.setOnClickListener(this)
        pinterestLink.setOnClickListener(this)
        bugReportLink.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.pinterestLink -> openUrl(PINTEREST)
            R.id.faqLink -> openUrl(FAQ_LINK)
            R.id.telegramLink -> openUrl(APP_TELEGRAM_LINK)
            R.id.appGithub -> openUrl(GITHUB_PROJECT)
            R.id.appTranslation -> openUrl(TRANSLATE)
            R.id.appRate -> openUrl(RATE_ON_GOOGLE_PLAY)
            R.id.appShare -> shareApp()
            R.id.donateLink -> NavigationUtil.goToSupportDevelopment(this)
            R.id.instagramLink -> openUrl(APP_INSTAGRAM_LINK)
            R.id.twitterLink -> openUrl(APP_TWITTER_LINK)
            R.id.changelog -> openUrl(TELEGRAM_CHANGE_LOG)
            R.id.openSource -> NavigationUtil.goToOpenSource(this)
            R.id.bugReportLink -> NavigationUtil.bugReport(this)
        }
    }

    private fun getAppVersion(): String {
        return try {
            val isPro = if (App.isProVersion()) "Pro" else "Free"
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            "${packageInfo.versionName} $isPro"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "0.0.0"
        }
    }

    private fun shareApp() {
        ShareCompat.IntentBuilder.from(this).setType("text/plain")
            .setChooserTitle(R.string.share_app)
            .setText(String.format(getString(R.string.app_share), packageName)).startChooser()
    }

    private fun loadContributors() {
        val type = object : TypeToken<List<Contributor>>() {

        }.type
        val contributors = Gson().fromJson<List<Contributor>>(contributorsJson, type)

        val contributorAdapter = ContributorAdapter(contributors)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = contributorAdapter
    }
}
