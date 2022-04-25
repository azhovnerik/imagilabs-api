package com.anahoret.imagilabsapi.acceptance

import com.anahoret.imagilabsapi.acceptance.features.CucumberSpringTest
import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["src/test/resources/features"],
    plugin = ["pretty"]
)
class CucumberIntegrationTest : CucumberSpringTest()
