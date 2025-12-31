class UserProfile {
  final int id;
  final String username;
  final String email;
  final String role;
  final String? fullName;
  final String? studentNumber;
  final String? phoneNumber;
  final bool active;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  UserProfile({
    required this.id,
    required this.username,
    required this.email,
    required this.role,
    this.fullName,
    this.studentNumber,
    this.phoneNumber,
    required this.active,
    this.createdAt,
    this.updatedAt,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      id: json['id'] as int,
      username: json['username'] as String,
      email: json['email'] as String,
      role: json['role'] as String,
      fullName: json['fullName'] as String?,
      studentNumber: json['studentNumber'] as String?,
      phoneNumber: json['phoneNumber'] as String?,
      active: json['active'] as bool? ?? true,
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'] as String)
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.parse(json['updatedAt'] as String)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'email': email,
      'role': role,
      'fullName': fullName,
      'studentNumber': studentNumber,
      'phoneNumber': phoneNumber,
      'active': active,
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }

  bool get isStudent => role == 'STUDENT';
  bool get isInstructor => role == 'INSTRUCTOR';
  bool get isFaculty => role == 'FACULTY';
  bool get isAdmin => role == 'ADMIN';
}
