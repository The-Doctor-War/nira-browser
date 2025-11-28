package com.prirai.android.nira

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.prirai.android.nira.customtabs.ExternalAppBrowserActivity
import com.prirai.android.nira.ext.components
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.feature.intent.processing.TabIntentProcessor
import mozilla.components.support.utils.SafeIntent

class IntentReceiverActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainScope().launch {
            val intent = intent?.let { Intent(it) } ?: Intent()
            val safeIntent = SafeIntent(intent)

            intent.flags = intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK.inv()
            intent.flags = intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK.inv()
            intent.flags = intent.flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS.inv()

            // Check if this is a custom tabs intent (external app link)
            // For external VIEW intents, open in custom tabs for faster loading
            val hasCustomTabExtra = safeIntent.hasExtra("android.support.customtabs.extra.SESSION") ||
                                   safeIntent.hasExtra("androidx.browser.customtabs.extra.SESSION")
            
            val isCustomTab = intent.action == Intent.ACTION_VIEW && 
                             intent.data != null &&
                             (hasCustomTabExtra ||
                              (intent.categories?.contains(Intent.CATEGORY_BROWSABLE) == true ||
                               intent.categories?.contains(Intent.CATEGORY_DEFAULT) == true)) &&
                             // Check if it has the OPEN_TO_BROWSER extra (internal navigation)
                             !intent.hasExtra(BrowserActivity.OPEN_TO_BROWSER)

            val activityClass = if (isCustomTab) {
                // For external links, use the ExternalAppBrowserActivity (custom tabs)
                ExternalAppBrowserActivity::class
            } else {
                // For regular intents, use the normal BrowserActivity
                // Process the intent to create a tab session
                val processor = TabIntentProcessor(components.tabsUseCases, components.searchUseCases.newTabSearch, isPrivate = false)
                processor.process(intent)
                BrowserActivity::class
            }

            intent.setClassName(applicationContext, activityClass.java.name)

            startActivity(intent)
            finish()
        }
    }
}