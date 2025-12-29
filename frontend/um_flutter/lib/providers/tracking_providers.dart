import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/tracking.dart';
import '../services/tracking_service.dart';
import 'app_providers.dart';

// TrackingService provider
final trackingServiceProvider = Provider<TrackingService>((ref) {
  final authState = ref.watch(authProvider);
  final token = authState.value?.token;
  return TrackingService(token);
});

// Active shuttles provider - auto-refreshes every 10 seconds
final activeShuttlesProvider = StreamProvider<List<ShuttleLocation>>((ref) {
  final trackingService = ref.watch(trackingServiceProvider);

  return Stream.periodic(const Duration(seconds: 10)).asyncMap((_) async {
    return await trackingService.getActiveShuttles();
  });
});

// Single shuttle location provider
final shuttleLocationProvider = FutureProvider.family<ShuttleLocation, int>((
  ref,
  shuttleId,
) async {
  final trackingService = ref.watch(trackingServiceProvider);
  return await trackingService.getShuttleLocation(shuttleId);
});

// All shuttles provider (admin only)
final allShuttlesProvider = FutureProvider<List<Shuttle>>((ref) async {
  final trackingService = ref.watch(trackingServiceProvider);
  return await trackingService.getAllShuttles();
});

// Alternative: Manual refresh provider (no auto-refresh)
final activeShuttlesManualProvider = FutureProvider<List<ShuttleLocation>>((
  ref,
) async {
  final trackingService = ref.watch(trackingServiceProvider);
  return await trackingService.getActiveShuttles();
});
