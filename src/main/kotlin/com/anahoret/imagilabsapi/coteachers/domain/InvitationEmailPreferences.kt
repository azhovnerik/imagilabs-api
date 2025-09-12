package com.anahoret.imagilabsapi.coteachers.domain

import java.util.*

class InvitationEmailPreferences(
    val fromName: String,
    val from: String,
    val sendTo: String,
    val invitationId: UUID
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InvitationEmailPreferences

        if (fromName != other.fromName) return false
        if (from != other.from) return false
        if (sendTo != other.sendTo) return false
        return invitationId == other.invitationId
    }

    override fun hashCode(): Int {
        var result = fromName.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + sendTo.hashCode()
        result = 31 * result + invitationId.hashCode()
        return result
    }
}
