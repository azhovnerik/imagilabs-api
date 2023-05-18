package com.anahoret.imagilabsapi.subscription.web

import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.subscription.domain.SetSubscriptionPeriodRequest
import com.anahoret.imagilabsapi.subscription.domain.SetSubscriptionPeriodUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class TeacherSubscriptionController(
    private val setSubscriptionPeriodUseCase: SetSubscriptionPeriodUseCase
) {

    @Secured(UserRole.admin)
    @PutMapping("/api/teachers/{teacherId}/subscription")
    fun setSubscriptionPeriodForTeacher(
        @RequestBody setSubscriptionPeriodRequest: SetSubscriptionPeriodRequest,
        @PathVariable teacherId: UUID
    ): ResponseEntity<*> {
        setSubscriptionPeriodUseCase.set(teacherId, setSubscriptionPeriodRequest)
        return ResponseEntity.ok().build<Void>()
    }

}
