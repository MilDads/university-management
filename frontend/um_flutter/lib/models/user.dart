class User {
  final String username;
  final String token;
  String? role;
  String? email;

  User({required this.username, required this.token, role});

  factory User.fromJson(Map<String, dynamic> json) {
    return User(username: json['username'], token: json['token']);
  }

  Map<String, dynamic> toJson() {
    return {'username': username, 'token': token};
  }
}

enum UserRole { ADMIN, STUDENT, INSTRUCTOR, FACULTY }
