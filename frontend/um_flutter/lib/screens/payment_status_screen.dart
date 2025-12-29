import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/payment.dart';
import '../providers/payment_providers.dart';
import '../providers/marketplace_providers.dart';

class PaymentStatusScreen extends ConsumerStatefulWidget {
  final int orderId;

  const PaymentStatusScreen({super.key, required this.orderId});

  @override
  ConsumerState<PaymentStatusScreen> createState() =>
      _PaymentStatusScreenState();
}

class _PaymentStatusScreenState extends ConsumerState<PaymentStatusScreen> {
  @override
  void initState() {
    super.initState();
    // Start checking payment status
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref
          .read(paymentStatusProvider.notifier)
          .checkPaymentStatus(widget.orderId);
    });
  }

  @override
  Widget build(BuildContext context) {
    final paymentState = ref.watch(paymentStatusProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Payment Status'),
        automaticallyImplyLeading: false,
      ),
      body: paymentState.when(
        data: (payment) {
          if (payment == null) {
            return const Center(child: CircularProgressIndicator());
          }

          return _buildPaymentResult(context, payment);
        },
        loading: () => _buildLoadingState(),
        error: (error, stack) => _buildErrorState(context, error),
      ),
    );
  }

  Widget _buildLoadingState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const CircularProgressIndicator(),
          const SizedBox(height: 24),
          const Text(
            'Processing Payment...',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w500),
          ),
          const SizedBox(height: 8),
          Text(
            'Order #${widget.orderId}',
            style: const TextStyle(color: Colors.grey),
          ),
          const SizedBox(height: 24),
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 32),
            child: Text(
              'Please wait while we confirm your payment',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(BuildContext context, Object error) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 80, color: Colors.red),
            const SizedBox(height: 24),
            const Text(
              'Payment Status Check Failed',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            Text(
              error.toString(),
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 32),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton(
                  onPressed: () {
                    ref
                        .read(paymentStatusProvider.notifier)
                        .checkPaymentStatus(widget.orderId);
                  },
                  child: const Text('Retry'),
                ),
                const SizedBox(width: 16),
                OutlinedButton(
                  onPressed: () {
                    Navigator.of(context).popUntil((route) => route.isFirst);
                  },
                  child: const Text('Go Home'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPaymentResult(BuildContext context, Payment payment) {
    final isSuccess = payment.isCompleted;
    final color = payment.statusColor;
    final icon = payment.statusIcon;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const SizedBox(height: 40),
          // Status Icon
          Icon(icon, size: 100, color: color),
          const SizedBox(height: 24),
          // Status Title
          Text(
            isSuccess
                ? 'Payment Successful!'
                : payment.isFailed
                ? 'Payment Failed'
                : payment.status,
            style: TextStyle(
              fontSize: 28,
              fontWeight: FontWeight.bold,
              color: color,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          // Message
          Text(
            isSuccess
                ? 'Your order has been confirmed and payment processed successfully.'
                : payment.isFailed
                ? payment.failureReason ?? 'The payment could not be processed.'
                : 'Payment is ${payment.status.toLowerCase()}',
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 16, color: Colors.grey),
          ),
          const SizedBox(height: 40),
          // Payment Details Card
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Payment Details',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const Divider(),
                  _buildDetailRow('Order ID', '#${payment.orderId}'),
                  _buildDetailRow('Payment ID', '#${payment.id}'),
                  if (payment.transactionId != null)
                    _buildDetailRow('Transaction ID', payment.transactionId!),
                  _buildDetailRow(
                    'Amount',
                    '\$${payment.amount.toStringAsFixed(2)}',
                  ),
                  _buildDetailRow(
                    'Payment Method',
                    payment.paymentMethod.replaceAll('_', ' '),
                  ),
                  _buildDetailRow('Status', payment.status),
                  _buildDetailRow('Date', _formatDate(payment.createdAt)),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),
          // Action Buttons
          if (isSuccess) ...[
            ElevatedButton(
              onPressed: () {
                // Invalidate orders to refresh
                ref.invalidate(myOrdersProvider);
                ref.invalidate(productsProvider);
                Navigator.of(context).popUntil((route) => route.isFirst);
              },
              child: const Padding(
                padding: EdgeInsets.symmetric(vertical: 16),
                child: Text('Continue Shopping'),
              ),
            ),
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: () {
                ref.invalidate(myOrdersProvider);
                Navigator.of(context).popUntil((route) => route.isFirst);
                // TODO: Navigate to My Orders
              },
              child: const Padding(
                padding: EdgeInsets.symmetric(vertical: 16),
                child: Text('View My Orders'),
              ),
            ),
          ] else if (payment.isFailed) ...[
            ElevatedButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              style: ElevatedButton.styleFrom(backgroundColor: Colors.orange),
              child: const Padding(
                padding: EdgeInsets.symmetric(vertical: 16),
                child: Text('Try Again'),
              ),
            ),
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: () {
                Navigator.of(context).popUntil((route) => route.isFirst);
              },
              child: const Padding(
                padding: EdgeInsets.symmetric(vertical: 16),
                child: Text('Go Home'),
              ),
            ),
          ] else ...[
            ElevatedButton(
              onPressed: () {
                ref
                    .read(paymentStatusProvider.notifier)
                    .checkPaymentStatus(widget.orderId);
              },
              child: const Padding(
                padding: EdgeInsets.symmetric(vertical: 16),
                child: Text('Refresh Status'),
              ),
            ),
          ],
          if (isSuccess) ...[
            const SizedBox(height: 24),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.green.shade50,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  Icon(Icons.info_outline, color: Colors.green.shade700),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'A confirmation email has been sent to your registered email address.',
                      style: TextStyle(color: Colors.green.shade700),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ],
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
          Text(value, style: const TextStyle(fontWeight: FontWeight.w500)),
        ],
      ),
    );
  }

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year} ${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
  }
}
