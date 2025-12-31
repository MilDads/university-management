class User {
  final String username;
  final String token;
  String? role;
  String? email;

  User({required this.username, required this.token, role});

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      username: json['username'],
      token: json['token'],
      role: json['role'] ?? 'STUDENT',
    );
  }

  Map<String, dynamic> toJson() {
    return {'username': username, 'token': token, 'role': role};
  }

  bool get isFaculty => role == 'FACULTY';
  bool get isStudent => role == 'STUDENT';
  bool get isInstructor => role == 'INSTRUCTOR';
  bool get isAdmin => role == 'ADMIN';
}

enum UserRole { ADMIN, STUDENT, INSTRUCTOR, FACULTY }

extension UserRoleExtension on UserRole {
  String get value {
    switch (this) {
      case UserRole.ADMIN:
        return 'ADMIN';
      case UserRole.STUDENT:
        return 'STUDENT';
      case UserRole.INSTRUCTOR:
        return 'INSTRUCTOR';
      case UserRole.FACULTY:
        return 'FACULTY';
    }
  }

  String get displayName {
    switch (this) {
      case UserRole.ADMIN:
        return 'Administrator';
      case UserRole.STUDENT:
        return 'Student';
      case UserRole.INSTRUCTOR:
        return 'Instructor';
      case UserRole.FACULTY:
        return 'Faculty';
    }
  }
}
