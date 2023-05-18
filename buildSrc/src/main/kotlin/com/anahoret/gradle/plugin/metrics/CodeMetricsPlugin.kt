package com.anahoret.gradle.plugin.metrics

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.the
import org.jsoup.Jsoup
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

abstract class CodeMetricsPluginExtension(project: Project) {

    companion object {

        const val NAME = "codeMetricsPluginExtension"
    }

    abstract val projectId: Property<Int>
    val fileTypes: SetProperty<String> = project.objects.setProperty(String::class.java)
        .convention(project.provider({ listOf("kt", "java") }))
    val serverUrl: Property<String> = project.objects.property(String::class.java)
        .convention(project.provider({ "https://api.metrics.anadea.co/api/metrics" }))
}

abstract class PublishMetricsTask : DefaultTask() {

    @TaskAction
    fun publishMetrics() {
        val metrics = MetricsCalculator(project).calculateMetrics()
        sendMetrics(metrics)
    }

    private fun sendMetrics(codeMetricsReport: MetricsReport) {
        sendMetric("totalLinesOfCode", codeMetricsReport.productionCodeLines)
        sendMetric("totalLinesOfTestCode", codeMetricsReport.testCodeLines)
        sendMetric("codeCoveragePercentage", codeMetricsReport.testCoveragePercent)
    }

    private fun sendMetric(metricName: String, metricValue: Int) {
        val codeMetricsPluginExtension = project.extensions.getByType(CodeMetricsPluginExtension::class.java)
        val serverUrl = codeMetricsPluginExtension.serverUrl.get()
        val projectId = codeMetricsPluginExtension.projectId.get()
        val projectMetricUri = URI.create(
            "$serverUrl?projectId=$projectId&metricName=$metricName"
        )
        val body = HttpRequest.BodyPublishers.ofString(
            """{ "metricValue": $metricValue }""".trimIndent()
        )
        val request = HttpRequest.newBuilder(projectMetricUri)
            .POST(body)
            .header("Content-Type", "application/json")
            .build()
        val response = HttpClient.newHttpClient().send(request, BodyHandlers.discarding())
        val statusCode = response.statusCode()
        if (statusCode != 200) {
            throw RuntimeException("Error: Failed to send metric $metricName with value $metricValue. HTTP code: $statusCode")
        }
    }

}

abstract class PrintMetricsTask : DefaultTask() {

    @TaskAction
    fun printMetrics() {
        val metrics = MetricsCalculator(project).calculateMetrics()
        println(metrics)
    }
}

class CodeMetricsPlugin : Plugin<Project> {

    private lateinit var codeMetricsPluginExtension: CodeMetricsPluginExtension

    override fun apply(project: Project) {
        codeMetricsPluginExtension = project.extensions.create<CodeMetricsPluginExtension>(
            CodeMetricsPluginExtension.NAME, CodeMetricsPluginExtension::class.java
        )

        val jacocoTestReportTasks = project.getAllTasks(true)
            .mapNotNull { it.value.find { it.name == "jacocoTestReport" } }
            .toTypedArray()

        project.tasks.register(
            "publishMetrics",
            PublishMetricsTask::class.java
        ).get().dependsOn(*jacocoTestReportTasks)

        project.tasks.register(
            "printMetrics",
            PrintMetricsTask::class.java
        ).get().dependsOn(*jacocoTestReportTasks)
    }

}

class MetricsCalculator(private val project: Project) {

    fun calculateMetrics(): MetricsReport {
        val codeStatistics = getCodeStatistics(project)
        val coverage = getCoverage()

        return MetricsReport(
            productionCodeLines = codeStatistics.productionCodeLines,
            testCodeLines = codeStatistics.testCodeLines,
            testCoveragePercent = coverage
        )
    }

    private fun getCoverage(): Int {
        return project.allprojects.map(::getCoverage).average().toInt()
    }

    fun getCoverage(project: Project): Int {
        return Jsoup.parse(File(project.buildDir, "/reports/jacoco/test/html/index.html"), Charsets.UTF_8.name())
            .select("#coveragetable tfoot tr td:nth-child(3)")
            .text()
            .dropLast(1) // drop % symbol
            .toInt()
    }

    private fun getCodeStatistics(project: Project): CodeStatistics {
        val codeLinesMap = project.allprojects
            .map { it.the<SourceSetContainer>() }
            .map { sourceSetContainer -> sourceSetContainer.associate(::countLines) }
            .sumValues()

        return CodeStatistics(
            productionCodeLines = codeLinesMap.getValue("main"),
            testCodeLines = codeLinesMap.getValue("test")
        )
    }

    private fun countLines(sourceSet: SourceSet): Pair<String, Int> {
        val codeMetricsPluginExtension = project.extensions.getByType(CodeMetricsPluginExtension::class.java)
        val fileTypes = codeMetricsPluginExtension.fileTypes.get()
        val filesToCountLines = sourceSet.allSource.filter { it.extension in fileTypes }
        return sourceSet.name to filesToCountLines.sumOf(::linesOfCodeInFile)
    }

    private fun linesOfCodeInFile(file: File): Int {
        return file.readLines().count(String::isNotBlank)
    }

    private fun List<Map<String, Int>>.sumValues(): Map<String, Int> {
        return fold(mutableMapOf<String, Int>()) { acc, linesMap ->
            linesMap.forEach { (sourceSetName, linesOfCode) ->
                acc[sourceSetName] = (acc[sourceSetName] ?: 0) + linesOfCode
            }
            acc
        }
    }

    data class CodeStatistics(
        val productionCodeLines: Int,
        val testCodeLines: Int
    )

}

data class MetricsReport(
    val productionCodeLines: Int,
    val testCodeLines: Int,
    val testCoveragePercent: Int
)
