import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user_profile.dart';
import 'api_service.dart';

class UserService {
  final String? _token;

  UserService(this._token);

  Map<String, String> _getHeaders() {
    final headers = {'Content-Type': 'application/json'};
    if (_token != null) {
      headers['Authorization'] = 'Bearer $_token';
    }
    return headers;
  }

  /// Get the current user's profile
  Future<UserProfile> getMyProfile() async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/profiles/me'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return UserProfile.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else if (response.statusCode == 404) {
        throw ServerException('Profile not found');
      } else {
        throw ServerException('Failed to load profile');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading profile: ${e.toString()}');
    }
  }

  /// Get a user profile by ID (admin only)
  Future<UserProfile> getUserProfile(int userId) async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/profiles/$userId'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return UserProfile.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else if (response.statusCode == 403) {
        throw ServerException('Access denied');
      } else if (response.statusCode == 404) {
        throw ServerException('User not found');
      } else {
        throw ServerException('Failed to load user profile');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading user profile: ${e.toString()}');
    }
  }

  /// Update the current user's profile
  Future<UserProfile> updateMyProfile({
    String? fullName,
    String? studentNumber,
    String? phoneNumber,
    String? email,
  }) async {
    try {
      final body = <String, dynamic>{};
      if (fullName != null) body['fullName'] = fullName;
      if (studentNumber != null) body['studentNumber'] = studentNumber;
      if (phoneNumber != null) body['phoneNumber'] = phoneNumber;
      if (phoneNumber != null) body['email'] = email;

      final response = await http
          .put(
            Uri.parse('${ApiService.baseUrl}/api/profiles/me'),
            headers: _getHeaders(),
            body: jsonEncode(body),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return UserProfile.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Failed to update profile');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error updating profile: ${e.toString()}');
    }
  }

  /// Update a user's role (admin only)
  Future<void> updateUserRole(int userId, String role) async {
    try {
      final response = await http
          .patch(
            Uri.parse('${ApiService.baseUrl}/api/profiles/$userId/role'),
            headers: _getHeaders(),
            body: jsonEncode({'role': role}),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else if (response.statusCode == 403) {
        throw ServerException('Only admins can update roles');
      } else if (response.statusCode != 200) {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Failed to update role');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error updating role: ${e.toString()}');
    }
  }

  /// Get all user profiles (admin only)
  Future<List<UserProfile>> getAllProfiles() async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/profiles'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => UserProfile.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else if (response.statusCode == 403) {
        throw ServerException('Admin access required');
      } else {
        throw ServerException('Failed to load profiles');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading profiles: ${e.toString()}');
    }
  }
}
