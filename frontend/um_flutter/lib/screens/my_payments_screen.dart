import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/payment.dart';
import '../providers/payment_providers.dart';

class MyPaymentsScreen extends ConsumerWidget {
  const MyPaymentsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final paymentsAsync = ref.watch(myPaymentsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('My Payments'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.invalidate(myPaymentsProvider),
          ),
        ],
      ),
      body: paymentsAsync.when(
        data: (payments) {
          if (payments.isEmpty) {
            return _buildEmptyState();
          }

          // Group payments by status
          final completedPayments = payments
              .where((p) => p.isCompleted)
              .toList();
          final pendingPayments = payments.where((p) => p.isPending).toList();
          final failedPayments = payments.where((p) => p.isFailed).toList();
          final refundedPayments = payments.where((p) => p.isRefunded).toList();

          return RefreshIndicator(
            onRefresh: () async => ref.invalidate(myPaymentsProvider),
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                // Summary Card
                _buildSummaryCard(payments),
                const SizedBox(height: 24),

                // Pending Payments
                if (pendingPayments.isNotEmpty) ...[
                  _buildSectionHeader(
                    'Pending Payments',
                    pendingPayments.length,
                  ),
                  ...pendingPayments.map(
                    (payment) => _PaymentCard(payment: payment),
                  ),
                  const SizedBox(height: 24),
                ],

                // Completed Payments
                if (completedPayments.isNotEmpty) ...[
                  _buildSectionHeader(
                    'Completed Payments',
                    completedPayments.length,
                  ),
                  ...completedPayments.map(
                    (payment) => _PaymentCard(payment: payment),
                  ),
                  const SizedBox(height: 24),
                ],

                // Failed Payments
                if (failedPayments.isNotEmpty) ...[
                  _buildSectionHeader('Failed Payments', failedPayments.length),
                  ...failedPayments.map(
                    (payment) => _PaymentCard(payment: payment),
                  ),
                  const SizedBox(height: 24),
                ],

                // Refunded Payments
                if (refundedPayments.isNotEmpty) ...[
                  _buildSectionHeader(
                    'Refunded Payments',
                    refundedPayments.length,
                  ),
                  ...refundedPayments.map(
                    (payment) => _PaymentCard(payment: payment),
                  ),
                ],
              ],
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 64, color: Colors.red),
              const SizedBox(height: 16),
              Text('Error: ${error.toString()}'),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => ref.invalidate(myPaymentsProvider),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.payment, size: 64, color: Colors.grey),
          SizedBox(height: 16),
          Text(
            'No payments found',
            style: TextStyle(fontSize: 18, color: Colors.grey),
          ),
          SizedBox(height: 8),
          Text(
            'Your payment history will appear here',
            style: TextStyle(color: Colors.grey),
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryCard(List<Payment> payments) {
    final totalSpent = payments
        .where((p) => p.isCompleted)
        .fold(0.0, (sum, p) => sum + p.amount);
    final pendingAmount = payments
        .where((p) => p.isPending)
        .fold(0.0, (sum, p) => sum + p.amount);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Payment Summary',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const Divider(),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _buildSummaryItem(
                  'Total Spent',
                  '\$${totalSpent.toStringAsFixed(2)}',
                  Colors.green,
                  Icons.check_circle,
                ),
                _buildSummaryItem(
                  'Pending',
                  '\$${pendingAmount.toStringAsFixed(2)}',
                  Colors.orange,
                  Icons.pending,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSummaryItem(
    String label,
    String value,
    Color color,
    IconData icon,
  ) {
    return Column(
      children: [
        Icon(icon, color: color, size: 32),
        const SizedBox(height: 8),
        Text(
          value,
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
        Text(label, style: const TextStyle(color: Colors.grey, fontSize: 12)),
      ],
    );
  }

  Widget _buildSectionHeader(String title, int count) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Text(
            title,
            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
            decoration: BoxDecoration(
              color: Colors.grey.shade300,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              count.toString(),
              style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
            ),
          ),
        ],
      ),
    );
  }
}

class _PaymentCard extends StatelessWidget {
  final Payment payment;

  const _PaymentCard({required this.payment});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {
          _showPaymentDetails(context, payment);
        },
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Icon(payment.statusIcon, color: payment.statusColor),
                      const SizedBox(width: 8),
                      Text(
                        'Payment #${payment.id}',
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                      ),
                    ],
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: payment.statusColor.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      payment.status,
                      style: TextStyle(
                        color: payment.statusColor,
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                      ),
                    ),
                  ),
                ],
              ),
              const Divider(),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Order #${payment.orderId}',
                        style: const TextStyle(
                          color: Colors.grey,
                          fontSize: 12,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        payment.paymentMethod.replaceAll('_', ' '),
                        style: const TextStyle(fontSize: 14),
                      ),
                    ],
                  ),
                  Text(
                    '\$${payment.amount.toStringAsFixed(2)}',
                    style: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    _formatDate(payment.createdAt),
                    style: const TextStyle(color: Colors.grey, fontSize: 12),
                  ),
                  if (payment.transactionId != null)
                    Text(
                      'Txn: ${payment.transactionId!.substring(0, 8)}...',
                      style: const TextStyle(color: Colors.grey, fontSize: 10),
                    ),
                ],
              ),
              if (payment.isFailed && payment.failureReason != null) ...[
                const SizedBox(height: 8),
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Colors.red.shade50,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Row(
                    children: [
                      const Icon(
                        Icons.error_outline,
                        size: 16,
                        color: Colors.red,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          payment.failureReason!,
                          style: const TextStyle(
                            fontSize: 12,
                            color: Colors.red,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  void _showPaymentDetails(BuildContext context, Payment payment) {
    showModalBottomSheet(
      context: context,
      builder: (context) => Container(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Payment Details',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
            const Divider(),
            _buildDetailRow('Payment ID', '#${payment.id}'),
            _buildDetailRow('Order ID', '#${payment.orderId}'),
            _buildDetailRow('Amount', '\$${payment.amount.toStringAsFixed(2)}'),
            _buildDetailRow(
              'Payment Method',
              payment.paymentMethod.replaceAll('_', ' '),
            ),
            _buildDetailRow('Status', payment.status),
            if (payment.transactionId != null)
              _buildDetailRow('Transaction ID', payment.transactionId!),
            _buildDetailRow('Created', _formatDate(payment.createdAt)),
            _buildDetailRow('Updated', _formatDate(payment.updatedAt)),
            if (payment.failureReason != null)
              _buildDetailRow('Failure Reason', payment.failureReason!),
          ],
        ),
      ),
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Colors.grey)),
          Flexible(
            child: Text(
              value,
              style: const TextStyle(fontWeight: FontWeight.w500),
              textAlign: TextAlign.right,
            ),
          ),
        ],
      ),
    );
  }

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year} ${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
  }
}
