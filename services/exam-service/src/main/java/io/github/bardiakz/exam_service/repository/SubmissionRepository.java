package io.github.bardiakz.exam_service.repository;

import io.github.bardiakz.exam_service.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByExamIdAndStudentId(Long examId, String studentId);

    List<Submission> findByStudentId(String studentId);

    List<Submission> findByExamId(Long examId);

    boolean existsByExamIdAndStudentId(Long examId, String studentId);
}