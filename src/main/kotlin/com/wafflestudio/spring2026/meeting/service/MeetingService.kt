package com.wafflestudio.spring2026.meeting.service

import com.wafflestudio.spring2026.meeting.MeetingNotFoundException
import com.wafflestudio.spring2026.meeting.model.Meeting
import com.wafflestudio.spring2026.meeting.repository.MeetingRepository
import org.springframework.stereotype.Service

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
) {
    fun createMeeting(
        title: String,
        capacity: Int,
    ): Meeting =
        meetingRepository.save(
            title = title,
            capacity = capacity,
        )

    fun getMeeting(id: Long): Meeting =
        meetingRepository.findById(id)
            ?: throw MeetingNotFoundException(id)

    fun getMeetings(): List<Meeting> = meetingRepository.findAll()

    fun patchMeeting(id: Long, title: String?, capacity: Int?): Meeting {

        val meeting = meetingRepository.findById(id)
            ?: throw MeetingNotFoundException(id)


        val updatedMeeting = meeting.copy(
            title = title ?: meeting.title,
            capacity = capacity ?: meeting.capacity
        )

        return meetingRepository.update(updatedMeeting) // 또는 repository에 맞는 갱신 메서드 호출
    }

    fun deleteMeeting(id: Long) {
        val meeting = meetingRepository.findById(id) ?: throw MeetingNotFoundException(id)

        meetingRepository.delete(meeting.id)
    }
}
