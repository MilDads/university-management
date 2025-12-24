class Booking {
  final int id;
  final int resourceId;
  final String userId;
  final DateTime startTime;
  final DateTime endTime;
  final String purpose;
  final String status;

  Booking({
    required this.id,
    required this.resourceId,
    required this.userId,
    required this.startTime,
    required this.endTime,
    required this.purpose,
    required this.status,
  });

  factory Booking.fromJson(Map<String, dynamic> json) {
    return Booking(
      id: json['id'],
      resourceId: json['resourceId'],
      userId: json['userId'],
      startTime: DateTime.parse(json['startTime']),
      endTime: DateTime.parse(json['endTime']),
      purpose: json['purpose'],
      status: json['status'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'resourceId': resourceId,
      'userId': userId,
      'startTime': startTime.toIso8601String(),
      'endTime': endTime.toIso8601String(),
      'purpose': purpose,
      'status': status,
    };
  }
}

enum BookingStatus { PENDING, CONFIRMED, REJECTED, CANCELLED, COMPLETED }

extension BookingStatusExtension on BookingStatus {
  String get value {
    return name;
  }
}
