package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeResponse

class ProjectUpdateRequest(
    val name: String,
    val sourceCode: String,
    val runCodeResponse: RunCodeResponse
)
