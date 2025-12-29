import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/marketplace_providers.dart';

class MyOrdersScreen extends ConsumerWidget {
  const MyOrdersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final ordersAsync = ref.watch(myOrdersProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('My Orders')),
      body: ordersAsync.when(
        data: (orders) {
          if (orders.isEmpty) {
            return const Center(child: Text('No orders found.'));
          }
          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: orders.length,
            itemBuilder: (context, index) {
              final order = orders[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 16),
                child: ExpansionTile(
                  leading: const Icon(Icons.shopping_bag),
                  title: Text('Order #${order.id}'),
                  subtitle: Text('Status: ${order.status} - Total: \$${order.totalAmount.toStringAsFixed(2)}'),
                  children: [
                    ListView.builder(
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      itemCount: order.items.length,
                      itemBuilder: (context, itemIndex) {
                        final item = order.items[itemIndex];
                        return ListTile(
                          title: Text(item.productName),
                          subtitle: Text('Qty: ${item.quantity} x \$${item.unitPrice.toStringAsFixed(2)}'),
                          trailing: Text('\$${item.totalPrice.toStringAsFixed(2)}'),
                        );
                      },
                    ),
                    Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text('Date: ${order.createdAt.toLocal().toString().split(' ')[0]}'),
                          if (order.paymentId != null)
                             Text('Payment ID: ${order.paymentId}', style: const TextStyle(fontSize: 10, color: Colors.grey)),
                        ],
                      ),
                    )
                  ],
                ),
              );
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
      ),
    );
  }
}