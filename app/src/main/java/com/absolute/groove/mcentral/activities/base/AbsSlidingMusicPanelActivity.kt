package com.absolute.groove.mcentral.activities.base

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.absolute.groove.appthemehelper.util.ATHUtil
import com.absolute.groove.appthemehelper.util.ColorUtil
import com.absolute.groove.mcentral.R
import com.absolute.groove.mcentral.RetroBottomSheetBehavior
import com.absolute.groove.mcentral.extensions.hide
import com.absolute.groove.mcentral.extensions.show
import com.absolute.groove.mcentral.fragments.MiniPlayerFragment
import com.absolute.groove.mcentral.fragments.NowPlayingScreen
import com.absolute.groove.mcentral.fragments.NowPlayingScreen.*
import com.absolute.groove.mcentral.fragments.base.AbsPlayerFragment
import com.absolute.groove.mcentral.fragments.player.adaptive.AdaptiveFragment
import com.absolute.groove.mcentral.fragments.player.blur.BlurPlayerFragment
import com.absolute.groove.mcentral.fragments.player.card.CardFragment
import com.absolute.groove.mcentral.fragments.player.cardblur.CardBlurFragment
import com.absolute.groove.mcentral.fragments.player.circle.CirclePlayerFragment
import com.absolute.groove.mcentral.fragments.player.classic.ClassicPlayerFragment
import com.absolute.groove.mcentral.fragments.player.color.ColorFragment
import com.absolute.groove.mcentral.fragments.player.fit.FitFragment
import com.absolute.groove.mcentral.fragments.player.flat.FlatPlayerFragment
import com.absolute.groove.mcentral.fragments.player.full.FullPlayerFragment
import com.absolute.groove.mcentral.fragments.player.gradient.GradientPlayerFragment
import com.absolute.groove.mcentral.fragments.player.material.MaterialFragment
import com.absolute.groove.mcentral.fragments.player.normal.PlayerFragment
import com.absolute.groove.mcentral.fragments.player.peak.PeakPlayerFragment
import com.absolute.groove.mcentral.fragments.player.plain.PlainPlayerFragment
import com.absolute.groove.mcentral.fragments.player.simple.SimplePlayerFragment
import com.absolute.groove.mcentral.fragments.player.tiny.TinyPlayerFragment
import com.absolute.groove.mcentral.helper.MusicPlayerRemote
import com.absolute.groove.mcentral.model.CategoryInfo
import com.absolute.groove.mcentral.util.DensityUtil
import com.absolute.groove.mcentral.util.PreferenceUtilKT
import com.absolute.groove.mcentral.views.BottomNavigationBarTinted
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.android.synthetic.main.sliding_music_panel_layout.*

