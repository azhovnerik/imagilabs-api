@file:Suppress("unused")

package com.anahoret.imagilabsapi.pythoncompiler.domain

class RunCodeRequest(
    val code: String,
    val language: String = "en"
)
