package com.anahoret.imagilabsapi.schools.domain

import com.anahoret.imagilabsapi.schools.storage.SchoolEntity
import com.anahoret.imagilabsapi.schools.storage.SchoolRepository
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface SchoolService {
    fun create(name: String): School
    fun getById(id: UUID): School?
    fun listAll(sort: Sort = Sort.by(Sort.Direction.ASC, "name")): List<School>
    fun update(id: UUID, name: String): School?
    fun delete(id: UUID)
    fun exists(name: String): Boolean
    fun listByIds(ids: Iterable<UUID>): List<School>
}

@Service
class SchoolServiceImpl(
    private val schoolRepository: SchoolRepository
) : SchoolService {

    @Transactional
    override fun create(name: String): School {
        if (schoolRepository.existsByNameIgnoreCase(name)) {
            throw IllegalArgumentException("School with name '$name' already exists")
        }

        val entity = SchoolEntity(name = name)
        return School.fromEntity(schoolRepository.save(entity))
    }

    override fun getById(id: UUID): School? {
        return schoolRepository.findByIdOrNull(id)?.let { School.fromEntity(it) }
    }

    override fun listAll(sort: Sort): List<School> {
        return schoolRepository.findAll(sort).map { School.fromEntity(it) }
    }

    @Transactional
    override fun update(id: UUID, name: String): School? {
        val entity = schoolRepository.findByIdOrNull(id) ?: return null

        // Check if another school with this name exists
        val existing = schoolRepository.findByNameIgnoreCase(name)
        if (existing != null && existing.id != id) {
            throw IllegalArgumentException("School with name '$name' already exists")
        }

        entity.name = name
        return School.fromEntity(schoolRepository.save(entity))
    }

    @Transactional
    override fun delete(id: UUID) {
        schoolRepository.deleteById(id)
    }

    override fun exists(name: String): Boolean {
        return schoolRepository.existsByNameIgnoreCase(name)
    }

    override fun listByIds(ids: Iterable<UUID>): List<School> {
        return schoolRepository.findAllById(ids).map { School.fromEntity(it) }
    }
}