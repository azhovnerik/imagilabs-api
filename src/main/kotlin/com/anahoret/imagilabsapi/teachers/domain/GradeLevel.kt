package com.anahoret.imagilabsapi.teachers.domain

/**
 * Grade levels based on EdLink API
 * @see <a href="https://ed.link/docs/api/v2.0/enums/grade-level">EdLink Grade Levels</a>
 */
enum class GradeLevel(val displayName: String) {
    PRESCHOOL("Preschool"),
    PRE_KINDERGARTEN("Pre-Kindergarten"),
    TRANSITIONAL_KINDERGARTEN("Transitional Kindergarten"),
    KINDERGARTEN("Kindergarten"),
    FIRST_GRADE("1st Grade"),
    SECOND_GRADE("2nd Grade"),
    THIRD_GRADE("3rd Grade"),
    FOURTH_GRADE("4th Grade"),
    FIFTH_GRADE("5th Grade"),
    SIXTH_GRADE("6th Grade"),
    SEVENTH_GRADE("7th Grade"),
    EIGHTH_GRADE("8th Grade"),
    NINTH_GRADE("9th Grade"),
    TENTH_GRADE("10th Grade"),
    ELEVENTH_GRADE("11th Grade"),
    TWELFTH_GRADE("12th Grade"),
    THIRTEENTH_GRADE("13th Grade"),
    POST_GRADUATE("Post-Graduate"),
    UNGRADED("Ungraded"),
    OTHER("Other")
}