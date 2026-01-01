import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:intl/intl.dart';
import '../providers/tracking_providers.dart';
import '../providers/app_providers.dart';
import '../models/tracking.dart';

class TrackingScreen extends ConsumerStatefulWidget {
  const TrackingScreen({super.key});

  @override
  ConsumerState<TrackingScreen> createState() => _TrackingScreenState();
}

class _TrackingScreenState extends ConsumerState<TrackingScreen> {
  bool _showMapView = true; // Default to map view
  bool _showAllShuttles = false;
  bool _autoRefresh = true;
  int? _selectedShuttleId;
  final MapController _mapController = MapController();

  @override
  Widget build(BuildContext context) {
    final userInfo = ref.watch(currentUserInfoProvider);
    final isFaculty = userInfo.isFaculty;

    final shuttlesAsync = _showAllShuttles
        ? ref.watch(allShuttlesProvider)
        : (_autoRefresh
              ? ref.watch(activeShuttlesProvider)
              : ref.watch(activeShuttlesManualProvider));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Shuttle Tracking'),
        actions: [
          // View mode toggle
          IconButton(
            icon: Icon(_showMapView ? Icons.list : Icons.map),
            tooltip: _showMapView ? 'List View' : 'Map View',
            onPressed: () {
              setState(() {
                _showMapView = !_showMapView;
              });
            },
          ),
          // View toggle (Faculty only)
          if (isFaculty)
            PopupMenuButton<String>(
              icon: const Icon(Icons.filter_list),
              onSelected: (value) {
                setState(() {
                  _showAllShuttles = value == 'all';
                });
              },
              itemBuilder: (context) => [
                PopupMenuItem(
                  value: 'active',
                  child: Row(
                    children: [
                      Icon(
                        Icons.check_circle,
                        color: _showAllShuttles ? Colors.grey : Colors.green,
                      ),
                      const SizedBox(width: 8),
                      const Text('Active Only'),
                    ],
                  ),
                ),
                PopupMenuItem(
                  value: 'all',
                  child: Row(
                    children: [
                      Icon(
                        Icons.list,
                        color: _showAllShuttles ? Colors.blue : Colors.grey,
                      ),
                      const SizedBox(width: 8),
                      const Text('All Shuttles'),
                    ],
                  ),
                ),
              ],
            ),
          // Auto-refresh toggle
          if (!_showAllShuttles)
            IconButton(
              icon: Icon(_autoRefresh ? Icons.pause_circle : Icons.play_circle),
              tooltip: _autoRefresh
                  ? 'Disable auto-refresh'
                  : 'Enable auto-refresh',
              onPressed: () {
                setState(() {
                  _autoRefresh = !_autoRefresh;
                });
              },
            ),
          // Manual refresh
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'Refresh now',
            onPressed: _refreshList,
          ),
        ],
      ),
      body: Column(
        children: [
          _buildStatusBanner(isFaculty),
          Expanded(
            child: _showMapView
                ? _buildMapView(shuttlesAsync)
                : _buildListView(shuttlesAsync, isFaculty),
          ),
        ],
      ),
      floatingActionButton: isFaculty
          ? FloatingActionButton.extended(
              onPressed: () => _showAddShuttleDialog(context),
              icon: const Icon(Icons.add),
              label: const Text('Add Shuttle'),
            )
          : null,
    );
  }

  Widget _buildStatusBanner(bool isFaculty) {
    if (!_autoRefresh && !_showAllShuttles) return const SizedBox.shrink();

    Color bgColor;
    Color textColor;
    String message;
    IconData icon;

    if (_showAllShuttles) {
      bgColor = Colors.blue.shade50;
      textColor = Colors.blue.shade700;
      message = 'Viewing all shuttles';
      icon = Icons.list;
    } else {
      bgColor = Colors.green.shade50;
      textColor = Colors.green.shade700;
      message = 'Auto-refreshing every 10 seconds';
      icon = Icons.sync;
    }

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
      color: bgColor,
      child: Row(
        children: [
          Icon(icon, size: 16, color: textColor),
          const SizedBox(width: 8),
          Text(message, style: TextStyle(fontSize: 12, color: textColor)),
        ],
      ),
    );
  }

  Widget _buildMapView(AsyncValue shuttlesAsync) {
    return shuttlesAsync.when(
      data: (shuttles) {
        final locationsWithCoords = shuttles.where((shuttle) {
          if (shuttle is ShuttleLocation) {
            return true;
          } else if (shuttle is Shuttle) {
            return shuttle.currentLatitude != null &&
                shuttle.currentLongitude != null;
          }
          return false;
        }).toList();

        if (locationsWithCoords.isEmpty) {
          return _buildEmptyMapState();
        }

        // Calculate center point
        final latitudes = locationsWithCoords.map((s) {
          if (s is ShuttleLocation) return s.latitude;
          return (s as Shuttle).currentLatitude!;
        }).toList();

        final longitudes = locationsWithCoords.map((s) {
          if (s is ShuttleLocation) return s.longitude;
          return (s as Shuttle).currentLongitude!;
        }).toList();

        final centerLat = latitudes.reduce((a, b) => a + b) / latitudes.length;
        final centerLng =
            longitudes.reduce((a, b) => a + b) / longitudes.length;

        return Stack(
          children: [
            FlutterMap(
              mapController: _mapController,
              options: MapOptions(
                initialCenter: LatLng(centerLat, centerLng),
                initialZoom: 13.0,
                minZoom: 5.0,
                maxZoom: 18.0,
              ),
              children: [
                TileLayer(
                  urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                  userAgentPackageName: 'com.example.um_flutter',
                ),
                MarkerLayer(
                  markers: locationsWithCoords.map<Marker>((shuttle) {
                    final lat = shuttle is ShuttleLocation
                        ? shuttle.latitude
                        : (shuttle as Shuttle).currentLatitude!;
                    final lng = shuttle is ShuttleLocation
                        ? shuttle.longitude
                        : (shuttle as Shuttle).currentLongitude!;
                    final id = shuttle is ShuttleLocation
                        ? shuttle.shuttleId
                        : (shuttle as Shuttle).id;
                    final status = shuttle is ShuttleLocation
                        ? shuttle.status
                        : (shuttle as Shuttle).status;

                    return Marker(
                      point: LatLng(lat, lng),
                      width: 80,
                      height: 80,
                      child: GestureDetector(
                        onTap: () {
                          setState(() {
                            _selectedShuttleId = id;
                          });
                          _showShuttleDetails(context, shuttle);
                        },
                        child: Column(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(
                                color: _getStatusColor(status),
                                shape: BoxShape.circle,
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.black.withOpacity(0.3),
                                    blurRadius: 4,
                                    offset: const Offset(0, 2),
                                  ),
                                ],
                              ),
                              child: const Icon(
                                Icons.directions_bus,
                                color: Colors.white,
                                size: 24,
                              ),
                            ),
                            if (_selectedShuttleId == id)
                              Container(
                                margin: const EdgeInsets.only(top: 4),
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 6,
                                  vertical: 2,
                                ),
                                decoration: BoxDecoration(
                                  color: Colors.black87,
                                  borderRadius: BorderRadius.circular(4),
                                ),
                                child: Text(
                                  shuttle is ShuttleLocation
                                      ? shuttle.vehicleNumber
                                      : (shuttle as Shuttle).vehicleNumber,
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 10,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                          ],
                        ),
                      ),
                    );
                  }).toList(),
                ),
              ],
            ),
            // Legend
            Positioned(
              top: 16,
              right: 16,
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Status',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 12,
                        ),
                      ),
                      const SizedBox(height: 8),
                      _buildLegendItem('Active', Colors.green),
                      _buildLegendItem('Inactive', Colors.grey),
                      _buildLegendItem('Maintenance', Colors.orange),
                      _buildLegendItem('Out of Service', Colors.red),
                    ],
                  ),
                ),
              ),
            ),
          ],
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, stack) => _buildErrorState(error),
    );
  }

  Widget _buildLegendItem(String label, Color color) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 12,
            height: 12,
            decoration: BoxDecoration(color: color, shape: BoxShape.circle),
          ),
          const SizedBox(width: 6),
          Text(label, style: const TextStyle(fontSize: 11)),
        ],
      ),
    );
  }

  Widget _buildEmptyMapState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.location_off, size: 64, color: Colors.grey.shade400),
          const SizedBox(height: 16),
          Text(
            'No shuttle locations available',
            style: TextStyle(
              fontSize: 18,
              color: Colors.grey.shade600,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Shuttles will appear on the map when they report their location',
            style: TextStyle(fontSize: 14, color: Colors.grey.shade500),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildListView(AsyncValue shuttlesAsync, bool isFaculty) {
    return shuttlesAsync.when(
      data: (shuttles) {
        if (shuttles.isEmpty) {
          return _buildEmptyState();
        }

        return RefreshIndicator(
          onRefresh: () async => _refreshList(),
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: shuttles.length,
            itemBuilder: (context, index) {
              final shuttle = shuttles[index];
              if (shuttle is ShuttleLocation) {
                return _ShuttleLocationCard(
                  location: shuttle,
                  isFaculty: isFaculty,
                  onStatusChanged: _refreshList,
                  onLocationUpdate: _refreshList,
                );
              } else if (shuttle is Shuttle) {
                return _ShuttleCard(
                  shuttle: shuttle,
                  isFaculty: isFaculty,
                  onStatusChanged: _refreshList,
                  onLocationUpdate: _refreshList,
                );
              }
              return const SizedBox.shrink();
            },
          ),
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, stack) => _buildErrorState(error),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.directions_bus_outlined,
            size: 64,
            color: Colors.grey.shade400,
          ),
          const SizedBox(height: 16),
          Text(
            _showAllShuttles ? 'No shuttles registered' : 'No active shuttles',
            style: TextStyle(
              fontSize: 18,
              color: Colors.grey.shade600,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(Object error) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.red),
            const SizedBox(height: 16),
            Text(
              'Error loading shuttles',
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
            ),
            const SizedBox(height: 8),
            Text(
              error.toString(),
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: _refreshList,
              icon: const Icon(Icons.refresh),
              label: const Text('Retry'),
            ),
          ],
        ),
      ),
    );
  }

  void _showShuttleDetails(BuildContext context, dynamic shuttle) {
    final vehicleNumber = shuttle is ShuttleLocation
        ? shuttle.vehicleNumber
        : (shuttle as Shuttle).vehicleNumber;
    final routeName = shuttle is ShuttleLocation
        ? shuttle.routeName
        : (shuttle as Shuttle).routeName;
    final status = shuttle is ShuttleLocation
        ? shuttle.status
        : (shuttle as Shuttle).status;
    final lat = shuttle is ShuttleLocation
        ? shuttle.latitude
        : (shuttle as Shuttle).currentLatitude;
    final lng = shuttle is ShuttleLocation
        ? shuttle.longitude
        : (shuttle as Shuttle).currentLongitude;

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(vehicleNumber),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _DetailRow(label: 'Route', value: routeName),
            _DetailRow(label: 'Status', value: status.name),
            const Divider(),
            if (lat != null && lng != null) ...[
              _DetailRow(label: 'Latitude', value: lat.toStringAsFixed(6)),
              _DetailRow(label: 'Longitude', value: lng.toStringAsFixed(6)),
            ],
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  void _refreshList() {
    if (_showAllShuttles) {
      ref.invalidate(allShuttlesProvider);
    } else if (_autoRefresh) {
      ref.invalidate(activeShuttlesProvider);
    } else {
      ref.invalidate(activeShuttlesManualProvider);
    }
  }

  void _showAddShuttleDialog(BuildContext context) {
    final vehicleNumberController = TextEditingController();
    final routeNameController = TextEditingController();
    final capacityController = TextEditingController(text: '50');

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Add New Shuttle'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: vehicleNumberController,
              decoration: const InputDecoration(
                labelText: 'Vehicle Number',
                hintText: 'e.g., BUS-001',
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: routeNameController,
              decoration: const InputDecoration(
                labelText: 'Route Name',
                hintText: 'e.g., Main Campus Loop',
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: capacityController,
              decoration: const InputDecoration(labelText: 'Capacity'),
              keyboardType: TextInputType.number,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              if (vehicleNumberController.text.isEmpty ||
                  routeNameController.text.isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Please fill all fields')),
                );
                return;
              }

              try {
                final trackingService = ref.read(trackingServiceProvider);
                await trackingService.registerShuttle(
                  vehicleNumber: vehicleNumberController.text,
                  routeName: routeNameController.text,
                  capacity: int.parse(capacityController.text),
                );

                if (context.mounted) {
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Shuttle added successfully')),
                  );
                  _refreshList();
                }
              } catch (e) {
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Error: ${e.toString()}')),
                  );
                }
              }
            },
            child: const Text('Add'),
          ),
        ],
      ),
    );
  }

  Color _getStatusColor(ShuttleStatus status) {
    switch (status) {
      case ShuttleStatus.ACTIVE:
        return Colors.green;
      case ShuttleStatus.INACTIVE:
        return Colors.grey;
      case ShuttleStatus.MAINTENANCE:
        return Colors.orange;
      case ShuttleStatus.OUT_OF_SERVICE:
        return Colors.red;
    }
  }
}

