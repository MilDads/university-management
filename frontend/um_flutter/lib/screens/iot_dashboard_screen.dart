import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/iot_sensor.dart';
import '../models/sensor_type.dart';
import '../providers/iot_providers.dart';
import '../providers/app_providers.dart';
import 'sensor_details_screen.dart';
import 'add_edit_sensor_screen.dart';

class IotDashboardScreen extends ConsumerWidget {
  const IotDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sensorsAsync = ref.watch(sensorsProvider);
    final userInfo = ref.watch(currentUserInfoProvider);
    final canManage = userInfo.isAdmin || userInfo.isFaculty;

    return Scaffold(
      appBar: AppBar(
        title: const Text('IoT Dashboard'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.refresh(sensorsProvider),
          ),
        ],
      ),
      body: sensorsAsync.when(
        data: (sensors) {
          if (sensors.isEmpty) {
            return const Center(
              child: Text(
                'No sensors found.',
                style: TextStyle(fontSize: 16, color: Colors.grey),
              ),
            );
          }
          return RefreshIndicator(
            onRefresh: () async => ref.refresh(sensorsProvider),
            child: GridView.builder(
              padding: const EdgeInsets.all(16),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                childAspectRatio: 0.85, // Taller cards
                crossAxisSpacing: 16,
                mainAxisSpacing: 16,
              ),
              itemCount: sensors.length,
              itemBuilder: (context, index) {
                return SensorCard(sensor: sensors[index]);
              },
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 48, color: Colors.red),
              const SizedBox(height: 16),
              Text(
                'Error: ${error.toString()}',
                textAlign: TextAlign.center,
                style: const TextStyle(color: Colors.red),
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => ref.refresh(sensorsProvider),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      ),
      floatingActionButton: canManage
          ? FloatingActionButton.extended(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const AddEditSensorScreen(),
                  ),
                );
              },
              icon: const Icon(Icons.add),
              label: const Text('Add Sensor'),
            )
          : null,
    );
  }
}

class SensorCard extends StatelessWidget {
  final IotSensor sensor;

  const SensorCard({super.key, required this.sensor});

  Color _getColorForType(SensorType type) {
    switch (type) {
      case SensorType.TEMPERATURE:
        return Colors.orange;
      case SensorType.HUMIDITY:
        return Colors.blue;
      case SensorType.CO2:
        return Colors.grey; // Changed from green to grey for CO2
      case SensorType.LIGHT:
        return Colors.amber;
      case SensorType.MOTION:
        return Colors.purple;
      case SensorType.OCCUPANCY:
        return Colors.teal;
    }
  }

  IconData _getIconForType(SensorType type) {
    switch (type) {
      case SensorType.TEMPERATURE:
        return Icons.thermostat;
      case SensorType.HUMIDITY:
        return Icons.water_drop;
      case SensorType.CO2:
        return Icons.cloud;
      case SensorType.LIGHT:
        return Icons.wb_sunny;
      case SensorType.MOTION:
        return Icons.directions_run;
      case SensorType.OCCUPANCY:
        return Icons.people;
    }
  }

  @override
  Widget build(BuildContext context) {
    final color = _getColorForType(sensor.type);
    final icon = _getIconForType(sensor.type);

    return Card(
      elevation: 4,
      shadowColor: color.withValues(alpha: 0.3),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) =>
                  SensorDetailsScreen(sensorId: sensor.sensorId),
            ),
          );
        },
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: color.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(icon, color: color, size: 24),
                  ),
                  if (sensor.active)
                    const Icon(Icons.circle, color: Colors.green, size: 12)
                  else
                    const Icon(Icons.circle, color: Colors.grey, size: 12),
                ],
              ),
              const Spacer(),
              Text(
                sensor.name,
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 4),
              Text(
                sensor.location,
                style: Theme.of(
                  context,
                ).textTheme.bodySmall?.copyWith(color: Colors.grey[600]),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 8),
              if (sensor.lastValue != null)
                Row(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      sensor.lastValue!.toStringAsFixed(1),
                      style: TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                        color: color,
                      ),
                    ),
                    const SizedBox(width: 4),
                    Padding(
                      padding: const EdgeInsets.only(bottom: 4),
                      child: Text(
                        sensor.unit,
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey[600],
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                  ],
                )
              else
                const Text(
                  'No Data',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: Colors.grey,
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
