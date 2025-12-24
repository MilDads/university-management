import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user.dart';
import '../models/resource.dart';
import '../models/booking.dart';
import '../services/api_service.dart';

// API Service Provider
final apiServiceProvider = Provider<ApiService>((ref) => ApiService());

// Auth State Provider
final authProvider = StateNotifierProvider<AuthNotifier, AsyncValue<User?>>((
  ref,
) {
  return AuthNotifier(ref.read(apiServiceProvider));
});

class AuthNotifier extends StateNotifier<AsyncValue<User?>> {
  final ApiService _apiService;

  AuthNotifier(this._apiService) : super(const AsyncValue.loading()) {
    _loadUser();
  }

  Future<void> _loadUser() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString('token');
      final username = prefs.getString('username');
      final role = prefs.getString('role');

      if (token != null && username != null && role != null) {
        _apiService.setToken(token);
        state = AsyncValue.data(
          User(username: username, token: token, role: role),
        );
      } else {
        state = const AsyncValue.data(null);
      }
    } catch (e) {
      state = AsyncValue.error(e, StackTrace.current);
    }
  }

  Future<void> register(String username, String password, String role) async {
    try {
      await _apiService.register(username, password, role);
      // Auto-login after registration
      await login(username, password);
    } catch (e) {
      rethrow;
    }
  }

  Future<void> login(String username, String password) async {
    state = const AsyncValue.loading();
    try {
      final response = await _apiService.login(username, password);

      // Save to SharedPreferences
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('token', response.token!);
      await prefs.setString('username', response.username!);

      // Decode JWT to get role (simple base64 decode)
      final parts = response.token!.split('.');
      if (parts.length == 3) {
        final payload = parts[1];
        final normalized = base64Url.normalize(payload);
        final decoded = utf8.decode(base64Url.decode(normalized));
        final Map<String, dynamic> payloadMap = jsonDecode(decoded);
        final role = payloadMap['role'] ?? 'STUDENT';
        await prefs.setString('role', role);

        final user = User(
          username: response.username!,
          token: response.token!,
          role: role,
        );

        state = AsyncValue.data(user);
      }
    } catch (e) {
      state = AsyncValue.error(e, StackTrace.current);
      rethrow;
    }
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
    _apiService.clearToken();
    state = const AsyncValue.data(null);
  }
}

// Resources Provider
final resourcesProvider = FutureProvider<List<Resource>>((ref) async {
  final apiService = ref.read(apiServiceProvider);
  return apiService.getResources();
});

// Resources by Type Provider
final resourcesByTypeProvider = FutureProvider.family<List<Resource>, String>((
  ref,
  type,
) async {
  final apiService = ref.read(apiServiceProvider);
  return apiService.getResourcesByType(type);
});

// Available Resources by Type Provider
final availableResourcesByTypeProvider =
    FutureProvider.family<List<Resource>, String>((ref, type) async {
      final apiService = ref.read(apiServiceProvider);
      return apiService.getAvailableResourcesByType(type);
    });

// My Bookings Provider
final myBookingsProvider = FutureProvider<List<Booking>>((ref) async {
  final apiService = ref.read(apiServiceProvider);
  return apiService.getMyBookings();
});
