package com.anahoret.imagilabsapi.lovable.domain.accountcards

import com.anahoret.imagilabsapi.lovable.domain.LovableAccount
import org.springframework.stereotype.Service
import java.io.InputStream

interface StudentLovableAccountCardsCsvGenerator {

    fun generate(studentClassroomCards: List<LovableAccount>): InputStream
}

@Service
class StudentLovableAccountCardsCsvGeneratorImpl : StudentLovableAccountCardsCsvGenerator {

    companion object {

        val HEADERS = listOf(
            "Username",
            "Email",
            "Password"
        )
    }

    override fun generate(studentClassroomCards: List<LovableAccount>): InputStream {
        return StringBuilder()
            .appendLine(HEADERS.joinToString(","))
            .apply {
                studentClassroomCards.forEach { card ->
                    appendLine("${card.username},${card.email},${card.password}")
                }
            }.toString().byteInputStream()
    }

}
