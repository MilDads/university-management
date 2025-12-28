import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/resource.dart';
import '../providers/app_providers.dart';
import 'add_resource_screen.dart';
import 'my_bookings_screen.dart';
import 'create_booking_screen.dart';
import 'marketplace_screen.dart';
import 'placeholder_screen.dart';

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  String? _selectedType;

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authProvider);
    final user = authState.value;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Resources'),
        actions: [
          PopupMenuButton<String>(
// ... (rest of AppBar actions same as before)
            icon: const Icon(Icons.account_circle),
            itemBuilder: (context) => [
              PopupMenuItem(
                enabled: false,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      user?.username ?? 'User',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                    Text(
                      user?.role ?? '',
                      style: const TextStyle(color: Colors.grey, fontSize: 12),
                    ),
                  ],
                ),
              ),
              const PopupMenuDivider(),
               const PopupMenuItem(
                value: 'my_bookings',
                child: Row(
                  children: [
                    Icon(Icons.calendar_month),
                    SizedBox(width: 8),
                    Text('My Bookings'),
                  ],
                ),
              ),
              const PopupMenuDivider(),
              const PopupMenuItem(
                value: 'logout',
                child: Row(
                  children: [
                    Icon(Icons.logout),
                    SizedBox(width: 8),
                    Text('Logout'),
                  ],
                ),
              ),
            ],
            onSelected: (value) {
              if (value == 'logout') {
                ref.read(authProvider.notifier).logout();
              } else if (value == 'my_bookings') {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const MyBookingsScreen(),
                  ),
                );
              }
            },
          ),
        ],
      ),
      drawer: Drawer(
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            UserAccountsDrawerHeader(
              accountName: Text(user?.username ?? 'User'),
              accountEmail: Text(user?.role ?? ''),
              currentAccountPicture: const CircleAvatar(
                child: Icon(Icons.person, size: 40),
              ),
              decoration: BoxDecoration(color: Theme.of(context).primaryColor),
            ),
            ListTile(
              leading: const Icon(Icons.inventory_2),
              title: const Text('Resources'),
              selected: true,
              onTap: () => Navigator.pop(context),
            ),
            ListTile(
              leading: const Icon(Icons.calendar_month),
              title: const Text('My Bookings'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const MyBookingsScreen()),
                );
              },
            ),
            const Divider(),
            ListTile(
              leading: const Icon(Icons.shopping_cart),
              title: const Text('Marketplace'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const MarketplaceScreen(),
                  ),
                );
              },
            ),
            ListTile(
              leading: const Icon(Icons.assignment),
              title: const Text('Online Exams'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const ServicePlaceholderScreen(
                      title: 'Online Exams',
                      icon: Icons.assignment,
                      description: 'Take and manage your online university exams.',
                    ),
                  ),
                );
              },
            ),
            ListTile(
              leading: const Icon(Icons.sensors),
              title: const Text('IoT Devices'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const ServicePlaceholderScreen(
                      title: 'IoT Devices',
                      icon: Icons.sensors,
                      description: 'View real-time sensor data from campus facilities.',
                    ),
                  ),
                );
              },
            ),
            ListTile(
              leading: const Icon(Icons.directions_bus),
              title: const Text('Shuttle Tracking'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const ServicePlaceholderScreen(
                      title: 'Shuttle Tracking',
                      icon: Icons.directions_bus,
                      description: 'Track university shuttle locations in real-time.',
                    ),
                  ),
                );
              },
            ),
            const Divider(),
            ListTile(
              leading: const Icon(Icons.notifications),
              title: const Text('Notifications'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const ServicePlaceholderScreen(
                      title: 'Notifications',
                      icon: Icons.notifications,
                      description: 'Stay updated with university announcements.',
                    ),
                  ),
                );
              },
            ),
            ListTile(
              leading: const Icon(Icons.person),
              title: const Text('Profile'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const ServicePlaceholderScreen(
                      title: 'User Profile',
                      icon: Icons.person,
                      description: 'Manage your personal information and settings.',
                    ),
                  ),
                );
              },
            ),
            const Divider(),
            ListTile(
              leading: const Icon(Icons.logout),
              title: const Text('Logout'),
              onTap: () {
                Navigator.pop(context);
                ref.read(authProvider.notifier).logout();
              },
            ),
          ],
        ),
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
                  selected: _selectedType == null,
                  onSelected: (selected) {
                    setState(() => _selectedType = null);
                  },
                ),
                const SizedBox(width: 8),
                ...ResourceType.values.map((type) {
                  return Padding(
                    padding: const EdgeInsets.only(right: 8.0),
                    child: FilterChip(
                      label: Text(type.displayName),
                      selected: _selectedType == type.value,
                      onSelected: (selected) {
                        setState(() {
                          _selectedType = selected ? type.value : null;
                        });
                      },
                    ),
                  );
                }),
              ],
            ),
          ),
          // Resource List
          Expanded(child: _buildResourceList()),
        ],
      ),
      floatingActionButton: user?.isFaculty == true
          ? FloatingActionButton.extended(
              onPressed: () async {
                final result = await Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const AddResourceScreen(),
                  ),
                );
                if (result == true) {
                  ref.invalidate(resourcesProvider);
                }
              },
              icon: const Icon(Icons.add),
              label: const Text('Add Resource'),
            )
          : null,
    );
  }

  Widget _buildResourceList() {
    final resourcesAsync = _selectedType == null
        ? ref.watch(resourcesProvider)
        : ref.watch(resourcesByTypeProvider(_selectedType!));

    return resourcesAsync.when(
      data: (resources) {
        if (resources.isEmpty) {
          return const Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.inventory_2_outlined, size: 64, color: Colors.grey),
                SizedBox(height: 16),
                Text(
                  'No resources found',
                  style: TextStyle(fontSize: 18, color: Colors.grey),
                ),
              ],
            ),
          );
        }

        return RefreshIndicator(
          onRefresh: () async {
            ref.invalidate(resourcesProvider);
          },
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: resources.length,
            itemBuilder: (context, index) {
              return _ResourceCard(resource: resources[index]);
            },
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
            Text(
              'Error: ${error.toString()}',
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.red),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => ref.invalidate(resourcesProvider),
              child: const Text('Retry'),
            ),
          ],
        ),
      ),
    );
  }
}

