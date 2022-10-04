package com.simplicityapp.flow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.simplicityapp.modules.start.activity.ActivityLogin
import com.simplicityapp.modules.start.activity.ActivityWelcome
import org.junit.After
import org.junit.Test

class OnboardingFlowTest {

    lateinit var scenario: ActivityScenario<AppCompatActivity>

    @After
    fun cleanup() {
        scenario.close()
    }

    @Test
    fun openWelcomeActivity() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ActivityWelcome::class.java)
            .putExtra("title", "Testing params!")
        scenario = launchActivity(intent)

    }

    @Test
    fun openLoginActivity() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ActivityLogin::class.java)
            .putExtra("title", "Something different")
        scenario = launchActivity(intent)

    }
}