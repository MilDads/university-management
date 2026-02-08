import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/notification.dart';
import '../services/notification_service.dart';
import 'app_providers.dart';

// ==================== Notification Service Provider ====================
final notificationServiceProvider = Provider<NotificationService>((ref) {
  final authState = ref.watch(authProvider);
  final token = authState.value?.token;
  return NotificationService(token);
});

// ==================== My Notifications Provider ====================
final myNotificationsProvider = FutureProvider.autoDispose<List<Notification>>((
  ref,
) async {
  final notificationService = ref.read(notificationServiceProvider);
  return notificationService.getMyNotifications();
});

// ==================== Notifications by Email Provider ====================
final notificationsByEmailProvider = FutureProvider.autoDispose
    .family<List<Notification>, String>((ref, email) async {
      final notificationService = ref.read(notificationServiceProvider);
      return notificationService.getNotificationsByEmail(email);
    });

// ==================== Notification Statistics Provider ====================
final notificationStatsProvider = FutureProvider.autoDispose<Map<String, int>>((
  ref,
) async {
  final notificationService = ref.read(notificationServiceProvider);
  return notificationService.getNotificationStats();
});

// ==================== Unread Notifications Count Provider ====================
final unreadNotificationsCountProvider = FutureProvider.autoDispose<int>((
  ref,
) async {
  final notifications = await ref.watch(myNotificationsProvider.future);
  // For now, we'll consider PENDING notifications as "unread"
  // You can add a 'read' field to the backend model for better tracking
  return notifications.where((n) => n.isPending || !n.isSent).length;
});

// ==================== Recent Notifications Provider (Last 10) ====================
final recentNotificationsProvider =
    FutureProvider.autoDispose<List<Notification>>((ref) async {
      final notifications = await ref.watch(myNotificationsProvider.future);
      // Sort by creation date (most recent first) and take top 10
      final sorted = notifications.toList()
        ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
      return sorted.take(10).toList();
    });
