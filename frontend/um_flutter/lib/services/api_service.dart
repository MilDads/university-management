import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/resource.dart';
import '../models/booking.dart';

class AuthException implements Exception {
  final String message;
  AuthException(this.message);
  @override
  String toString() => message;
}

class NetworkException implements Exception {
  final String message;
  NetworkException(this.message);
  @override
  String toString() => message;
}

class ServerException implements Exception {
  final String message;
  ServerException(this.message);
  @override
  String toString() => message;
}

class ApiService {
  // For local development (web/desktop): http://localhost:8080
  // For Android emulator: http://10.0.2.2:8080
  // For iOS simulator: http://localhost:8080
  // For physical device: http://YOUR_COMPUTER_IP:8080 (e.g., http://192.168.1.100:8080)
  static String get baseUrl {
    if (kIsWeb) return 'http://localhost:8080';
    if (defaultTargetPlatform == TargetPlatform.android) return 'http://10.0.2.2:8080';
    return 'http://localhost:8080';
  }

  String? _token;

  void setToken(String token) {
    _token = token;
  }

  void clearToken() {
    _token = null;
  }

  Map<String, String> _getHeaders({bool requiresAuth = false}) {
    final headers = {'Content-Type': 'application/json'};

    if (requiresAuth && _token != null) {
      headers['Authorization'] = 'Bearer $_token';
    }

    return headers;
  }

  // Auth APIs
  Future<LoginResponse> register(
    String username,
    String password,
    String role,
  ) async {
    try {
      final response = await http
          .post(
            Uri.parse('$baseUrl/api/auth/register'),
            headers: _getHeaders(),
            body: jsonEncode({
              'username': username,
              'password': password,
              'role': role,
            }),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 201 || response.statusCode == 200) {
        // Registration successful - return success without token
        // User needs to login separately to get token
        final data = jsonDecode(response.body);
        return LoginResponse(
          success: true,
          message: data['message'] ?? 'User registered successfully',
        );
      } else {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Registration failed');
      }
    } catch (e) {
      throw NetworkException('Network error: ${e.toString()}');
    }
  }

  Future<LoginResponse> login(String username, String password) async {
    try {
      final response = await http
          .post(
            Uri.parse('$baseUrl/api/auth/login'),
            headers: _getHeaders(),
            body: jsonEncode({
              'username': username,
              'password': password,
              // Removed 'role' - not needed for login
            }),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        _token = data['token'];
        return LoginResponse(
          success: true,
          token: data['token'],
          username: data['username'],
        );
      } else {
        final error = jsonDecode(response.body);
        throw AuthException(error['error'] ?? 'Login failed');
      }
    } catch (e) {
      if (e.toString().contains('Connection refused') ||
          e.toString().contains('Failed host lookup') ||
          e.toString().contains('Network is unreachable')) {
        throw NetworkException(
          'Cannot connect to server. Check if:\n'
          '1. Backend server is running\n'
          '2. baseUrl is correct ($baseUrl)\n'
          '3. Device can reach the server',
        );
      }
      throw NetworkException('Network error: ${e.toString()}');
    }
  }


