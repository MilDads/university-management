import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../models/notification.dart' as model;
import '../providers/notification_providers.dart';

class NotificationsScreen extends ConsumerStatefulWidget {
  const NotificationsScreen({super.key});

  @override
  ConsumerState<NotificationsScreen> createState() =>
      _NotificationsScreenState();
}

class _NotificationsScreenState extends ConsumerState<NotificationsScreen> {
  String? _selectedFilter;

  @override
  Widget build(BuildContext context) {
    final notificationsAsync = ref.watch(myNotificationsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notifications'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              ref.invalidate(myNotificationsProvider);
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Filter Chips
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                FilterChip(
                  label: const Text('All'),
                  selected: _selectedFilter == null,
                  onSelected: (selected) {
                    setState(() => _selectedFilter = null);
                  },
                ),
                const SizedBox(width: 8),
                FilterChip(
                  label: const Text('Sent'),
                  selected: _selectedFilter == 'SENT',
                  onSelected: (selected) {
                    setState(() => _selectedFilter = selected ? 'SENT' : null);
                  },
                ),
                const SizedBox(width: 8),
                FilterChip(
                  label: const Text('Pending'),
                  selected: _selectedFilter == 'PENDING',
                  onSelected: (selected) {
                    setState(() {
                      _selectedFilter = selected ? 'PENDING' : null;
                    });
                  },
                ),
                const SizedBox(width: 8),
                FilterChip(
                  label: const Text('Failed'),
                  selected: _selectedFilter == 'FAILED',
                  onSelected: (selected) {
                    setState(() {
                      _selectedFilter = selected ? 'FAILED' : null;
                    });
                  },
                ),
              ],
            ),
          ),

          // Notifications List
          Expanded(
            child: notificationsAsync.when(
              data: (notifications) {
                // Apply filter
                final filteredNotifications = _selectedFilter == null
                    ? notifications
                    : notifications
                          .where((n) => n.status == _selectedFilter)
                          .toList();

                if (filteredNotifications.isEmpty) {
                  return Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.notifications_none,
                          size: 64,
                          color: Colors.grey.shade400,
                        ),
                        const SizedBox(height: 16),
                        Text(
                          'No notifications found',
                          style: TextStyle(
                            fontSize: 18,
                            color: Colors.grey.shade600,
                          ),
                        ),
                      ],
                    ),
                  );
                }

                return RefreshIndicator(
                  onRefresh: () async {
                    ref.invalidate(myNotificationsProvider);
                  },
                  child: ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: filteredNotifications.length,
                    itemBuilder: (context, index) {
                      final notification = filteredNotifications[index];
                      return _NotificationCard(notification: notification);
                    },
                  ),
                );
              },
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (error, stack) => Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(
                      Icons.error_outline,
                      size: 64,
                      color: Colors.red,
                    ),
                    const SizedBox(height: 16),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 32),
                      child: Text(
                        error.toString(),
                        textAlign: TextAlign.center,
                        style: const TextStyle(color: Colors.red),
                      ),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () {
                        ref.invalidate(myNotificationsProvider);
                      },
                      child: const Text('Retry'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _NotificationCard extends StatelessWidget {
  final model.Notification notification;

  const _NotificationCard({required this.notification});

  IconData _getIconForType(String type) {
    switch (type) {
      case 'USER_REGISTRATION':
        return Icons.person_add;
      case 'BOOKING_CONFIRMATION':
        return Icons.event_available;
      case 'BOOKING_CANCELLATION':
        return Icons.event_busy;
      case 'ORDER_CONFIRMATION':
        return Icons.shopping_cart;
      case 'PAYMENT_SUCCESS':
        return Icons.payment;
      case 'PAYMENT_FAILURE':
        return Icons.error_outline;
      case 'EXAM_CREATED':
        return Icons.assignment;
      case 'EXAM_SUBMITTED':
        return Icons.assignment_turned_in;
      case 'EXAM_GRADED':
        return Icons.grade;
      default:
        return Icons.notifications;
    }
  }

  Color _getColorForStatus(String status) {
    switch (status) {
      case 'SENT':
        return Colors.green;
      case 'PENDING':
        return Colors.orange;
      case 'FAILED':
        return Colors.red;
      case 'RETRY':
        return Colors.blue;
      default:
        return Colors.grey;
    }
  }

  String _formatDate(DateTime date) {
    final now = DateTime.now();
    final difference = now.difference(date);

    if (difference.inMinutes < 1) {
      return 'Just now';
    } else if (difference.inMinutes < 60) {
      return '${difference.inMinutes}m ago';
    } else if (difference.inHours < 24) {
      return '${difference.inHours}h ago';
    } else if (difference.inDays < 7) {
      return '${difference.inDays}d ago';
    } else {
      return DateFormat('MMM dd, yyyy').format(date);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {
          // Show notification details dialog
          showDialog(
            context: context,
            builder: (context) =>
                _NotificationDetailsDialog(notification: notification),
          );
        },
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: _getColorForStatus(
                    notification.status,
                  ).withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  _getIconForType(notification.type),
                  color: _getColorForStatus(notification.status),
                  size: 24,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            notification.subject,
                            style: Theme.of(context).textTheme.titleMedium
                                ?.copyWith(
                                  fontWeight: notification.isSent
                                      ? FontWeight.normal
                                      : FontWeight.bold,
                                ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          decoration: BoxDecoration(
                            color: _getColorForStatus(
                              notification.status,
                            ).withValues(alpha: 0.1),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            notification.status,
                            style: TextStyle(
                              color: _getColorForStatus(notification.status),
                              fontSize: 10,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      _formatDate(notification.createdAt),
                      style: Theme.of(
                        context,
                      ).textTheme.bodySmall?.copyWith(color: Colors.grey),
                    ),
                    if (notification.retryCount > 0) ...[
                      const SizedBox(height: 4),
                      Text(
                        'Retried ${notification.retryCount} time(s)',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: Colors.orange,
                          fontSize: 11,
                        ),
                      ),
                    ],
                  ],
                ),
              ),
              const Icon(Icons.chevron_right, color: Colors.grey),
            ],
          ),
        ),
      ),
    );
  }
}

class _NotificationDetailsDialog extends StatelessWidget {
  final model.Notification notification;

  const _NotificationDetailsDialog({required this.notification});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(notification.subject),
      content: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            _DetailRow(
              label: 'Type',
              value: notification.type.replaceAll('_', ' '),
            ),
            _DetailRow(label: 'Status', value: notification.status),
            _DetailRow(label: 'Recipient', value: notification.recipientEmail),
            _DetailRow(
              label: 'Created',
              value: DateFormat(
                'MMM dd, yyyy HH:mm',
              ).format(notification.createdAt),
            ),
            if (notification.sentAt != null)
              _DetailRow(
                label: 'Sent',
                value: DateFormat(
                  'MMM dd, yyyy HH:mm',
                ).format(notification.sentAt!),
              ),
            if (notification.retryCount > 0)
              _DetailRow(
                label: 'Retry Count',
                value: notification.retryCount.toString(),
              ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Close'),
        ),
      ],
    );
  }
}

class _DetailRow extends StatelessWidget {
  final String label;
  final String value;

  const _DetailRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(
              '$label:',
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                color: Colors.grey,
              ),
            ),
          ),
          Expanded(child: Text(value, style: const TextStyle(fontSize: 14))),
        ],
      ),
    );
  }
}
