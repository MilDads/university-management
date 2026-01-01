import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user.dart';
import '../models/user_profile.dart';
import '../models/resource.dart';
import '../models/booking.dart';
import '../services/api_service.dart';
import '../services/user_service.dart';

// ==================== API Service Provider ====================
final apiServiceProvider = Provider<ApiService>((ref) => ApiService());

// ==================== User Service Provider ====================
final userServiceProvider = Provider<UserService>((ref) {
  final authState = ref.watch(authProvider);
  final token = authState.value?.token;
  return UserService(token);
});

// ==================== Auth State Provider ====================
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

      if (token != null && username != null) {
        _apiService.setToken(token);
        state = AsyncValue.data(User(username: username, token: token));
      } else {
        state = const AsyncValue.data(null);
      }
    } catch (e) {
      state = AsyncValue.error(e, StackTrace.current);
    }
  }

  Future<void> register({
    required String username,
    required String password,
    String? fullName,
    String defaultRole = 'STUDENT',
  }) async {
    try {
      // Email is optional - will be added in profile later
      await _apiService.register(
        username: username,
        password: password,
        email: '$username@university.edu', // Auto-generated email
        fullName: fullName,
        defaultRole: defaultRole,
      );

      // Auto-login after registration
      await login(username, password);
    } catch (e) {
      debugPrint('AuthNotifier.register error: $e');
      rethrow;
    }
  }

  Future<void> login(String username, String password) async {
    // Don't set state to loading here, let the UI handle it.
    // Setting state triggers AuthWrapper to rebuild, which unmounts LoginScreen,
    // preventing the SnackBar from showing on error.
    try {
      final response = await _apiService.login(username, password);

      // Save to SharedPreferences (only username + token)
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('token', response.token!);
      await prefs.setString('username', response.username!);

      final user = User(username: response.username!, token: response.token!);

      state = AsyncValue.data(user);
    } catch (e) {
      debugPrint('AuthNotifier.login error: $e');
      // Don't set state to error, just rethrow so UI can handle it.
      // state = AsyncValue.error(e, StackTrace.current);
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

// ==================== User Profile Provider (WITH RETRY LOGIC) ====================
final userProfileProvider = FutureProvider.autoDispose<UserProfile?>((
  ref,
) async {
  final authState = ref.watch(authProvider);
  final user = authState.value;

  if (user == null) {
    return null;
  }

  final userService = ref.read(userServiceProvider);

  // Retry logic for newly registered users
  // Profile creation is event-driven and may take a moment
  int retries = 5;
  int delayMs = 500;

  for (int i = 0; i < retries; i++) {
    try {
      return await userService.getMyProfile();
    } catch (e) {
      if (e.toString().contains('Profile not found') && i < retries - 1) {
        // Profile doesn't exist yet, wait and retry
        debugPrint(
          'Profile not found, retrying in ${delayMs}ms... (attempt ${i + 1}/$retries)',
        );
        await Future.delayed(Duration(milliseconds: delayMs));
        delayMs = (delayMs * 1.5).toInt(); // Exponential backoff
        continue;
      }

      // If it's the last retry or a different error, handle gracefully
      if (e.toString().contains('Profile not found')) {
        debugPrint(
          'Profile not found after $retries attempts - user may need to contact support',
        );
        return null;
      }

      debugPrint('Error loading user profile: $e');
      return null;
    }
  }

  return null;
});

// ==================== Combined User Info Provider ====================
final currentUserInfoProvider = Provider<UserInfo>((ref) {
  final authState = ref.watch(authProvider);
  final profileState = ref.watch(userProfileProvider);

  final user = authState.value;
  final profile = profileState.value;

  return UserInfo(
    user: user,
    profile: profile,
    isLoading: authState.isLoading || profileState.isLoading,
  );
});

class UserInfo {
  final User? user;
  final UserProfile? profile;
  final bool isLoading;

  UserInfo({this.user, this.profile, this.isLoading = false});

  bool get isAuthenticated => user != null;

  String get username => profile?.username ?? user?.username ?? 'User';
  String get email => profile?.email ?? '';
  String get role => profile?.role ?? 'STUDENT';
  String? get fullName => profile?.fullName;
  String? get studentNumber => profile?.studentNumber;
  String? get phoneNumber => profile?.phoneNumber;

  bool get isStudent => profile?.isStudent ?? role == 'STUDENT';
  bool get isInstructor => profile?.isInstructor ?? role == 'INSTRUCTOR';
  bool get isFaculty => profile?.isFaculty ?? role == 'FACULTY';
  bool get isAdmin => profile?.isAdmin ?? role == 'ADMIN';

  // Helper method to check if profile has loaded
  bool get hasProfile => profile != null;
}

// ==================== Resources Provider ====================
final resourcesProvider = FutureProvider<List<Resource>>((ref) async {
  final apiService = ref.read(apiServiceProvider);
  return apiService.getResources();
});

// ==================== Resources by Type Provider ====================
final resourcesByTypeProvider = FutureProvider.family<List<Resource>, String>((
  ref,
  type,
) async {
  final apiService = ref.read(apiServiceProvider);
  return apiService.getResourcesByType(type);
});

// ==================== Available Resources by Type Provider ====================
final availableResourcesByTypeProvider =
    FutureProvider.family<List<Resource>, String>((ref, type) async {
      final apiService = ref.read(apiServiceProvider);
      return apiService.getAvailableResourcesByType(type);
    });

// ==================== My Bookings Provider ====================
final myBookingsProvider = FutureProvider.autoDispose<List<Booking>>((
  ref,
) async {
  final apiService = ref.read(apiServiceProvider);
  return apiService.getMyBookings();
});

// ==================== Single Resource Provider ====================
final resourceProvider = FutureProvider.autoDispose.family<Resource, int>((
  ref,
  id,
) async {
  final apiService = ref.read(apiServiceProvider);
  return apiService.getResourceById(id);
});

// ==================== Bookings for Resource Provider ====================
final bookingsForResourceProvider = FutureProvider.autoDispose
    .family<List<Booking>, int>((ref, resourceId) async {
      final apiService = ref.read(apiServiceProvider);
      return apiService.getBookingsForResource(resourceId);
    });
