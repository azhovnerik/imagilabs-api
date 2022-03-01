package com.anahoret.imagilabsapi.pythoncompiler

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

interface PythonCompilerService {

    fun runCode(code: String): RunCodeResponse
}

@Service
class PythonCompilerServiceImpl(
    private val pythonCompilerRestTemplate: RestTemplate
) : PythonCompilerService {

    override fun runCode(code: String): RunCodeResponse {
        return pythonCompilerRestTemplate.postForObject(
            url = "/api/python_interpreter",
            request = RunCodeRequest(code)
        )
    }

}
