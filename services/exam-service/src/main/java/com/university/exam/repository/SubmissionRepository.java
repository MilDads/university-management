package com.university.exam.repository;

import com.university.exam.model.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<ExamSubmission, Long> {
}
