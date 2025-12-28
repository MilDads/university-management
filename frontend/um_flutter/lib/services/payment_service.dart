import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/payment.dart';
import 'api_service.dart';

class PaymentService {
  final String? _token;

  PaymentService(this._token);

  Map<String, String> _getHeaders() {
    final headers = {'Content-Type': 'application/json'};
    if (_token != null) {
      headers['Authorization'] = 'Bearer $_token';
    }
    return headers;
  }

  Future<Payment> getPaymentByOrderId(int orderId) async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/payments/order/$orderId'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      print('DEBUG: Response status: ${response.statusCode}');
      print('DEBUG: Response body: ${response.body}');

      if (response.statusCode == 200) {
        final jsonData = jsonDecode(response.body) as Map<String, dynamic>;
        return Payment.fromJson(jsonData);
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else if (response.statusCode == 404) {
        throw ServerException('Payment not found for this order');
      } else {
        throw ServerException('Failed to load payment: ${response.statusCode}');
      }
    } on AuthException {
      rethrow;
    } on ServerException {
      rethrow;
    } catch (e, stack) {
      print('ERROR in getPaymentByOrderId: $e');
      print('Stack: $stack');
      throw NetworkException('Error loading payment: ${e.toString()}');
    }
  }

  Future<Payment> pollPaymentStatus(int orderId, {int maxAttempts = 10}) async {
    for (int i = 0; i < maxAttempts; i++) {
      try {
        print('DEBUG: Poll attempt ${i + 1}/$maxAttempts for order $orderId');
        final payment = await getPaymentByOrderId(orderId);
        print('DEBUG: Payment status: ${payment.status}');

        if (payment.status != 'PENDING') {
          return payment;
        }

        await Future.delayed(const Duration(seconds: 2));
      } catch (e) {
        print('DEBUG: Poll error on attempt ${i + 1}: $e');
        if (i == maxAttempts - 1) rethrow;
        await Future.delayed(const Duration(seconds: 2));
      }
    }

    throw ServerException('Payment status check timeout');
  }

  // Keep all your other methods the same...
  Future<Payment> getPaymentById(int id) async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/payments/$id'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return Payment.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else if (response.statusCode == 404) {
        throw ServerException('Payment not found');
      } else {
        throw ServerException('Failed to load payment');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading payment: ${e.toString()}');
    }
  }

  Future<List<Payment>> getMyPayments() async {
    try {
      if (_token == null) {
        throw AuthException('No authentication token available');
      }

      final parts = _token!.split('.');
      if (parts.length != 3) {
        throw AuthException('Invalid token format');
      }

      final payload = parts[1];
      final normalized = base64Url.normalize(payload);
      final decoded = utf8.decode(base64Url.decode(normalized));
      final Map<String, dynamic> payloadMap = jsonDecode(decoded);
      final username = payloadMap['sub'] as String?;

      if (username == null) {
        throw AuthException('Unable to extract username from token');
      }

      print('DEBUG: Fetching payments for user: $username');

      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/payments/user/$username'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      print('DEBUG: My Payments Response: ${response.statusCode}');
      print('DEBUG: My Payments Body: ${response.body}');

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        print('DEBUG: Found ${data.length} payments');
        return data.map((json) => Payment.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        throw ServerException('Failed to load payments');
      }
    } catch (e, stack) {
      print('ERROR in getMyPayments: $e');
      print('Stack: $stack');
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading payments: ${e.toString()}');
    }
  }
}
