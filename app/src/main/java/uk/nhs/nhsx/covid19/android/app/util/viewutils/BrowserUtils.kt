/*
 * Copyright © 2020 NHSX. All rights reserved.
 */

package uk.nhs.nhsx.covid19.android.app.util.viewutils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import com.jeroenmols.featureflag.framework.RuntimeBehavior
import com.jeroenmols.featureflag.framework.TestSetting
import timber.log.Timber
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.browser.BrowserActivity

fun Activity.openUrl(@StringRes urlStringId: Int, useInternalBrowser: Boolean = true) {
    val url = getString(urlStringId)
    openUrl(url, useInternalBrowser)
}

fun Activity.openUrl(url: String, useInternalBrowser: Boolean = true) {
    try {
        if (useInternalBrowser) openInInternalBrowser(url)
        else openInExternalBrowserForResult(url)
    } catch (t: Throwable) {
        Timber.e(t, "Error opening url: $url")
    }
}

private fun Activity.openInInternalBrowser(url: String) {
    if (RuntimeBehavior.isFeatureEnabled(TestSetting.USE_WEB_VIEW_FOR_INTERNAL_BROWSER)) {
        BrowserActivity.start(this, url)
    } else {
        CustomTabsIntent.Builder()
            .addDefaultShareMenuItem()
            .setToolbarColor(getColor(R.color.links_toolbar_color))
            .build()
            .launchUrl(this, Uri.parse(url))
    }
}

const val REQUEST_WEB_BROWSER = 10001

fun Activity.openInExternalBrowserForResult(url: String, requestId: Int = REQUEST_WEB_BROWSER) {
    if (RuntimeBehavior.isFeatureEnabled(TestSetting.USE_WEB_VIEW_FOR_EXTERNAL_BROWSER)) {
        BrowserActivity.startForResult(this, url, requestId)
    } else {
        startActivityForResult(Intent(Intent.ACTION_VIEW, Uri.parse(url)), requestId)
    }
}
