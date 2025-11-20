package com.anahoret.imagilabsapi.utils

object UriUtils {
    fun getQueryParameters(uri: String): Map<String, String> {
        return uri.substringAfter('?', "")
            .split('&')
            .filter { it.contains('=') }
            .associate { it.substringBefore('=') to it.substringAfter('=') }
    }
}
