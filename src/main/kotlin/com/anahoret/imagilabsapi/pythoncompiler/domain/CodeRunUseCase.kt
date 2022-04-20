package com.anahoret.imagilabsapi.pythoncompiler.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import org.springframework.stereotype.Service

interface CodeRunUseCase {

    fun run(runBy: UserProfile, code: String): Either<OperationError, RunCodeResponse>
}

@Service
class CodeRunUseCaseImpl(
    private val pythonCompilerService: PythonCompilerService,
    val pythonCompilerAccessService: PythonCompilerAccessService,
    private val animatedTextGenerator: AnimatedTextGenerator
) : CodeRunUseCase {

    override fun run(runBy: UserProfile, code: String): Either<OperationError, RunCodeResponse> {
        if (!pythonCompilerAccessService.canRunCode(runBy))
            return AccessDeniedError("ACCESS_TO_COMPILER_DENIED").left()
        return pythonCompilerService.runCode(code)
            .let(::generateAnimatedTextIfNeeded)
            .right()
    }

    private fun generateAnimatedTextIfNeeded(runCodeResponse: RunCodeResponse): RunCodeResponse {
        val output = runCodeResponse.output ?: return runCodeResponse
        if (output.scrollingText.text == null) return runCodeResponse
        val scrollingTextAnimation = animatedTextGenerator.generateScrollingTextAnimation(output.scrollingText)
        return runCodeResponse.copy(output = output.copy(animation = scrollingTextAnimation))
    }

}
