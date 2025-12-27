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

  /// Get payment by ID
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

  /// Get payment by order ID
  Future<Payment> getPaymentByOrderId(int orderId) async {
    try {
      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/payments/order/$orderId'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return Payment.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else if (response.statusCode == 404) {
        throw ServerException('Payment not found for this order');
      } else {
        throw ServerException('Failed to load payment');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading payment: ${e.toString()}');
    }
  }

  /// Get all payments for current user
  Future<List<Payment>> getMyPayments() async {
    try {
      // Extract username from token (same as in app_providers.dart)
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

      final response = await http
          .get(
            Uri.parse('${ApiService.baseUrl}/api/payments/user/$username'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => Payment.fromJson(json)).toList();
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else {
        throw ServerException('Failed to load payments');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error loading payments: ${e.toString()}');
    }
  }

  /// Process a manual payment (typically triggered automatically by order creation)
  Future<Payment> processPayment({
    required int orderId,
    required String username,
    required double amount,
    required String paymentMethod,
    String? cardNumber,
    String? cvv,
    String? expiryDate,
  }) async {
    try {
      final response = await http
          .post(
            Uri.parse('${ApiService.baseUrl}/api/payments'),
            headers: _getHeaders(),
            body: jsonEncode({
              'orderId': orderId,
              'username': username,
              'amount': amount,
              'paymentMethod': paymentMethod,
              if (cardNumber != null) 'cardNumber': cardNumber,
              if (cvv != null) 'cvv': cvv,
              if (expiryDate != null) 'expiryDate': expiryDate,
            }),
          )
          .timeout(const Duration(seconds: 30)); // Longer timeout for payment

      if (response.statusCode == 201 || response.statusCode == 200) {
        return Payment.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else if (response.statusCode == 400) {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Invalid payment request');
      } else {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Payment processing failed');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error processing payment: ${e.toString()}');
    }
  }

  /// Request a refund for a completed payment (admin/faculty only)
  Future<Payment> refundPayment(int paymentId) async {
    try {
      final response = await http
          .post(
            Uri.parse('${ApiService.baseUrl}/api/payments/$paymentId/refund'),
            headers: _getHeaders(),
          )
          .timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return Payment.fromJson(jsonDecode(response.body));
      } else if (response.statusCode == 401) {
        throw AuthException('Unauthorized');
      } else if (response.statusCode == 403) {
        throw ServerException('You do not have permission to refund payments');
      } else if (response.statusCode == 400) {
        final error = jsonDecode(response.body);
        throw ServerException(error['error'] ?? 'Cannot refund this payment');
      } else {
        throw ServerException('Refund request failed');
      }
    } catch (e) {
      if (e is AuthException || e is ServerException) rethrow;
      throw NetworkException('Error processing refund: ${e.toString()}');
    }
  }

  /// Poll payment status (useful for checking if async payment completed)
  Future<Payment> pollPaymentStatus(int orderId, {int maxAttempts = 10}) async {
    for (int i = 0; i < maxAttempts; i++) {
      try {
        final payment = await getPaymentByOrderId(orderId);

        // If payment is no longer pending, return it
        if (payment.status != 'PENDING') {
          return payment;
        }

        // Wait 2 seconds before next attempt
        await Future.delayed(const Duration(seconds: 2));
      } catch (e) {
        // If payment not found yet, wait and retry
        if (i == maxAttempts - 1) rethrow;
        await Future.delayed(const Duration(seconds: 2));
      }
    }

    throw ServerException('Payment status check timeout');
  }
}
