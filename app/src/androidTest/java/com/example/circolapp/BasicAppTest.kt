package com.example.circolapp

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test di base per verificare la funzionalità essenziale dell'app
 * Questi test dovrebbero sempre passare indipendentemente dalla configurazione Firebase
 */
@RunWith(AndroidJUnit4::class)
class BasicAppTest {
    
    private val TAG = "BasicAppTest"
    
    @Test
    fun testApplicationContext() {
        try {
            val appContext = ApplicationProvider.getApplicationContext<Context>()
            assertEquals("com.example.circolapp", appContext.packageName)
            assertNotNull("Application context should not be null", appContext)
            Log.i(TAG, "Application context test passed")
        } catch (e: Exception) {
            Log.e(TAG, "Application context test failed", e)
            throw e
        }
    }
    
    @Test
    fun testResourcesAvailable() {
        try {
            val appContext = ApplicationProvider.getApplicationContext<Context>()
            val resources = appContext.resources
            assertNotNull("Resources should not be null", resources)
            
            // Test some basic resource access
            val displayMetrics = resources.displayMetrics
            assertNotNull("Display metrics should be available", displayMetrics)
            assertTrue("Display density should be positive", displayMetrics.density > 0)
            
            Log.i(TAG, "Resources availability test passed")
        } catch (e: Exception) {
            Log.e(TAG, "Resources availability test failed", e)
            throw e
        }
    }
    
    @Test
    fun testAppName() {
        try {
            val appContext = ApplicationProvider.getApplicationContext<Context>()
            val packageManager = appContext.packageManager
            val appInfo = packageManager.getApplicationInfo(appContext.packageName, 0)
            assertNotNull("Application info should not be null", appInfo)
            
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            assertNotNull("App name should not be null", appName)
            assertFalse("App name should not be empty", appName.isEmpty())
            
            Log.i(TAG, "App name test passed: $appName")
        } catch (e: Exception) {
            Log.e(TAG, "App name test failed", e)
            throw e
        }
    }
}