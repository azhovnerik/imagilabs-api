package com.anahoret.imagilabsapi.pythoncompiler

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

interface PythonCompilerService {

    fun runCode(code: String): ResponseEntity<RunCodeResponse>
}

@Service
class PythonCompilerServiceImpl(
    private val pythonCompilerRestTemplate: RestTemplate
) : PythonCompilerService {

    override fun runCode(code: String): ResponseEntity<RunCodeResponse> {
        return pythonCompilerRestTemplate.postForEntity(
            url = "/api/python_interpreter",
            request = RunCodeRequest(code)
        )
    }

}
