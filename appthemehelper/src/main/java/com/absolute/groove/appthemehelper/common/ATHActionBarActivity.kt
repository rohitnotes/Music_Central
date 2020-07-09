package com.absolute.groove.appthemehelper.common

import androidx.appcompat.widget.Toolbar

import com.absolute.groove.appthemehelper.util.ToolbarContentTintHelper

class ATHActionBarActivity : ATHToolbarActivity() {

    override fun getATHToolbar(): Toolbar? {
        return ToolbarContentTintHelper.getSupportActionBarView(supportActionBar)
    }
}
