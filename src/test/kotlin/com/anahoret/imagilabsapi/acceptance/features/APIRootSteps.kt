package com.anahoret.imagilabsapi.acceptance.features

import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.hamcrest.Matchers.`is`
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get

class APIRootSteps(val mvc: MockMvc) {

    lateinit var get: ResultActionsDsl

    @When("^I navigate to API root with host header (.*)$")
    fun `I navigate to API root with host header`(hostHeader: String) {
        get = mvc.get("/") {
            header("Host", hostHeader)
        }
    }

    @Then("^Swagger docs URI in response equals to (.*)$")
    fun `I see welcome message`(swaggerDocsUri: String) {
        get.andExpect {
            jsonPath("\$.payload.documentation", `is`(swaggerDocsUri))
        }
    }

}
