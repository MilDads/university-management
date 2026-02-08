import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/exam.dart';
import '../services/exam_service.dart';
import 'app_providers.dart';

// Exam Service Provider
final examServiceProvider = Provider<ExamService>((ref) {
  final authState = ref.watch(authProvider);
  final token = authState.value?.token;
  return ExamService(token);
});

// My Exams Provider (Instructor)
final myExamsProvider = FutureProvider.autoDispose<List<Exam>>((ref) async {
  final service = ref.read(examServiceProvider);
  return service.getMyExams();
});

// Active Exams Provider (Student)
final activeExamsProvider = FutureProvider.autoDispose<List<Exam>>((ref) async {
  final service = ref.read(examServiceProvider);
  return service.getActiveExams();
});

// Upcoming Exams Provider (Student)
final upcomingExamsProvider = FutureProvider.autoDispose<List<Exam>>((
  ref,
) async {
  final service = ref.read(examServiceProvider);
  return service.getUpcomingExams();
});

// Single Exam Provider
final examDetailsProvider = FutureProvider.autoDispose.family<Exam, int>((
  ref,
  id,
) async {
  final service = ref.read(examServiceProvider);
  return service.getExamById(id);
});
