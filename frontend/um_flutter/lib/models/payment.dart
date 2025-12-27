import 'package:flutter/material.dart';

class Payment {
  final int id;
  final int orderId;
  final String username;
  final double amount;
  final String paymentMethod;
  final String status;
  final String? transactionId;
  final String? failureReason;
  final DateTime createdAt;
  final DateTime updatedAt;

  Payment({
    required this.id,
    required this.orderId,
    required this.username,
    required this.amount,
    required this.paymentMethod,
    required this.status,
    this.transactionId,
    this.failureReason,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Payment.fromJson(Map<String, dynamic> json) {
    return Payment(
      id: json['id'],
      orderId: json['orderId'],
      username: json['username'],
      amount: (json['amount'] as num).toDouble(),
      paymentMethod: json['paymentMethod'],
      status: json['status'],
      transactionId: json['transactionId'],
      failureReason: json['failureReason'],
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }

  bool get isPending => status == 'PENDING';
  bool get isCompleted => status == 'COMPLETED';
  bool get isFailed => status == 'FAILED';
  bool get isRefunded => status == 'REFUNDED';

  Color get statusColor {
    switch (status) {
      case 'COMPLETED':
        return const Color(0xFF4CAF50); // Green
      case 'PENDING':
        return const Color(0xFFFF9800); // Orange
      case 'FAILED':
        return const Color(0xFFF44336); // Red
      case 'REFUNDED':
        return const Color(0xFF2196F3); // Blue
      default:
        return const Color(0xFF9E9E9E); // Grey
    }
  }

  IconData get statusIcon {
    switch (status) {
      case 'COMPLETED':
        return Icons.check_circle;
      case 'PENDING':
        return Icons.pending;
      case 'FAILED':
        return Icons.error;
      case 'REFUNDED':
        return Icons.refresh;
      default:
        return Icons.help;
    }
  }
}

enum PaymentMethod { creditCard, debitCard, paypal, bankTransfer, wallet }

extension PaymentMethodExtension on PaymentMethod {
  String get value {
    switch (this) {
      case PaymentMethod.creditCard:
        return 'CREDIT_CARD';
      case PaymentMethod.debitCard:
        return 'DEBIT_CARD';
      case PaymentMethod.paypal:
        return 'PAYPAL';
      case PaymentMethod.bankTransfer:
        return 'BANK_TRANSFER';
      case PaymentMethod.wallet:
        return 'WALLET';
    }
  }

  String get displayName {
    switch (this) {
      case PaymentMethod.creditCard:
        return 'Credit Card';
      case PaymentMethod.debitCard:
        return 'Debit Card';
      case PaymentMethod.paypal:
        return 'PayPal';
      case PaymentMethod.bankTransfer:
        return 'Bank Transfer';
      case PaymentMethod.wallet:
        return 'Wallet';
    }
  }

  IconData get icon {
    switch (this) {
      case PaymentMethod.creditCard:
        return Icons.credit_card;
      case PaymentMethod.debitCard:
        return Icons.payment;
      case PaymentMethod.paypal:
        return Icons.account_balance_wallet;
      case PaymentMethod.bankTransfer:
        return Icons.account_balance;
      case PaymentMethod.wallet:
        return Icons.wallet;
    }
  }
}

enum PaymentStatus { pending, completed, failed, refunded }

extension PaymentStatusExtension on PaymentStatus {
  String get value {
    switch (this) {
      case PaymentStatus.pending:
        return 'PENDING';
      case PaymentStatus.completed:
        return 'COMPLETED';
      case PaymentStatus.failed:
        return 'FAILED';
      case PaymentStatus.refunded:
        return 'REFUNDED';
    }
  }
}
