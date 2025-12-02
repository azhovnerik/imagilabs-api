package com.anahoret.imagilabsapi.edlink.api

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.Logger
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.time.measureTimedValue

@Aspect
@Component
@Profile("stage")
class EdLinkLoggerAspect(
    private val logger: Logger
) {

    @Pointcut("execution(public * com.anahoret.imagilabsapi.edlink.api.*Api.*(..))")
    fun logEdLinkApiRequests() {
    }

    @Around("logEdLinkApiRequests()")
    fun aroundApiRequest(joinPoint: ProceedingJoinPoint): Any? {
        val result = measureTimedValue {
            joinPoint.proceed()
        }
        logger.info("${joinPoint.target.javaClass.simpleName}.${joinPoint.signature.name} executed in ${result.duration}")
        return result.value
    }

}