// Existing card widgets remain the same...
class _ShuttleLocationCard extends ConsumerWidget {
  final ShuttleLocation location;
  final bool isFaculty;
  final VoidCallback onStatusChanged;
  final VoidCallback onLocationUpdate;

  const _ShuttleLocationCard({
    required this.location,
    required this.isFaculty,
    required this.onStatusChanged,
    required this.onLocationUpdate,
  });

  Color _getStatusColor() {
    switch (location.status) {
      case ShuttleStatus.ACTIVE:
        return Colors.green;
      case ShuttleStatus.INACTIVE:
        return Colors.grey;
      case ShuttleStatus.MAINTENANCE:
        return Colors.orange;
      case ShuttleStatus.OUT_OF_SERVICE:
        return Colors.red;
    }
  }

  IconData _getStatusIcon() {
    switch (location.status) {
      case ShuttleStatus.ACTIVE:
        return Icons.check_circle;
      case ShuttleStatus.INACTIVE:
        return Icons.pause_circle;
      case ShuttleStatus.MAINTENANCE:
        return Icons.build_circle;
      case ShuttleStatus.OUT_OF_SERVICE:
        return Icons.cancel;
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final statusColor = _getStatusColor();
    final statusIcon = _getStatusIcon();

    return Card(
      elevation: 2,
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () => _showDetails(context, ref),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: statusColor.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(
                      Icons.directions_bus,
                      color: statusColor,
                      size: 32,
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          location.routeName,
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 16,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          '${location.vehicleNumber} (ID: ${location.shuttleId})',
                          style: TextStyle(
                            color: Colors.grey.shade600,
                            fontSize: 14,
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (isFaculty)
                    PopupMenuButton<String>(
                      onSelected: (value) => _handleAction(context, ref, value),
                      itemBuilder: (context) => [
                        const PopupMenuItem(
                          value: 'update_location',
                          child: Row(
                            children: [
                              Icon(Icons.my_location),
                              SizedBox(width: 8),
                              Text('Update Location'),
                            ],
                          ),
                        ),
                        const PopupMenuDivider(),
                        const PopupMenuItem(
                          value: 'active',
                          child: Row(
                            children: [
                              Icon(Icons.check_circle, color: Colors.green),
                              SizedBox(width: 8),
                              Text('Set Active'),
                            ],
                          ),
                        ),
                        const PopupMenuItem(
                          value: 'inactive',
                          child: Row(
                            children: [
                              Icon(Icons.pause_circle, color: Colors.grey),
                              SizedBox(width: 8),
                              Text('Set Inactive'),
                            ],
                          ),
                        ),
                        const PopupMenuItem(
                          value: 'maintenance',
                          child: Row(
                            children: [
                              Icon(Icons.build_circle, color: Colors.orange),
                              SizedBox(width: 8),
                              Text('Set Maintenance'),
                            ],
                          ),
                        ),
                      ],
                    ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Icon(statusIcon, size: 16, color: statusColor),
                  const SizedBox(width: 4),
                  Text(
                    location.status.name,
                    style: TextStyle(
                      color: statusColor,
                      fontWeight: FontWeight.w500,
                      fontSize: 12,
                    ),
                  ),
                  const Spacer(),
                  if (location.lastUpdate != null)
                    Text(
                      _formatDateTime(location.lastUpdate!),
                      style: TextStyle(
                        fontSize: 11,
                        color: Colors.grey.shade500,
                      ),
                    ),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Icon(
                    Icons.location_on,
                    size: 14,
                    color: Colors.grey.shade500,
                  ),
                  const SizedBox(width: 4),
                  Text(
                    '${location.latitude.toStringAsFixed(4)}, ${location.longitude.toStringAsFixed(4)}',
                    style: TextStyle(fontSize: 11, color: Colors.grey.shade600),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showDetails(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(location.vehicleNumber),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _DetailRow(label: 'ID', value: location.shuttleId.toString()),
            _DetailRow(label: 'Route', value: location.routeName),
            _DetailRow(label: 'Status', value: location.status.name),
            const Divider(),
            _DetailRow(
              label: 'Latitude',
              value: location.latitude.toStringAsFixed(6),
            ),
            _DetailRow(
              label: 'Longitude',
              value: location.longitude.toStringAsFixed(6),
            ),
            if (location.lastUpdate != null)
              _DetailRow(
                label: 'Last Update',
                value: DateFormat(
                  'MMM dd, yyyy HH:mm:ss',
                ).format(location.lastUpdate!),
              ),
          ],
        ),
        actions: [
          if (isFaculty)
            TextButton.icon(
              onPressed: () {
                Navigator.pop(context);
                _showUpdateLocationDialog(context, ref);
              },
              icon: const Icon(Icons.my_location),
              label: const Text('Update Location'),
            ),
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  void _showUpdateLocationDialog(BuildContext context, WidgetRef ref) {
    final latController = TextEditingController(
      text: location.latitude.toString(),
    );
    final lngController = TextEditingController(
      text: location.longitude.toString(),
    );

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Update Location'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: latController,
              decoration: const InputDecoration(
                labelText: 'Latitude',
                hintText: 'e.g., 40.7128',
              ),
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: lngController,
              decoration: const InputDecoration(
                labelText: 'Longitude',
                hintText: 'e.g., -74.0060',
              ),
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              try {
                final lat = double.parse(latController.text);
                final lng = double.parse(lngController.text);

                final trackingService = ref.read(trackingServiceProvider);
                await trackingService.updateShuttleLocation(
                  shuttleId: location.shuttleId,
                  latitude: lat,
                  longitude: lng,
                );

                if (context.mounted) {
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('Location updated successfully'),
                    ),
                  );
                  onLocationUpdate();
                }
              } catch (e) {
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Error: ${e.toString()}')),
                  );
                }
              }
            },
            child: const Text('Update'),
          ),
        ],
      ),
    );
  }

  void _handleAction(BuildContext context, WidgetRef ref, String action) async {
    if (action == 'update_location') {
      _showUpdateLocationDialog(context, ref);
      return;
    }

    ShuttleStatus newStatus;
    switch (action) {
      case 'active':
        newStatus = ShuttleStatus.ACTIVE;
        break;
      case 'inactive':
        newStatus = ShuttleStatus.INACTIVE;
        break;
      case 'maintenance':
        newStatus = ShuttleStatus.MAINTENANCE;
        break;
      default:
        return;
    }

    try {
      final trackingService = ref.read(trackingServiceProvider);
      await trackingService.updateShuttleStatus(location.shuttleId, newStatus);

      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Shuttle status updated to ${newStatus.name}'),
          ),
        );
        onStatusChanged();
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Error: ${e.toString()}')));
      }
    }
  }

  String _formatDateTime(DateTime date) {
    final now = DateTime.now();
    final difference = now.difference(date);

    if (difference.inMinutes < 1) {
      return 'Just now';
    } else if (difference.inMinutes < 60) {
      return '${difference.inMinutes}m ago';
    } else if (difference.inHours < 24) {
      return '${difference.inHours}h ago';
    } else {
      return DateFormat('MMM dd, HH:mm').format(date);
    }
  }
}

