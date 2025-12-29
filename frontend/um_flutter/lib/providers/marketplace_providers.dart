import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'app_providers.dart';
import '../services/marketplace_service.dart';
import '../models/product.dart';
import '../models/order.dart';

final marketplaceServiceProvider = Provider<MarketplaceService>((ref) {
  final authState = ref.watch(authProvider);
  final token = authState.value?.token;
  return MarketplaceService(token);
});

final productsProvider = FutureProvider.autoDispose<List<Product>>((ref) async {
  final service = ref.watch(marketplaceServiceProvider);
  return service.getProducts();
});

final myOrdersProvider = FutureProvider.autoDispose<List<MarketOrder>>((ref) async {
  final service = ref.watch(marketplaceServiceProvider);
  return service.getMyOrders();
});
