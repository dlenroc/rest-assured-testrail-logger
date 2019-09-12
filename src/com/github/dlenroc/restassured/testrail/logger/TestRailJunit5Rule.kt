package com.github.dlenroc.restassured.testrail.logger

import io.restassured.RestAssured.replaceFiltersWith
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext

class TestRailJunit5Rule : BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private val testRailFilter = TestRailFilter()

    override fun beforeTestExecution(context: ExtensionContext) {
        replaceFiltersWith(testRailFilter)
    }

    override fun afterTestExecution(context: ExtensionContext) {
        testRailFilter.save()
    }
}
