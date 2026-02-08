import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/notification.dart';
import 'api_service.dart';

class NotificationService {
  final String? _token;

  NotificationService(this._token);

  Map<String, String> _getHeaders() {
    final headers = {
      'Content-Type': 'application/json',
      'X-Internal-Secret': 'your-secret-key-here',
    };
    if (_token != null) {
      headers['Authorization'] = 'Bearer $_token';
    }
    return headers;
  }

  /// Get all notifications for the current user
  Future<List<Notification>> getMyNotifications() async {
    try {
      final uri = Uri.parse('${ApiService.baseUrl}/api/notifications/me');
      final headers = _getHeaders();

      print('=== Fetching Notifications ===');
      print('URL: $uri');
      print('Token present: ${_token != null}');
      print('Headers: ${headers.keys.toList()}');

      final response = await http
          .get(uri, headers: headers)
          .timeout(const Duration(seconds: 10));

      print('Response status: ${response.statusCode}');
      print('Response body: ${response.body}');

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        print('Parsed ${data.length} notifications');

        final notifications = data.map((json) {
          try {
            return Notification.fromJson(json as Map<String, dynamic>);
          } catch (e) {
            print('Error parsing notification: $e');
            print('Problematic JSON: $json');
            rethrow;
          }
        }).toList();

        print(
          'Successfully created ${notifications.length} notification objects',
        );
        return notifications;
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized - please login again');
      } else if (response.statusCode == 400) {
        throw ServerException('Bad request - ${response.body}');
      } else {
        throw ServerException(
          'Failed to load notifications (${response.statusCode}): ${response.body}',
        );
      }
    } catch (e) {
      print('Exception in getMyNotifications: $e');
      print('Stack trace: ${StackTrace.current}');
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading notifications: ${e.toString()}');
    }
  }

  /// Get notifications by email (admin or for current user's email)
  Future<List<Notification>> getNotificationsByEmail(String email) async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/notifications/email/$email'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => Notification.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        throw ServerException('Failed to load notifications');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading notifications: ${e.toString()}');
    }
  }

  /// Get notification statistics
  Future<Map<String, int>> getNotificationStats() async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/notifications/stats'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final Map<String, dynamic> data = jsonDecode(response.body);
        return data.map((key, value) => MapEntry(key, value as int));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        throw ServerException('Failed to load notification statistics');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException(
        'Error loading notification stats: ${e.toString()}',
      );
    }
  }

  /// Send a manual notification (admin only)
  Future<Notification> sendNotification({
    required String recipientEmail,
    required String subject,
    required String body,
    required String type,
  }) async {
    try {
      final response = await http
          .post(
            Uri.parse('${ApiService.baseUrl}/api/notifications'),
            headers: _getHeaders(),
            body: jsonEncode({
              'recipientEmail': recipientEmail,
              'subject': subject,
              'body': body,
              'type': type,
            }),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 201 || response.statusCode == 200) {
        return Notification.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else if (response.statusCode == 403) {
        throw ServerException('Admin access required');
      } else {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Failed to send notification');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error sending notification: ${e.toString()}');
    }
  }
}
