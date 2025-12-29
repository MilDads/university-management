import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import '../models/payment.dart';
import '../services/payment_service.dart';
import 'app_providers.dart';

/// Payment Service Provider
final paymentServiceProvider = Provider<PaymentService>((ref) {
  final authState = ref.watch(authProvider);
  final token = authState.value?.token;
  return PaymentService(token);
});

/// Get payment by ID
final paymentProvider = FutureProvider.family<Payment, int>((
  ref,
  paymentId,
) async {
  final service = ref.watch(paymentServiceProvider);
  return await service.getPaymentById(paymentId);
});

/// Get payment by order ID
final paymentByOrderProvider = FutureProvider.family<Payment, int>((
  ref,
  orderId,
) async {
  final service = ref.watch(paymentServiceProvider);
  return await service.getPaymentByOrderId(orderId);
});

/// Get all payments for current user
final myPaymentsProvider = FutureProvider.autoDispose<List<Payment>>((
  ref,
) async {
  final service = ref.watch(paymentServiceProvider);
  return await service.getMyPayments();
});

/// Payment status notifier for real-time updates
class PaymentStatusNotifier extends StateNotifier<AsyncValue<Payment?>> {
  final PaymentService _service;

  PaymentStatusNotifier(this._service) : super(const AsyncValue.data(null));

  Future<void> checkPaymentStatus(int orderId) async {
    state = const AsyncValue.loading();
    try {
      final payment = await _service.pollPaymentStatus(orderId);
      state = AsyncValue.data(payment);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  void reset() {
    state = const AsyncValue.data(null);
  }
}

/// Payment status provider
final paymentStatusProvider =
    StateNotifierProvider<PaymentStatusNotifier, AsyncValue<Payment?>>((ref) {
      final service = ref.watch(paymentServiceProvider);
      return PaymentStatusNotifier(service);
    });