  // Resource APIs
  Future<List<Resource>> getResources() async {
    try {
      final response = await http
          .get(
            Uri.parse('$baseUrl/api/resources'),
            headers: _getHeaders(requiresAuth: true),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => Resource.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else {
        throw ServerException('Failed to load resources');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading resources: ${e.toString()}');
    }
  }

  Future<Resource> getResourceById(int id) async {
    try {
      final response = await http
          .get(
            Uri.parse('$baseUrl/api/resources/$id'),
            headers: _getHeaders(requiresAuth: true),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return Resource.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else {
        throw ServerException('Failed to load resource');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading resource: ${e.toString()}');
    }
  }

  Future<List<Resource>> getResourcesByType(String type) async {
    try {
      final response = await http
          .get(
            Uri.parse('$baseUrl/api/resources/type/$type'),
            headers: _getHeaders(requiresAuth: true),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => Resource.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else {
        throw ServerException('Failed to load resources by type');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading resources: ${e.toString()}');
    }
  }

  Future<List<Resource>> getAvailableResourcesByType(String type) async {
    try {
      final response = await http
          .get(
            Uri.parse('$baseUrl/api/resources/type/$type/available'),
            headers: _getHeaders(requiresAuth: true),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => Resource.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else {
        throw ServerException('Failed to load available resources');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading resources: ${e.toString()}');
    }
  }

  Future<Resource> createResource({
    required String name,
    required String description,
    required String type,
    required String location,
    required int capacity,
  }) async {
    try {
      final response = await http
          .post(
            Uri.parse('$baseUrl/api/resources'),
            headers: _getHeaders(requiresAuth: true),
            body: jsonEncode({
              'name': name,
              'description': description,
              'type': type,
              'location': location,
              'capacity': capacity,
            }),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 201 || response.statusCode == 200) {
        return Resource.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Failed to create resource');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error creating resource: ${e.toString()}');
    }
  }

  Future<void> deleteResource(int id) async {
    try {
      final response = await http
          .delete(
            Uri.parse('$baseUrl/api/resources/$id'),
            headers: _getHeaders(requiresAuth: true),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else if (response.statusCode != 200) {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Failed to delete resource');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error deleting resource: ${e.toString()}');
    }
  }

  // Booking APIs
  Future<Booking> createBooking({
    required int resourceId,
    required DateTime startTime,
    required DateTime endTime,
    required String purpose,
  }) async {
    try {
      // Need to pass X-User-Id header as well, but backend might extract from token?
      // Based on BookingController: @RequestHeader(value = "X-User-Id", required = false) String userId
      // And API Gateway might be stripping/forwarding headers.
      // Usually API Gateway extracts userId from JWT and forwards it as X-User-Id.
      // Assuming the Gateway does this.

      final response = await http
          .post(
            Uri.parse('$baseUrl/api/bookings'),
            headers: _getHeaders(requiresAuth: true),
            body: jsonEncode({
              'resourceId': resourceId,
              'startTime': startTime.toIso8601String(),
              'endTime': endTime.toIso8601String(),
              'purpose': purpose,
            }),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 201) {
        return Booking.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else if (response.statusCode == 409) {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Booking conflict');
      } else {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Failed to create booking');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error creating booking: ${e.toString()}');
    }
  }

  Future<List<Booking>> getMyBookings() async {
    try {
      final response = await http
          .get(
            Uri.parse('$baseUrl/api/bookings/my-bookings'),
            headers: _getHeaders(requiresAuth: true),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => Booking.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else {
        throw ServerException('Failed to load bookings');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading bookings: ${e.toString()}');
    }
  }

  Future<List<Booking>> getBookingsForResource(int resourceId) async {
    try {
      final response = await http
          .get(
            Uri.parse('$baseUrl/api/bookings/resource/$resourceId'),
            headers: _getHeaders(requiresAuth: true),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => Booking.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else {
        throw ServerException('Failed to load resource bookings');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading resource bookings: ${e.toString()}');
    }
  }

  Future<void> cancelBooking(int id) async {
    try {
      final response = await http
          .delete(
            Uri.parse('$baseUrl/api/bookings/$id'),
            headers: _getHeaders(requiresAuth: true),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return;
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Failed to cancel booking');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error cancelling booking: ${e.toString()}');
    }
  }
// complete register
  Future<bool> registerWithProfile({
    required String username,
    required String password,
    required String role,
    required String fullName,
    required String studentNumber,
    String? phoneNumber,
  }) async {
    try {
      final authResult = await register(username, password, role);
      if (!authResult.success) {
        throw AuthException('unsuccessful register: ${authResult.message}');
      }

      final profileResponse = await http.post(
        Uri.parse('$baseUrl/api/profiles'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'username': username,
          'email': username,         
          'role': role,
          'fullName': fullName,
          'studentNumber': studentNumber,
          'phoneNumber': phoneNumber ?? '',
          'tenantId': 1,              
        }),
      ).timeout(const Duration(seconds: 10));

      if (profileResponse.statusCode == 201 || profileResponse.statusCode == 200) {
        print('register complete');
        return true;
      } else {
        try {
          final error = jsonDecode(profileResponse.body);
          throw ServerException(error['error'] ?? 'register failed');
        } catch (_) {
          throw ServerException('register failed with code: ${profileResponse.statusCode}');
        }
      }
    } catch (e) {
      print('register failure: $e');
      return false;
    }
  }
}

class LoginResponse {
  final bool success;
  final String? token;
  final String? username;
  final String? message;

  LoginResponse({
    required this.success,
    this.token,
    this.username,
    this.message,
  });
}
