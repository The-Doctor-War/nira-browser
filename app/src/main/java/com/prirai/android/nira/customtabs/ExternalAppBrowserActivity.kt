package com.prirai.android.nira.customtabs

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.navigation.fragment.NavHostFragment
import mozilla.components.browser.state.action.CustomTabListAction
import mozilla.components.browser.state.selector.findCustomTab
import mozilla.components.browser.state.state.createCustomTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.feature.intent.ext.EXTRA_SESSION_ID
import mozilla.components.support.utils.SafeIntent
import com.prirai.android.nira.BrowserActivity
import com.prirai.android.nira.R
import com.prirai.android.nira.ext.components
import com.prirai.android.nira.ext.getIntentSessionId

/**
 * Activity that holds the [ExternalAppBrowserFragment] that is launched within an external app,
 * such as custom tabs.
 */
open class ExternalAppBrowserActivity : BrowserActivity() {

    override fun navigateToBrowserOnColdStart() {
        // Do nothing - custom tabs handle their own navigation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Create a custom tab session from the intent
        val safeIntent = mozilla.components.support.utils.SafeIntent(intent)
        val url = safeIntent.dataString
        
        if (url != null) {
            val customTab = createCustomTab(
                url = url,
                source = SessionState.Source.Internal.CustomTab
            )
            components.store.dispatch(CustomTabListAction.AddCustomTabAction(customTab))
            intent.putExtra("CUSTOM_TAB_ID", customTab.id)
        }
        
        super.onCreate(savedInstanceState)
        hasCalledOnCreate = true
        
        // Navigate to the external app browser fragment
        if (savedInstanceState == null) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as? NavHostFragment
            navHostFragment?.let { host ->
                val customTabId = intent.getStringExtra("CUSTOM_TAB_ID")
                val bundle = Bundle().apply {
                    putString("activeSessionId", customTabId)
                    putString(EXTRA_SESSION_ID, customTabId)
                }
                host.navController.navigate(R.id.externalAppBrowserFragment, bundle)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        
        // For custom tabs, the session is created by the fragment when it processes the intent
        // So we don't check for hasExternalTab() here - let the fragment handle it
    }
    
    private var hasCalledOnCreate = false

    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing) {
            // When this activity finishes, the process is staying around and the session still
            // exists then remove it now to free all its resources. Once this activity is finished
            // then there's no way to get back to it other than relaunching it.
            val tabId = getExternalTabId()
            val customTab = tabId?.let { components.store.state.findCustomTab(it) }
            if (tabId != null && customTab != null) {
                components.tabsUseCases.removeTab(tabId)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun hasExternalTab(): Boolean {
        return getExternalTab() != null
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getExternalTab(): SessionState? {
        val id = getExternalTabId() ?: return null
        return components.store.state.findCustomTab(id)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getExternalTabId(): String? {
        return getIntentSessionId(SafeIntent(intent))
    }
}
