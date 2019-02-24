package com.github.dlenroc.restassured.testrail.logger

import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.internal.support.Prettifier
import io.restassured.parsing.Parser.fromContentType
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import org.junit.Test
import java.io.File
import java.lang.reflect.Method

class TestRailFilter : Filter {
    private val requests = mutableListOf<Request>()
    private var name: String? = null

    class Request(val request: FilterableRequestSpecification, val response: io.restassured.response.Response)

    override fun filter(requestSpec: FilterableRequestSpecification, responseSpec: FilterableResponseSpecification, ctx: FilterContext): io.restassured.response.Response {
        val response = ctx.next(requestSpec, responseSpec)
        val method = getCurrentTestMethod()
        if (method != null)
            name = "${method.declaringClass.name}#${method.name}"

        requests.add(Request(requestSpec, response))

        return response
    }

    private fun responseFormat(responseSpec: io.restassured.response.Response): String {
        var response = ""

        response += "**Response code:** ${responseSpec.statusLine.drop(8)}\n"
        if (responseSpec.body != null && responseSpec.body.asString().isNotBlank()) {
            response += "**Response body:**\n\n"
            response += Prettifier().prettify(responseSpec.body.asString(), fromContentType(responseSpec.contentType))
                    .replace(Regex("^", RegexOption.MULTILINE), "    ")
        }

        return response
    }

    private fun requestFormat(requestSpec: FilterableRequestSpecification): String {
        var request = ""

        request += "Perform a ${requestSpec.method} request to ${requestSpec.userDefinedPath} with the following parameters:"
        request += "\n    - **Headers:**\n"
        request += requestSpec.headers.filter { it.name != "Accept" }.joinToString("\n") {
            "        - ${it.name}: ${it.value}"
        }

        if (requestSpec.pathParams.isNotEmpty()) {
            request += "\n    - **Path parameters:**\n"
            request += requestSpec.pathParams.entries.joinToString("\n") {
                "        - ${it.key}: ${it.value}"
            }
        }

        if (requestSpec.queryParams.isNotEmpty()) {
            request += "\n    - **Query parameters:**\n"
            request += requestSpec.queryParams.entries.joinToString("\n") {
                "        - ${it.key}: ${it.value}"
            }
        }

        if (requestSpec.formParams.isNotEmpty()) {
            request += "\n    - **Form parameters:**\n"
            request += requestSpec.formParams.entries.joinToString("\n") {
                "        - ${it.key}: ${it.value}"
            }
        }

        if (requestSpec.requestParams.isNotEmpty()) {
            request += "\n    - **Request parameters:**\n"
            request += requestSpec.requestParams.entries.joinToString("\n") {
                "        - ${it.key}: ${it.value}"
            }
        }

        val body = requestSpec.getBody<Any?>()
        if (body != null && body.toString().isNotBlank()) {
            request += "\n    - **Body:**\n\n"
            request += Prettifier().prettify(body.toString(), fromContentType(requestSpec.contentType))
                    .replace(Regex("^", RegexOption.MULTILINE), "            ") + "\n"
        }

        return request
    }

    private fun getCurrentTestMethod(): Method? {
        return Thread.currentThread().stackTrace.asSequence().map { stackTraceElement ->
            try {
                Class.forName(stackTraceElement.className).declaredMethods.firstOrNull {
                    it.name == stackTraceElement.methodName
                }
            } catch (e: Throwable) {
                null
            }
        }.firstOrNull {
            it?.isAnnotationPresent(Test::class.java) ?: false
        }
    }

    fun save() {
        if (requests.isEmpty())
            return

        File("target/testrail/$name.txt").apply {
            if (exists())
                delete()

            parentFile.mkdirs()

            appendText(requests.withIndex().joinToString("\n") {
                "${it.index + 1}. ${requestFormat(it.value.request)}"
            })

            appendText("\n\n")

            appendText(responseFormat(requests.last().response))

            requests.clear()
        }
    }
}