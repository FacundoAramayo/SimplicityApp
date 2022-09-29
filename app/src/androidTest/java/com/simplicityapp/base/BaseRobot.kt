package com.simplicityapp.base

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

abstract class BaseRobot {

    fun checkIsVisible(@IdRes viewId: Int): BaseRobot {
        onView(viewId)
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        return this
    }

    fun checkIsHidden(@IdRes viewId: Int): BaseRobot {
        onView(viewId)
            .check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
        return this
    }

    fun checkViewHasText(@IdRes viewId: Int, @StringRes stringId: Int): BaseRobot {
        onView(viewId)
            .check(ViewAssertions.matches(ViewMatchers.withText(stringId)))
        return this
    }

    fun checkViewHasText(@IdRes viewId: Int, text: String): BaseRobot {
        onView(viewId)
            .check(ViewAssertions.matches(ViewMatchers.withText(text)))
        return this
    }

    fun checkIsSelected(@IdRes viewId: Int): BaseRobot {
        onView(viewId)
            .check(ViewAssertions.matches(ViewMatchers.isSelected()))
        return this
    }

    fun clickOnView(@IdRes viewId:Int): BaseRobot {
        onView(viewId)
            .perform(ViewActions.click())
        return this
    }

    fun insertTextToView(@IdRes viewId:Int, text: String): BaseRobot {
        onView(viewId)
            .perform(ViewActions.click())
            .perform(ViewActions.typeText(text))
            .perform(ViewActions.closeSoftKeyboard())
        return this
    }

    private fun onView(@IdRes viewId: Int): ViewInteraction {
        return Espresso.onView(ViewMatchers.withId(viewId))
    }
}
