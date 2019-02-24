package com.github.dlenroc.restassured.testrail.logger

import io.restassured.RestAssured.replaceFiltersWith
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class TestRailJunit4Rule : TestWatcher() {
    private val testRailFilter = TestRailFilter()

    override fun starting(description: Description) {
        replaceFiltersWith(testRailFilter)
    }

    override fun finished(description: Description) {
        testRailFilter.save()
    }
}