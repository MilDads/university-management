import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/product.dart';
import '../providers/marketplace_providers.dart';
import '../providers/app_providers.dart';
import 'my_orders_screen.dart';
import 'add_product_screen.dart';
import 'payment_status_screen.dart';
import 'my_payments_screen.dart';

class MarketplaceScreen extends ConsumerWidget {
  const MarketplaceScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final productsAsync = ref.watch(productsProvider);
    final userInfo = ref.watch(currentUserInfoProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Marketplace'),
        actions: [
          IconButton(
            icon: const Icon(Icons.payment),
            tooltip: 'My Payments',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => const MyPaymentsScreen(),
                ),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.history),
            tooltip: 'My Orders',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const MyOrdersScreen()),
              );
            },
          ),
        ],
      ),
      body: productsAsync.when(
        data: (products) {
          if (products.isEmpty) {
            return const Center(child: Text('No products available.'));
          }
          return RefreshIndicator(
            onRefresh: () async => ref.invalidate(productsProvider),
            child: GridView.builder(
              padding: const EdgeInsets.all(16),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                childAspectRatio: 0.70,
                crossAxisSpacing: 16,
                mainAxisSpacing: 16,
              ),
              itemCount: products.length,
              itemBuilder: (context, index) {
                return _ProductCard(product: products[index]);
              },
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('Error: $e'),
              ElevatedButton(
                onPressed: () => ref.invalidate(productsProvider),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      ),
      // FAB for FACULTY users only - ✅ Check userInfo.isFaculty
      floatingActionButton: userInfo.isFaculty
          ? FloatingActionButton.extended(
              onPressed: () async {
                final result = await Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const AddProductScreen(),
                  ),
                );
                if (result == true) {
                  ref.invalidate(productsProvider);
                }
              },
              icon: const Icon(Icons.add),
              label: const Text('Add Product'),
            )
          : null,
    );
  }
}

class _ProductCard extends ConsumerWidget {
  final Product product;

  const _ProductCard({required this.product});

  Future<void> _buyProduct(BuildContext context, WidgetRef ref) async {
    final quantity = await showDialog<int>(
      context: context,
      builder: (context) => _BuyDialog(product: product),
    );

    if (quantity != null && context.mounted) {
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => const Center(
          child: Card(
            child: Padding(
              padding: EdgeInsets.all(24.0),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  CircularProgressIndicator(),
                  SizedBox(height: 16),
                  Text('Creating order...'),
                ],
              ),
            ),
          ),
        ),
      );

      try {
        final order = await ref
            .read(marketplaceServiceProvider)
            .createOrder(product.id, quantity);

        if (context.mounted) {
          Navigator.pop(context);
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => PaymentStatusScreen(orderId: order.id),
            ),
          );
          ref.invalidate(productsProvider);
        }
      } catch (e) {
        if (context.mounted) {
          Navigator.pop(context);
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Failed to create order: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  color: Colors.grey.shade200,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: const Icon(Icons.image, size: 50, color: Colors.grey),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              product.name,
              style: const TextStyle(fontWeight: FontWeight.bold),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            Text(
              product.category,
              style: const TextStyle(fontSize: 10, color: Colors.blue),
            ),
            const SizedBox(height: 4),
            Text(
              product.description,
              style: const TextStyle(fontSize: 12, color: Colors.grey),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 4),
            Row(
              children: [
                Text(
                  'Stock: ${product.stock}',
                  style: const TextStyle(fontSize: 10, color: Colors.grey),
                ),
                const SizedBox(width: 8),
                if (!product.active)
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 6,
                      vertical: 2,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.red.shade100,
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: const Text(
                      'Inactive',
                      style: TextStyle(fontSize: 8, color: Colors.red),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '\$${product.price.toStringAsFixed(2)}',
                  style: TextStyle(
                    color: Theme.of(context).primaryColor,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.add_shopping_cart),
                  onPressed: product.stock > 0 && product.active
                      ? () => _buyProduct(context, ref)
                      : null,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _BuyDialog extends StatefulWidget {
  final Product product;
  const _BuyDialog({required this.product});

  @override
  State<_BuyDialog> createState() => _BuyDialogState();
}

class _BuyDialogState extends State<_BuyDialog> {
  int _quantity = 1;

  @override
  Widget build(BuildContext context) {
    final totalAmount = widget.product.price * _quantity;

    return AlertDialog(
      title: Text('Buy ${widget.product.name}'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Price: \$${widget.product.price.toStringAsFixed(2)}'),
          const SizedBox(height: 16),
          const Text(
            'Quantity:',
            style: TextStyle(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              IconButton(
                onPressed: _quantity > 1
                    ? () => setState(() => _quantity--)
                    : null,
                icon: const Icon(Icons.remove_circle_outline),
                iconSize: 32,
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 24,
                  vertical: 8,
                ),
                decoration: BoxDecoration(
                  border: Border.all(color: Colors.grey),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  '$_quantity',
                  style: const TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              IconButton(
                onPressed: _quantity < widget.product.stock
                    ? () => setState(() => _quantity++)
                    : null,
                icon: const Icon(Icons.add_circle_outline),
                iconSize: 32,
              ),
            ],
          ),
          const SizedBox(height: 16),
          const Divider(),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Total:',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              Text(
                '\$${totalAmount.toStringAsFixed(2)}',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Theme.of(context).primaryColor,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.blue.shade50,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              children: [
                Icon(Icons.info_outline, size: 20, color: Colors.blue.shade700),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Payment will be processed automatically after order creation.',
                    style: TextStyle(fontSize: 12, color: Colors.blue.shade700),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        ElevatedButton.icon(
          onPressed: () => Navigator.pop(context, _quantity),
          icon: const Icon(Icons.shopping_cart_checkout),
          label: const Text('Proceed to Payment'),
        ),
      ],
    );
  }
}
