import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/tracking.dart';
import 'api_service.dart';

class TrackingService {
  final String? _token;

  TrackingService(this._token);

  Map<String, String> _getHeaders() {
    final headers = {'Content-Type': 'application/json'};
    if (_token != null) {
      headers['Authorization'] = 'Bearer $_token';
    }
    return headers;
  }

  Future<List<ShuttleLocation>> getActiveShuttles() async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/tracking/shuttles/active'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => ShuttleLocation.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        throw ServerException('Failed to load active shuttles');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading active shuttles: ${e.toString()}');
    }
  }

  Future<ShuttleLocation> getShuttleLocation(int id) async {
    try {
      final response = await http
          .get(
            Uri.parse(
              '${ApiService.baseUrl}/api/tracking/shuttles/$id/location',
            ),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return ShuttleLocation.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        throw ServerException('Failed to load shuttle location');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading shuttle location: ${e.toString()}');
    }
  }

  // Admin methods
  Future<List<Shuttle>> getAllShuttles() async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/tracking/shuttles'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => Shuttle.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        throw ServerException('Failed to load all shuttles');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading all shuttles: ${e.toString()}');
    }
  }

  Future<Shuttle> registerShuttle({
    required String vehicleNumber,
    required String routeName,
    required int capacity,
  }) async {
    try {
      final uri = Uri.parse('${ApiService.baseUrl}/api/tracking/shuttles')
          .replace(
            queryParameters: {
              'vehicleNumber': vehicleNumber,
              'routeName': routeName,
              'capacity': capacity.toString(),
            },
          );

      final response = await http
          .post(uri, headers: _getHeaders())
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 201) {
        return Shuttle.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Failed to register shuttle');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error registering shuttle: ${e.toString()}');
    }
  }

  Future<void> updateShuttleStatus(int id, ShuttleStatus status) async {
    try {
      final uri = Uri.parse(
        '${ApiService.baseUrl}/api/tracking/shuttles/$id/status',
      ).replace(queryParameters: {'status': status.toString().split('.').last});

      final response = await http
          .patch(uri, headers: _getHeaders())
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return;
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        throw ServerException('Failed to update shuttle status');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error updating shuttle status: ${e.toString()}');
    }
  }
}