abstract class AbsSlidingMusicPanelActivity : AbsMusicServiceActivity(),
    AbsPlayerFragment.Callbacks {
    companion object {
        val TAG: String = AbsSlidingMusicPanelActivity::class.java.simpleName
    }

    private lateinit var behavior: RetroBottomSheetBehavior<FrameLayout>
    private var miniPlayerFragment: MiniPlayerFragment? = null
    private var playerFragment: AbsPlayerFragment? = null
    private var cps: NowPlayingScreen? = null
    private var navigationBarColor: Int = 0
    private var taskColor: Int = 0
    private var lightStatusBar: Boolean = false
    private var lightNavigationBar: Boolean = false
    private var navigationBarColorAnimator: ValueAnimator? = null
    protected abstract fun createContentView(): View
    private lateinit var shapeDrawable: MaterialShapeDrawable
    private val panelState: Int
        get() = behavior.state

    private val bottomSheetCallbackList = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            setMiniPlayerAlphaProgress(slideOffset)
            dimBackground.show()
            dimBackground.alpha = slideOffset
            shapeDrawable.interpolation = 1 - slideOffset
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    onPanelExpanded()
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    onPanelCollapsed()
                    dimBackground.hide()
                }
                else -> {

                }
            }
        }
    }

    fun getBottomSheetBehavior() = behavior

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createContentView())
        chooseFragmentForTheme()
        setupSlidingUpPanel()



        behavior = BottomSheetBehavior.from(slidingPanel) as RetroBottomSheetBehavior

        val themeColor = ATHUtil.resolveColor(this, android.R.attr.windowBackground, Color.GRAY)
        dimBackground.setBackgroundColor(ColorUtil.withAlpha(themeColor, 0.5f))
        shapeDrawable = MaterialShapeDrawable(
            ShapeAppearanceModel.builder(
                this,
                R.style.ClassicThemeOverLay,
                0
            ).build()
        )
        slidingPanel.background = shapeDrawable
    }

    override fun onResume() {
        super.onResume()
        if (cps != PreferenceUtilKT.nowPlayingScreen) {
            postRecreate()
        }
        behavior.addBottomSheetCallback(bottomSheetCallbackList)

        if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            setMiniPlayerAlphaProgress(1f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        behavior.removeBottomSheetCallback(bottomSheetCallbackList)
        if (navigationBarColorAnimator != null) navigationBarColorAnimator?.cancel() // just in case
    }

    protected fun wrapSlidingMusicPanel(@LayoutRes resId: Int): View {
        val slidingMusicPanelLayout =
            layoutInflater.inflate(R.layout.sliding_music_panel_layout, null)
        val contentContainer =
            slidingMusicPanelLayout.findViewById<ViewGroup>(R.id.mainContentFrame)
        layoutInflater.inflate(resId, contentContainer)
        return slidingMusicPanelLayout
    }

    private fun collapsePanel() {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun expandPanel() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        setMiniPlayerAlphaProgress(1f)
    }

    private fun setMiniPlayerAlphaProgress(progress: Float) {
        if (miniPlayerFragment?.view == null) return
        val alpha = 1 - progress
        miniPlayerFragment?.view?.alpha = alpha
        // necessary to make the views below clickable
        miniPlayerFragment?.view?.visibility = if (alpha == 0f) View.GONE else View.VISIBLE

        bottomNavigationView.translationY = progress * 500
        //bottomNavigationView.alpha = alpha
    }

    open fun onPanelCollapsed() {
        // restore values
        super.setLightStatusbar(lightStatusBar)
        super.setTaskDescriptionColor(taskColor)
        super.setNavigationbarColor(navigationBarColor)
        super.setLightNavigationBar(lightNavigationBar)


        playerFragment?.setMenuVisibility(false)
        playerFragment?.userVisibleHint = false
        playerFragment?.onHide()
    }

    open fun onPanelExpanded() {
        val playerFragmentColor = playerFragment!!.paletteColor
        super.setTaskDescriptionColor(playerFragmentColor)

        playerFragment?.setMenuVisibility(true)
        playerFragment?.userVisibleHint = true
        playerFragment?.onShow()
        onPaletteColorChanged()
    }

    private fun setupSlidingUpPanel() {
        slidingPanel.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                slidingPanel.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (cps != Peak) {
                    val params = slidingPanel.layoutParams as ViewGroup.LayoutParams
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    slidingPanel.layoutParams = params
                }
                when (panelState) {
                    BottomSheetBehavior.STATE_EXPANDED -> onPanelExpanded()
                    BottomSheetBehavior.STATE_COLLAPSED -> onPanelCollapsed()
                    else -> playerFragment!!.onHide()
                }
            }
        })
    }

    fun getBottomNavigationView(): BottomNavigationBarTinted {
        return bottomNavigationView
    }

    fun setBottomBarVisibility(visible: Int) {
        bottomNavigationView.visibility = visible
        hideBottomBar(MusicPlayerRemote.playingQueue.isEmpty())
    }

    private fun hideBottomBar(hide: Boolean) {
        val heightOfBar = resources.getDimensionPixelSize(R.dimen.mini_player_height)
        val heightOfBarWithTabs =
            resources.getDimensionPixelSize(R.dimen.mini_player_height_expanded)

        if (hide) {
            behavior.isHideable = true
            behavior.peekHeight = 0
            bottomNavigationView.elevation = DensityUtil.dip2px(this, 10f).toFloat()
            collapsePanel()
        } else {
            if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                slidingPanel.elevation = DensityUtil.dip2px(this, 10f).toFloat()
                bottomNavigationView.elevation = DensityUtil.dip2px(this, 10f).toFloat()
                behavior.isHideable = false
                behavior.peekHeight =
                    if (bottomNavigationView.visibility == View.VISIBLE) {
                        heightOfBarWithTabs
                    } else {
                        heightOfBar
                    }
            }
        }
    }

    private fun chooseFragmentForTheme() {
        cps = PreferenceUtilKT.nowPlayingScreen
        val fragment: Fragment = when (cps) {
            Blur -> BlurPlayerFragment()
            Adaptive -> AdaptiveFragment()
            Normal -> PlayerFragment()
            Card -> CardFragment()
            BlurCard -> CardBlurFragment()
            Fit -> FitFragment()
            Flat -> FlatPlayerFragment()
            Full -> FullPlayerFragment()
            Plain -> PlainPlayerFragment()
            Simple -> SimplePlayerFragment()
            Material -> MaterialFragment()
            Color -> ColorFragment()
            Tiny -> TinyPlayerFragment()
            Peak -> PeakPlayerFragment()
            Circle -> CirclePlayerFragment()
            Classic -> ClassicPlayerFragment()
            Gradient -> GradientPlayerFragment()
            else -> PlayerFragment()
        } // must implement AbsPlayerFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.playerFragmentContainer, fragment)
            .commit()
        supportFragmentManager.executePendingTransactions()

        playerFragment =
            supportFragmentManager.findFragmentById(R.id.playerFragmentContainer) as AbsPlayerFragment
        miniPlayerFragment =
            supportFragmentManager.findFragmentById(R.id.miniPlayerFragment) as MiniPlayerFragment
        miniPlayerFragment?.view?.setOnClickListener { expandPanel() }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
            slidingPanel.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    slidingPanel.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    hideBottomBar(false)
                }
            })
        } // don't call hideBottomBar(true) here as it causes a bug with the SlidingUpPanelLayout
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        hideBottomBar(MusicPlayerRemote.playingQueue.isEmpty())
    }

    override fun onBackPressed() {
        if (!handleBackPress()) super.onBackPressed()
    }

    open fun handleBackPress(): Boolean {
        if (behavior.peekHeight != 0 && playerFragment!!.onBackPressed()) return true
        if (panelState == BottomSheetBehavior.STATE_EXPANDED) {
            collapsePanel()
            return true
        }
        return false
    }

    override fun onPaletteColorChanged() {
        if (panelState == BottomSheetBehavior.STATE_EXPANDED) {
            val paletteColor = playerFragment!!.paletteColor
            super.setTaskDescriptionColor(paletteColor)

            val isColorLight = ColorUtil.isColorLight(paletteColor)

            if (PreferenceUtilKT.isAdaptiveColor && (cps == Normal || cps == Flat)) {
                super.setLightNavigationBar(true)
                super.setLightStatusbar(isColorLight)
            } else if (cps == Card || cps == Blur || cps == BlurCard) {
                super.setLightStatusbar(false)
                super.setLightNavigationBar(true)
                super.setNavigationbarColor(Color.BLACK)
            } else if (cps == Color || cps == Tiny || cps == Gradient) {
                super.setNavigationbarColor(paletteColor)
                super.setLightNavigationBar(isColorLight)
                super.setLightStatusbar(isColorLight)
            } else if (cps == Full) {
                super.setNavigationbarColor(paletteColor)
                super.setLightNavigationBar(isColorLight)
                super.setLightStatusbar(false)
            } else if (cps == Classic) {
                super.setLightStatusbar(false)
            } else if (cps == Fit) {
                super.setLightStatusbar(false)
            } else {
                super.setLightStatusbar(
                    ColorUtil.isColorLight(
                        ATHUtil.resolveColor(
                            this,
                            android.R.attr.windowBackground
                        )
                    )
                )
                super.setLightNavigationBar(true)
            }
        }
    }

    override fun setLightStatusbar(enabled: Boolean) {
        lightStatusBar = enabled
        if (panelState == BottomSheetBehavior.STATE_COLLAPSED) {
            super.setLightStatusbar(enabled)
        }
    }

    override fun setLightNavigationBar(enabled: Boolean) {
        lightNavigationBar = enabled
        if (panelState == BottomSheetBehavior.STATE_COLLAPSED) {
            super.setLightNavigationBar(enabled)
        }
    }

    override fun setNavigationbarColor(color: Int) {
        navigationBarColor = color
        if (panelState == BottomSheetBehavior.STATE_COLLAPSED) {
            if (navigationBarColorAnimator != null) navigationBarColorAnimator!!.cancel()
            super.setNavigationbarColor(color)
        }
    }

    override fun setTaskDescriptionColor(color: Int) {
        taskColor = color
        if (panelState == BottomSheetBehavior.STATE_COLLAPSED) {
            super.setTaskDescriptionColor(color)
        }
    }

    fun updateTabs() {
        bottomNavigationView.menu.clear()
        val currentTabs: List<CategoryInfo> = PreferenceUtilKT.libraryCategory
        for (tab in currentTabs) {
            if (tab.visible) {
                val menu = tab.category
                bottomNavigationView.menu.add(0, menu.id, 0, menu.stringRes).setIcon(menu.icon)
            }
        }
        if (bottomNavigationView.menu.size() == 1) {
            bottomNavigationView.hide()
        }
    }
}