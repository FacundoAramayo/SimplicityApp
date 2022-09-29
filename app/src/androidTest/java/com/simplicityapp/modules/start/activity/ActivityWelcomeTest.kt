package com.simplicityapp.modules.start.activity

import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simplicityapp.robot.WelcomeRobot
import com.simplicityapp.modules.start.*
import com.simplicityapp.modules.start.activity.ActivityWelcome
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityWelcomeTest {

    @get:Rule
    val scenarioRule: ActivityScenarioRule<ActivityWelcome> = ActivityScenarioRule(
        ActivityWelcome::class.java
    )

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun finish() {
        Intents.release()
    }

    @Test
    fun verifyWelcomeScreenIsVisible() {
        WelcomeRobot()
            .launch()
            .verifyTitle("¡Hola!")
    }

    @Test
    fun verifySecondScreenIsShownCorrectly() {
        WelcomeRobot()
            .launch()
    }

    @Test
    fun verifyWelcomeNavigationWorks() {
        WelcomeRobot()
            .launch()
            .next()
            .next()
            .back()
            .back()
            .next()
            .next()
            .start()
    }
}
