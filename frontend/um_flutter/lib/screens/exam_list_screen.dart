import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../models/exam.dart';
import '../providers/app_providers.dart';
import '../providers/exam_providers.dart';
import 'add_edit_exam_screen.dart';

class ExamListScreen extends ConsumerWidget {
  const ExamListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final userInfo = ref.watch(currentUserInfoProvider);
    final isInstructor = userInfo.isInstructor || userInfo.isFaculty;

    return DefaultTabController(
      length: isInstructor ? 1 : 2,
      child: Scaffold(
        appBar: AppBar(
          title: Text(isInstructor ? 'My Exams' : 'Online Exams'),
          bottom: isInstructor
              ? null
              : const TabBar(
                  tabs: [
                    Tab(text: 'Active'),
                    Tab(text: 'Upcoming'),
                  ],
                ),
        ),
        body: isInstructor
            ? const _InstructorExamList()
            : const TabBarView(
                children: [_ActiveExamsList(), _UpcomingExamsList()],
              ),
        floatingActionButton: isInstructor
            ? FloatingActionButton.extended(
                onPressed: () async {
                  final result = await Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const AddEditExamScreen(),
                    ),
                  );
                  if (result == true) {
                    ref.invalidate(myExamsProvider);
                  }
                },
                icon: const Icon(Icons.add),
                label: const Text('Create Exam'),
              )
            : null,
      ),
    );
  }
}

class _InstructorExamList extends ConsumerWidget {
  const _InstructorExamList();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final examsAsync = ref.watch(myExamsProvider);

    return examsAsync.when(
      data: (exams) {
        if (exams.isEmpty) {
          return const Center(
            child: Text('No exams created yet. Tap + to create one.'),
          );
        }
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: exams.length,
          itemBuilder: (context, index) {
            final exam = exams[index];
            return _ExamCard(exam: exam, isInstructor: true);
          },
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (err, stack) => Center(
        child: Text('Error: $err', style: const TextStyle(color: Colors.red)),
      ),
    );
  }
}

class _ActiveExamsList extends ConsumerWidget {
  const _ActiveExamsList();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final examsAsync = ref.watch(activeExamsProvider);

    return examsAsync.when(
      data: (exams) {
        if (exams.isEmpty) return const Center(child: Text('No active exams.'));
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: exams.length,
          itemBuilder: (context, index) {
            return _ExamCard(exam: exams[index], isInstructor: false);
          },
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (err, stack) => Center(child: Text('Error: $err')),
    );
  }
}

class _UpcomingExamsList extends ConsumerWidget {
  const _UpcomingExamsList();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final examsAsync = ref.watch(upcomingExamsProvider);

    return examsAsync.when(
      data: (exams) {
        if (exams.isEmpty) {
          return const Center(child: Text('No upcoming exams.'));
        }
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: exams.length,
          itemBuilder: (context, index) {
            return _ExamCard(exam: exams[index], isInstructor: false);
          },
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (err, stack) => Center(child: Text('Error: $err')),
    );
  }
}

class _ExamCard extends ConsumerWidget {
  final Exam exam;
  final bool isInstructor;

  const _ExamCard({required this.exam, required this.isInstructor});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dateFormat = DateFormat('MMM d, h:mm a');

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Text(
                    exam.title,
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ),
                _StatusBadge(status: exam.status),
              ],
            ),
            if (exam.description != null) ...[
              const SizedBox(height: 8),
              Text(exam.description!),
            ],
            const SizedBox(height: 12),
            Row(
              children: [
                const Icon(Icons.access_time, size: 16, color: Colors.grey),
                const SizedBox(width: 4),
                Text(
                  '${dateFormat.format(exam.startTime)} - ${dateFormat.format(exam.endTime)}',
                  style: const TextStyle(color: Colors.grey),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Row(
              children: [
                const Icon(Icons.timer, size: 16, color: Colors.grey),
                const SizedBox(width: 4),
                Text(
                  '${exam.durationMinutes} mins',
                  style: const TextStyle(color: Colors.grey),
                ),
                const SizedBox(width: 16),
                const Icon(Icons.grade, size: 16, color: Colors.grey),
                const SizedBox(width: 4),
                Text(
                  '${exam.totalMarks} marks',
                  style: const TextStyle(color: Colors.grey),
                ),
              ],
            ),
            if (isInstructor && exam.status == ExamStatus.DRAFT) ...[
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () async {
                    // Confirm publish
                    final confirm = await showDialog<bool>(
                      context: context,
                      builder: (context) => AlertDialog(
                        title: const Text('Publish Exam?'),
                        content: const Text(
                          'Once published, students will be able to see this exam. This action cannot be undone.',
                        ),
                        actions: [
                          TextButton(
                            onPressed: () => Navigator.pop(context, false),
                            child: const Text('Cancel'),
                          ),
                          ElevatedButton(
                            onPressed: () => Navigator.pop(context, true),
                            child: const Text('Publish'),
                          ),
                        ],
                      ),
                    );

                    if (confirm == true) {
                      try {
                        await ref
                            .read(examServiceProvider)
                            .publishExam(exam.id!);
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('Exam published!')),
                        );
                        ref.invalidate(myExamsProvider);
                      } catch (e) {
                        ScaffoldMessenger.of(
                          context,
                        ).showSnackBar(SnackBar(content: Text('Error: $e')));
                      }
                    }
                  },
                  child: const Text('Publish Exam'),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  final ExamStatus status;

  const _StatusBadge({required this.status});

  @override
  Widget build(BuildContext context) {
    Color color;
    String text;

    switch (status) {
      case ExamStatus.DRAFT:
        color = Colors.orange;
        text = 'DRAFT';
        break;
      case ExamStatus.SCHEDULED:
        color = Colors.blue;
        text = 'SCHEDULED';
        break;
      case ExamStatus.ACTIVE:
        color = Colors.green;
        text = 'ACTIVE';
        break;
      case ExamStatus.CLOSED:
        color = Colors.grey;
        text = 'CLOSED';
        break;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: color,
          fontSize: 12,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}
