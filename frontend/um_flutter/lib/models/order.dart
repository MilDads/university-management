class MarketOrder {
  final int id;
  final String userId;
  final double totalAmount;
  final String status;
  final List<MarketOrderItem> items;
  final DateTime createdAt;
  final String? paymentId;

  MarketOrder({
    required this.id,
    required this.userId,
    required this.totalAmount,
    required this.status,
    required this.items,
    required this.createdAt,
    this.paymentId,
  });

  factory MarketOrder.fromJson(Map<String, dynamic> json) {
    return MarketOrder(
      id: json['id'],
      userId: json['userId'],
      totalAmount: (json['totalAmount'] as num).toDouble(),
      status: json['status'],
      items: (json['items'] as List)
          .map((item) => MarketOrderItem.fromJson(item))
          .toList(),
      createdAt: DateTime.parse(json['createdAt']),
      paymentId: json['paymentId'],
    );
  }
}

class MarketOrderItem {
  final int id;
  final int productId;
  final String productName;
  final int quantity;
  final double unitPrice;
  final double totalPrice;

  MarketOrderItem({
    required this.id,
    required this.productId,
    required this.productName,
    required this.quantity,
    required this.unitPrice,
    required this.totalPrice,
  });

  factory MarketOrderItem.fromJson(Map<String, dynamic> json) {
    return MarketOrderItem(
      id: json['id'],
      productId: json['productId'],
      productName: json['productName'],
      quantity: json['quantity'],
      unitPrice: (json['unitPrice'] as num).toDouble(),
      totalPrice: (json['totalPrice'] as num).toDouble(),
    );
  }
}