class Notification {
  final int id;
  final String recipientEmail;
  final String subject;
  final String body;
  final String type;
  final String status;
  final DateTime createdAt;
  final DateTime? sentAt;
  final int retryCount;

  Notification({
    required this.id,
    required this.recipientEmail,
    required this.subject,
    required this.body,
    required this.type,
    required this.status,
    required this.createdAt,
    this.sentAt,
    required this.retryCount,
  });

  factory Notification.fromJson(Map<String, dynamic> json) {
    return Notification(
      id: json['id'] as int,
      recipientEmail: json['recipientEmail'] as String,
      subject: json['subject'] as String,
      body: json['body'] as String? ?? '', // Handle null with empty string
      type: json['type'].toString(), // Handles both String and enum
      status: json['status'].toString(), // Handles both String and enum
      createdAt: DateTime.parse(json['createdAt'] as String),
      sentAt: json['sentAt'] != null
          ? DateTime.parse(json['sentAt'] as String)
          : null,
      retryCount: json['retryCount'] as int? ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'recipientEmail': recipientEmail,
      'subject': subject,
      'body': body,
      'type': type,
      'status': status,
      'createdAt': createdAt.toIso8601String(),
      'sentAt': sentAt?.toIso8601String(),
      'retryCount': retryCount,
    };
  }

  bool get isSent => status == 'SENT';
  bool get isPending => status == 'PENDING';
  bool get isFailed => status == 'FAILED';
  bool get isRetrying => status == 'RETRY';
}

enum NotificationType {
  USER_REGISTRATION,
  BOOKING_CONFIRMATION,
  BOOKING_CANCELLATION,
  ORDER_CONFIRMATION,
  PAYMENT_SUCCESS,
  PAYMENT_FAILURE,
  EXAM_CREATED,
  EXAM_SUBMITTED,
  EXAM_GRADED,
  GENERAL;

  String get displayName {
    switch (this) {
      case NotificationType.USER_REGISTRATION:
        return 'Welcome';
      case NotificationType.BOOKING_CONFIRMATION:
        return 'Booking Confirmed';
      case NotificationType.BOOKING_CANCELLATION:
        return 'Booking Cancelled';
      case NotificationType.ORDER_CONFIRMATION:
        return 'Order Placed';
      case NotificationType.PAYMENT_SUCCESS:
        return 'Payment Successful';
      case NotificationType.PAYMENT_FAILURE:
        return 'Payment Failed';
      case NotificationType.EXAM_CREATED:
        return 'New Exam';
      case NotificationType.EXAM_SUBMITTED:
        return 'Exam Submitted';
      case NotificationType.EXAM_GRADED:
        return 'Exam Graded';
      case NotificationType.GENERAL:
        return 'Notification';
    }
  }
}

enum NotificationStatus {
  PENDING,
  SENT,
  FAILED,
  RETRY;

  String get displayName {
    switch (this) {
      case NotificationStatus.PENDING:
        return 'Pending';
      case NotificationStatus.SENT:
        return 'Sent';
      case NotificationStatus.FAILED:
        return 'Failed';
      case NotificationStatus.RETRY:
        return 'Retrying';
    }
  }
}
