package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

interface OpenAiAccessService {
    fun canGetAssistanceForProject(userProfile: UserProfile): Boolean
    fun hasTipTokens(userProfile: UserProfile): Boolean
}

@Service
class OpenAiAccessServiceImpl(
    private val tipTokensService: TipTokensService,
    private val environmentPermissionService: EnvironmentPermissionService
) : OpenAiAccessService {

    override fun canGetAssistanceForProject(userProfile: UserProfile): Boolean {
        return environmentPermissionService.canGetAssistanceForProject(userProfile)
    }

    override fun hasTipTokens(userProfile: UserProfile): Boolean {
        return tipTokensService.hasTipTokens(userProfile) ?: false
    }
}

interface EnvironmentPermissionService {
    fun canGetAssistanceForProject(userProfile: UserProfile): Boolean
}

@Profile("stage", "default")
@Service
class EnvironmentPermissionServiceStaging : EnvironmentPermissionService {
    override fun canGetAssistanceForProject(userProfile: UserProfile): Boolean {
        return true
    }
}

@Profile("prod")
@Service
class EnvironmentPermissionServiceProduction(
    private val clock: Clock
) : EnvironmentPermissionService {
    private val availableToAllDate = ZonedDateTime.of(2024, 9, 16, 0, 0, 0, 0, ZoneId.of("UTC-7"))
        .toInstant().toEpochMilli()

    override fun canGetAssistanceForProject(userProfile: UserProfile): Boolean {
        val now = clock.millis()
        return now >= availableToAllDate ||
                userProfile is TeacherProfile && emailIsInWhitelist(userProfile)
    }

    private fun emailIsInWhitelist(teacherProfile: TeacherProfile): Boolean {
        return teacherProfile.email.endsWith("@imagilabs.com") ||
                teacherProfile.email.endsWith("@anadeainc.com") ||
                teacherProfile.email in specificEmails
    }

    companion object {
        private val specificEmails = setOf(
            "turnerw@whitehouseisd.org",
            "kelpowers@gmail.com",
            "brianne@codeyourdreams.org",
            "sfortino@tchs.org",
            "margretasgerdur@gmail.com",
            "zeisj@fcpsk12.net",
            "tess.sandbox@gmail.com",
            "pam@devcon.ph",
            "christopher.combs@evsck12.com",
            "jrandy.macdonald@gmail.com",
            "pbesthoff@spsd.us",
            "rchew@burlington-nj.net",
            "kkeefe@wboe.net",
            "bellavca@winslow-schools.com",
            "dhack@watchungschools.us",
            "megan_schutz@nplainfield.org",
            "kkefalas@lindenps.org",
            "jemmolo@cwcboe.org",
            "kacevedo@spsd.us",
            "blairbuscareno@parkridge.k12.nj.us",
            "mlandolfi@watchungschools.us",
            "jhenry@chclc.org",
            "laura.sudak@boontonschools.org",
            "lcardace@gmail.com",
            "srothrock@minehillcas.org",
            "ksturdivant@camden.k12.nj.us",
            "jhrljo@gmail.com",
            "esteidle@chclc.org",
            "sbolognese@spsd.us",
            "ffazal@yahoo.com",
            "jwoods@chclc.org"
        )
    }
}
