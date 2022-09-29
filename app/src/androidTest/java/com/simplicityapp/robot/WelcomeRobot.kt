package com.simplicityapp.robot

import com.simplicityapp.base.BaseRobot
import com.simplicityapp.R

class WelcomeRobot: BaseRobot() {

    fun launch(): WelcomeRobot {
        checkIsVisible(R.id.main_content)
        return this
    }

    fun next(): WelcomeRobot {
        checkIsVisible(R.id.btn_welcome_next)
            .checkViewHasText(R.id.btn_welcome_next, R.string.welcome_next)
            .clickOnView(R.id.btn_welcome_next)
        return this
    }

    fun back(): WelcomeRobot {
        checkViewHasText(R.id.btn_welcome_back, R.string.welcome_back)
            .clickOnView(R.id.btn_welcome_back)
        return this
    }

    fun start(): WelcomeRobot {
        checkIsVisible(R.id.btn_welcome_next)
            .checkViewHasText(R.id.btn_welcome_next, R.string.welcome_start)
            .clickOnView(R.id.btn_welcome_next)
        return this
    }

    fun verifyTitle(text: String): WelcomeRobot {
        checkIsVisible(R.id.tv_welcome_title_1)
            .checkViewHasText(R.id.tv_welcome_title_1, text)

        return this
    }

    fun verifySecondTitle(text: String): WelcomeRobot {
        checkIsVisible(R.id.tv_welcome_title_2)
            .checkViewHasText(R.id.tv_welcome_title_2, text)

        return this
    }

    fun verifyThirdTitle(text: String): WelcomeRobot {
        checkIsVisible(R.id.tv_welcome_title_3)
            .checkViewHasText(R.id.tv_welcome_title_3, text)

        return this
    }
}
