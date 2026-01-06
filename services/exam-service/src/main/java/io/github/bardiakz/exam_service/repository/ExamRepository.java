package io.github.bardiakz.exam_service.repository;

import io.github.bardiakz.exam_service.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByInstructorId(String instructorId);

    List<Exam> findByStatus(Exam.ExamStatus status);

    @Query("SELECT e FROM Exam e WHERE e.startTime <= :now AND e.endTime >= :now AND e.status = 'ACTIVE'")
    List<Exam> findActiveExams(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Exam e WHERE e.startTime > :now AND e.status = 'SCHEDULED'")
    List<Exam> findUpcomingExams(@Param("now") LocalDateTime now);
}