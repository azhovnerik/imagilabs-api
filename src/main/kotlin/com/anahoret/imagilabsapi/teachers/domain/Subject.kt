package com.anahoret.imagilabsapi.teachers.domain

/**
 * Subject types based on EdLink API
 * @see <a href="https://ed.link/docs/api/v2.0/subjects/overview">EdLink Subjects</a>
 */
enum class Subject(val displayName: String) {
    ENGLISH_LANGUAGE_ARTS("English Language Arts"),
    MATHEMATICS("Mathematics"),
    SCIENCE("Science"),
    SOCIAL_STUDIES("Social Studies"),
    WORLD_LANGUAGES("World Languages"),
    COMPUTER_SCIENCE("Computer Science"),
    CAREER_AND_TECHNICAL_EDUCATION("Career and Technical Education"),
    ARTS("Arts"),
    MUSIC("Music"),
    PHYSICAL_EDUCATION("Physical Education"),
    HEALTH("Health"),
    STEM("STEM"),
    OTHER("Other")
}