class _ShuttleCard extends ConsumerWidget {
  final Shuttle shuttle;
  final bool isFaculty;
  final VoidCallback onStatusChanged;
  final VoidCallback onLocationUpdate;

  const _ShuttleCard({
    required this.shuttle,
    required this.isFaculty,
    required this.onStatusChanged,
    required this.onLocationUpdate,
  });

  Color _getStatusColor() {
    switch (shuttle.status) {
      case ShuttleStatus.ACTIVE:
        return Colors.green;
      case ShuttleStatus.INACTIVE:
        return Colors.grey;
      case ShuttleStatus.MAINTENANCE:
        return Colors.orange;
      case ShuttleStatus.OUT_OF_SERVICE:
        return Colors.red;
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final statusColor = _getStatusColor();
    final hasLocation =
        shuttle.currentLatitude != null && shuttle.currentLongitude != null;

    return Card(
      elevation: 2,
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: statusColor.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(
                    Icons.directions_bus,
                    color: statusColor,
                    size: 32,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        shuttle.routeName,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${shuttle.vehicleNumber} (ID: ${shuttle.id})',
                        style: TextStyle(
                          color: Colors.grey.shade600,
                          fontSize: 14,
                        ),
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
                    color: statusColor.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    shuttle.status.name,
                    style: TextStyle(
                      color: statusColor,
                      fontWeight: FontWeight.bold,
                      fontSize: 12,
                    ),
                  ),
                ),
                if (isFaculty)
                  PopupMenuButton<String>(
                    onSelected: (value) => _handleAction(context, ref, value),
                    itemBuilder: (context) => [
                      const PopupMenuItem(
                        value: 'update_location',
                        child: Row(
                          children: [
                            Icon(Icons.my_location),
                            SizedBox(width: 8),
                            Text('Update Location'),
                          ],
                        ),
                      ),
                      const PopupMenuDivider(),
                      const PopupMenuItem(
                        value: 'active',
                        child: Text('Set Active'),
                      ),
                      const PopupMenuItem(
                        value: 'inactive',
                        child: Text('Set Inactive'),
                      ),
                      const PopupMenuItem(
                        value: 'maintenance',
                        child: Text('Set Maintenance'),
                      ),
                    ],
                  ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Icon(Icons.people, size: 16, color: Colors.grey.shade500),
                const SizedBox(width: 4),
                Text(
                  'Capacity: ${shuttle.capacity}',
                  style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                ),
                if (shuttle.driver != null) ...[
                  const SizedBox(width: 16),
                  Icon(Icons.person, size: 16, color: Colors.grey.shade500),
                  const SizedBox(width: 4),
                  Text(
                    shuttle.driver!,
                    style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                  ),
                ],
              ],
            ),
            if (hasLocation) ...[
              const SizedBox(height: 8),
              Row(
                children: [
                  Icon(
                    Icons.location_on,
                    size: 14,
                    color: Colors.grey.shade500,
                  ),
                  const SizedBox(width: 4),
                  Text(
                    '${shuttle.currentLatitude!.toStringAsFixed(4)}, ${shuttle.currentLongitude!.toStringAsFixed(4)}',
                    style: TextStyle(fontSize: 11, color: Colors.grey.shade600),
                  ),
                ],
              ),
            ] else ...[
              const SizedBox(height: 8),
              Row(
                children: [
                  Icon(
                    Icons.location_off,
                    size: 14,
                    color: Colors.grey.shade400,
                  ),
                  const SizedBox(width: 4),
                  Text(
                    'No location data',
                    style: TextStyle(fontSize: 11, color: Colors.grey.shade400),
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }

  void _handleAction(BuildContext context, WidgetRef ref, String action) async {
    if (action == 'update_location') {
      _showUpdateLocationDialog(context, ref);
      return;
    }

    ShuttleStatus newStatus;
    switch (action) {
      case 'active':
        newStatus = ShuttleStatus.ACTIVE;
        break;
      case 'inactive':
        newStatus = ShuttleStatus.INACTIVE;
        break;
      case 'maintenance':
        newStatus = ShuttleStatus.MAINTENANCE;
        break;
      default:
        return;
    }

    try {
      final trackingService = ref.read(trackingServiceProvider);
      await trackingService.updateShuttleStatus(shuttle.id, newStatus);

      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Shuttle status updated to ${newStatus.name}'),
          ),
        );
        onStatusChanged();
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Error: ${e.toString()}')));
      }
    }
  }

  void _showUpdateLocationDialog(BuildContext context, WidgetRef ref) {
    final latController = TextEditingController(
      text: shuttle.currentLatitude?.toString() ?? '40.7128',
    );
    final lngController = TextEditingController(
      text: shuttle.currentLongitude?.toString() ?? '-74.0060',
    );

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Update Location'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: latController,
              decoration: const InputDecoration(labelText: 'Latitude'),
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: lngController,
              decoration: const InputDecoration(labelText: 'Longitude'),
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              try {
                final lat = double.parse(latController.text);
                final lng = double.parse(lngController.text);

                final trackingService = ref.read(trackingServiceProvider);
                await trackingService.updateShuttleLocation(
                  shuttleId: shuttle.id,
                  latitude: lat,
                  longitude: lng,
                );

                if (context.mounted) {
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('Location updated successfully'),
                    ),
                  );
                  onLocationUpdate();
                }
              } catch (e) {
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Error: ${e.toString()}')),
                  );
                }
              }
            },
            child: const Text('Update'),
          ),
        ],
      ),
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
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          SizedBox(
            width: 100,
            child: Text(
              '$label:',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}
