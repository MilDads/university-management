import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/iot_sensor.dart';
import '../models/sensor_type.dart';
import '../providers/app_providers.dart';
import '../providers/iot_providers.dart';

class AddEditSensorScreen extends ConsumerStatefulWidget {
  final IotSensor? sensor;

  const AddEditSensorScreen({super.key, this.sensor});

  @override
  ConsumerState<AddEditSensorScreen> createState() =>
      _AddEditSensorScreenState();
}

class _AddEditSensorScreenState extends ConsumerState<AddEditSensorScreen> {
  final _formKey = GlobalKey<FormState>();
  final _idController = TextEditingController();
  final _nameController = TextEditingController();
  final _locationController = TextEditingController();
  final _unitController = TextEditingController();

  SensorType _selectedType = SensorType.TEMPERATURE;
  bool _active = true;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    if (widget.sensor != null) {
      _idController.text = widget.sensor!.sensorId;
      _nameController.text = widget.sensor!.name;
      _locationController.text = widget.sensor!.location;
      _unitController.text = widget.sensor!.unit;
      _selectedType = widget.sensor!.type;
      _active = widget.sensor!.active;
    }
  }

  @override
  void dispose() {
    _idController.dispose();
    _nameController.dispose();
    _locationController.dispose();
    _unitController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      final apiService = ref.read(apiServiceProvider);

      if (widget.sensor == null) {
        // Create
        await apiService.createSensor(
          sensorId: _idController.text.trim(),
          name: _nameController.text.trim(),
          type: _selectedType.name,
          location: _locationController.text.trim(),
          unit: _unitController.text.trim(),
        );
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Sensor registered successfully')),
          );
        }
      } else {
        // Update
        await apiService.updateSensor(
          widget.sensor!.sensorId,
          name: _nameController.text.trim(),
          type: _selectedType.name,
          location: _locationController.text.trim(),
          unit: _unitController.text.trim(),
          active: _active,
        );
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Sensor updated successfully')),
          );
        }
      }

      // Refresh providers
      ref.invalidate(sensorsProvider);
      if (widget.sensor != null) {
        ref.invalidate(sensorProvider(widget.sensor!.sensorId));
      }

      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error: ${e.toString()}'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.sensor != null;

    return Scaffold(
      appBar: AppBar(
        title: Text(isEditing ? 'Edit Sensor' : 'Register Sensor'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              TextFormField(
                controller: _idController,
                decoration: const InputDecoration(
                  labelText: 'Sensor ID',
                  hintText: 'Unique Identifier (e.g., SENS-001)',
                  border: OutlineInputBorder(),
                ),
                enabled: !isEditing, // ID cannot be changed after creation
                validator: (value) {
                  if (value == null || value.isEmpty)
                    return 'Please enter sensor ID';
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(
                  labelText: 'Name',
                  hintText: 'Descriptive Name',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty)
                    return 'Please enter name';
                  return null;
                },
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<SensorType>(
                value: _selectedType,
                decoration: const InputDecoration(
                  labelText: 'Type',
                  border: OutlineInputBorder(),
                ),
                items: SensorType.values.map((type) {
                  return DropdownMenuItem(value: type, child: Text(type.label));
                }).toList(),
                onChanged: (value) {
                  if (value != null) {
                    setState(() {
                      _selectedType = value;
                      // Auto-fill unit if empty
                      if (_unitController.text.isEmpty) {
                        switch (value) {
                          case SensorType.TEMPERATURE:
                            _unitController.text = '°C';
                            break;
                          case SensorType.HUMIDITY:
                            _unitController.text = '%';
                            break;
                          case SensorType.CO2:
                            _unitController.text = 'ppm';
                            break;
                          case SensorType.LIGHT:
                            _unitController.text = 'lux';
                            break;
                          default:
                            break;
                        }
                      }
                    });
                  }
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _locationController,
                decoration: const InputDecoration(
                  labelText: 'Location',
                  hintText: 'Building, Room, etc.',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty)
                    return 'Please enter location';
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _unitController,
                decoration: const InputDecoration(
                  labelText: 'Unit',
                  hintText: 'Measurement Unit (e.g., °C, %, ppm)',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty)
                    return 'Please enter unit';
                  return null;
                },
              ),
              if (isEditing) ...[
                const SizedBox(height: 16),
                SwitchListTile(
                  title: const Text('Active'),
                  value: _active,
                  onChanged: (value) => setState(() => _active = value),
                ),
              ],
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: _isLoading ? null : _save,
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                ),
                child: _isLoading
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : Text(isEditing ? 'Update Sensor' : 'Register Sensor'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
