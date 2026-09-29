package com.wafflestudio.spring2026.seminar.repository

import com.wafflestudio.spring2026.seminar.model.Seminar
import org.springframework.data.repository.CrudRepository

interface SeminarRepository : CrudRepository<Seminar, Long>