class _ResourceCard extends StatelessWidget {
  final Resource resource;

  const _ResourceCard({required this.resource});

  IconData _getIconForType(String type) {
    switch (type) {
      case 'LAB':
        return Icons.science;
      case 'ROOM':
        return Icons.meeting_room;
      case 'EQUIPMENT':
        return Icons.devices;
      case 'SHUTTLE':
        return Icons.directions_bus;
      case 'STUDY_SPACE':
        return Icons.book;
      case 'SPORTS_FACILITY':
        return Icons.sports;
      default:
        return Icons.category;
    }
  }

  Color _getColorForStatus(String status) {
    switch (status) {
      case 'AVAILABLE':
        return Colors.green;
      case 'BOOKED':
        return Colors.orange;
      case 'MAINTENANCE':
        return Colors.blue;
      case 'UNAVAILABLE':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: InkWell(
        onTap: () {
          // TODO: Navigate to resource details
        },
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    _getIconForType(resource.type),
                    size: 32,
                    color: Theme.of(context).primaryColor,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          resource.name,
                          style: Theme.of(context).textTheme.titleLarge,
                        ),
                        Text(
                          resource.type,
                          style: Theme.of(
                            context,
                          ).textTheme.bodySmall?.copyWith(color: Colors.grey),
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 6,
                    ),
                    decoration: BoxDecoration(
                      color: _getColorForStatus(
                        resource.status,
                      ).withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      resource.status,
                      style: TextStyle(
                        color: _getColorForStatus(resource.status),
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                      ),
                    ),
                  ),
                ],
              ),
              if (resource.description != null &&
                  resource.description!.isNotEmpty) ...[
                const SizedBox(height: 12),
                Text(
                  resource.description!,
                  style: Theme.of(context).textTheme.bodyMedium,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
              const SizedBox(height: 12),
              Row(
                children: [
                  const Icon(Icons.location_on, size: 16, color: Colors.grey),
                  const SizedBox(width: 4),
                  Expanded(
                    child: Text(
                      resource.location,
                      style: const TextStyle(color: Colors.grey),
                    ),
                  ),
                  const SizedBox(width: 16),
                  const Icon(Icons.people, size: 16, color: Colors.grey),
                  const SizedBox(width: 4),
                  Text(
                    'Capacity: ${resource.capacity}',
                    style: const TextStyle(color: Colors.grey),
                  ),
                ],
              ),
              if (resource.status == 'AVAILABLE') ...[
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder:
                              (context) =>
                                  CreateBookingScreen(resource: resource),
                        ),
                      );
                    },
                    child: const Text('Book Now'),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
