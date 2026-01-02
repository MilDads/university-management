import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../models/iot_sensor.dart';
import '../models/sensor_reading.dart';
import '../models/sensor_type.dart';
import '../providers/iot_providers.dart';
import '../providers/app_providers.dart';
import 'add_edit_sensor_screen.dart';

class SensorDetailsScreen extends ConsumerWidget {
  final String sensorId;

  const SensorDetailsScreen({super.key, required this.sensorId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sensorAsync = ref.watch(sensorProvider(sensorId));
    final readingsAsync = ref.watch(sensorReadingsProvider(sensorId));
    final userInfo = ref.watch(currentUserInfoProvider);
    final canManage = userInfo.isAdmin || userInfo.isFaculty;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Sensor Details'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              ref.invalidate(sensorProvider(sensorId));
              ref.invalidate(sensorReadingsProvider(sensorId));
            },
          ),
          if (canManage)
            IconButton(
              icon: const Icon(Icons.edit),
              onPressed: () {
                sensorAsync.whenData((sensor) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => AddEditSensorScreen(sensor: sensor),
                    ),
                  );
                });
              },
            ),
        ],
      ),
      body: sensorAsync.when(
        data: (sensor) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildHeader(context, sensor),
                const SizedBox(height: 24),
                const Text(
                  'Live Data',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 16),
                SizedBox(
                  height: 300,
                  child: readingsAsync.when(
                    data: (readings) {
                      if (readings.isEmpty) {
                        return const Center(child: Text('No data recorded'));
                      }
                      return _SensorChart(
                        readings: readings,
                        unit: sensor.unit,
                        color: _getColorForType(sensor.type),
                      );
                    },
                    loading: () =>
                        const Center(child: CircularProgressIndicator()),
                    error: (e, s) => Center(child: Text('Error: $e')),
                  ),
                ),
                const SizedBox(height: 24),
                const Text(
                  'Recent Readings',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                readingsAsync.when(
                  data: (readings) {
                    if (readings.isEmpty) return const SizedBox();
                    // Sort by timestamp descending
                    final sorted = List<SensorReading>.from(readings)
                      ..sort((a, b) => b.timestamp.compareTo(a.timestamp));

                    return ListView.separated(
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      itemCount: sorted.length,
                      separatorBuilder: (c, i) => const Divider(),
                      itemBuilder: (context, index) {
                        final reading = sorted[index];
                        return ListTile(
                          title: Text(
                            '${reading.value.toStringAsFixed(2)} ${reading.unit}',
                            style: const TextStyle(fontWeight: FontWeight.bold),
                          ),
                          subtitle: Text(
                            DateFormat(
                              'MMM d, y HH:mm:ss',
                            ).format(reading.timestamp),
                          ),
                          leading: const Icon(
                            Icons.history,
                            color: Colors.grey,
                          ),
                        );
                      },
                    );
                  },
                  loading: () => const Center(child: LinearProgressIndicator()),
                  error: (e, s) => const SizedBox(),
                ),
              ],
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(child: Text('Error: $error')),
      ),
    );
  }

  Widget _buildHeader(BuildContext context, IotSensor sensor) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Row(
              children: [
                Icon(
                  _getIconForType(sensor.type),
                  size: 40,
                  color: _getColorForType(sensor.type),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        sensor.name,
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      Text(
                        sensor.location,
                        style: Theme.of(context).textTheme.bodyMedium,
                      ),
                    ],
                  ),
                ),
                Chip(
                  label: Text(sensor.active ? 'Active' : 'Inactive'),
                  backgroundColor: sensor.active
                      ? Colors.green.withValues(alpha: 0.1)
                      : Colors.red.withValues(alpha: 0.1),
                  labelStyle: TextStyle(
                    color: sensor.active ? Colors.green : Colors.red,
                  ),
                ),
              ],
            ),
            const Divider(height: 32),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildStat(
                  'Current',
                  '${sensor.lastValue?.toStringAsFixed(1) ?? "--"} ${sensor.unit}',
                ),
                _buildStat('Type', sensor.type.label),
                _buildStat(
                  'Last Update',
                  sensor.lastUpdate != null
                      ? DateFormat('HH:mm').format(sensor.lastUpdate!)
                      : '--',
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStat(String label, String value) {
    return Column(
      children: [
        Text(label, style: const TextStyle(color: Colors.grey, fontSize: 12)),
        const SizedBox(height: 4),
        Text(
          value,
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
      ],
    );
  }

  // Duplicated from dashboard - ideally share this
  Color _getColorForType(SensorType type) {
    switch (type) {
      case SensorType.TEMPERATURE:
        return Colors.orange;
      case SensorType.HUMIDITY:
        return Colors.blue;
      case SensorType.CO2:
        return Colors.grey;
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
}

class _SensorChart extends StatelessWidget {
  final List<SensorReading> readings;
  final String unit;
  final Color color;

  const _SensorChart({
    required this.readings,
    required this.unit,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    // Sort chronologically
    final sorted = List<SensorReading>.from(readings)
      ..sort((a, b) => a.timestamp.compareTo(b.timestamp));

    final spots = sorted.asMap().entries.map((e) {
      return FlSpot(e.key.toDouble(), e.value.value);
    }).toList();

    return LineChart(
      LineChartData(
        gridData: const FlGridData(show: false),
        titlesData: FlTitlesData(
          bottomTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              getTitlesWidget: (value, meta) {
                if (value.toInt() >= 0 && value.toInt() < sorted.length) {
                  // Show title for first, last, and middle
                  if (value.toInt() == 0 ||
                      value.toInt() == sorted.length - 1 ||
                      value.toInt() == sorted.length ~/ 2) {
                    return Padding(
                      padding: const EdgeInsets.only(top: 8.0),
                      child: Text(
                        DateFormat(
                          'HH:mm',
                        ).format(sorted[value.toInt()].timestamp),
                        style: const TextStyle(fontSize: 10),
                      ),
                    );
                  }
                }
                return const SizedBox();
              },
            ),
          ),
          leftTitles: const AxisTitles(
            sideTitles: SideTitles(showTitles: true, reservedSize: 40),
          ),
          topTitles: const AxisTitles(
            sideTitles: SideTitles(showTitles: false),
          ),
          rightTitles: const AxisTitles(
            sideTitles: SideTitles(showTitles: false),
          ),
        ),
        borderData: FlBorderData(
          show: true,
          border: Border.all(color: Colors.grey.withValues(alpha: 0.2)),
        ),
        lineBarsData: [
          LineChartBarData(
            spots: spots,
            isCurved: true,
            color: color,
            barWidth: 3,
            isStrokeCapRound: true,
            dotData: const FlDotData(show: false),
            belowBarData: BarAreaData(
              show: true,
              color: color.withValues(alpha: 0.1),
            ),
          ),
        ],
      ),
    );
  }
}
