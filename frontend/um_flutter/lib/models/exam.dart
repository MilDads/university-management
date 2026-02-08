enum ExamStatus { DRAFT, SCHEDULED, ACTIVE, CLOSED }

enum QuestionType { MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER }

class Question {
  final int? id;
  final String text;
  final QuestionType type;
  final List<String> options;
  final String correctAnswer;
  final int marks;
  final int orderNumber;

  Question({
    this.id,
    required this.text,
    required this.type,
    required this.options,
    required this.correctAnswer,
    required this.marks,
    required this.orderNumber,
  });

  factory Question.fromJson(Map<String, dynamic> json) {
    return Question(
      id: json['id'],
      text: json['text'],
      type: QuestionType.values.firstWhere(
        (e) => e.toString().split('.').last == json['type'],
        orElse: () => QuestionType.MULTIPLE_CHOICE,
      ),
      options: json['options'] != null
          ? List<String>.from(json['options'])
          : [],
      correctAnswer: json['correctAnswer'],
      marks: json['marks'],
      orderNumber: json['orderNumber'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'text': text,
      'type': type.toString().split('.').last,
      'options': options,
      'correctAnswer': correctAnswer,
      'marks': marks,
      'orderNumber': orderNumber,
    };
  }
}

class Exam {
  final int? id;
  final String title;
  final String? description;
  final String? instructorId;
  final DateTime startTime;
  final DateTime endTime;
  final int durationMinutes;
  final int totalMarks;
  final ExamStatus status;
  final List<Question> questions;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  Exam({
    this.id,
    required this.title,
    this.description,
    this.instructorId,
    required this.startTime,
    required this.endTime,
    required this.durationMinutes,
    required this.totalMarks,
    this.status = ExamStatus.DRAFT,
    required this.questions,
    this.createdAt,
    this.updatedAt,
  });

  factory Exam.fromJson(Map<String, dynamic> json) {
    return Exam(
      id: json['id'],
      title: json['title'],
      description: json['description'],
      instructorId: json['instructorId'],
      startTime: DateTime.parse(json['startTime']),
      endTime: DateTime.parse(json['endTime']),
      durationMinutes: json['durationMinutes'],
      totalMarks: json['totalMarks'],
      status: ExamStatus.values.firstWhere(
        (e) => e.toString().split('.').last == json['status'],
        orElse: () => ExamStatus.DRAFT,
      ),
      questions: (json['questions'] as List)
          .map((q) => Question.fromJson(q))
          .toList(),
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'])
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.parse(json['updatedAt'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'title': title,
      'description': description,
      'instructorId': instructorId,
      'startTime': startTime.toIso8601String(),
      'endTime': endTime.toIso8601String(),
      'durationMinutes': durationMinutes,
      'totalMarks': totalMarks,
      'status': status.toString().split('.').last,
      'questions': questions.map((q) => q.toJson()).toList(),
    };
  }
}
