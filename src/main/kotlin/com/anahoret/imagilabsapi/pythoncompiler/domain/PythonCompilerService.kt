package com.anahoret.imagilabsapi.pythoncompiler.domain

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

interface PythonCompilerService {

    fun runCode(runCodeRequest: RunCodeRequest): RunCodeResponse
}

@Service
class PythonCompilerServiceImpl(
    private val pythonCompilerRestTemplate: RestTemplate
) : PythonCompilerService {

    override fun runCode(runCodeRequest: RunCodeRequest): RunCodeResponse {
        return pythonCompilerRestTemplate.postForObject(
            url = "/api/python_interpreter",
            request = runCodeRequest
        )
    }

}
