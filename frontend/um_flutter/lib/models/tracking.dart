enum ShuttleStatus { ACTIVE, INACTIVE, MAINTENANCE, OUT_OF_SERVICE }

class ShuttleLocation {
  final int shuttleId;
  final String vehicleNumber;
  final String routeName;
  final ShuttleStatus status;
  final double latitude;
  final double longitude;
  final DateTime? lastUpdate;

  ShuttleLocation({
    required this.shuttleId,
    required this.vehicleNumber,
    required this.routeName,
    required this.status,
    required this.latitude,
    required this.longitude,
    this.lastUpdate,
  });

  factory ShuttleLocation.fromJson(Map<String, dynamic> json) {
    return ShuttleLocation(
      shuttleId: json['shuttleId'] as int,
      vehicleNumber: json['vehicleNumber'] as String,
      routeName: json['routeName'] as String,
      status: ShuttleStatus.values.firstWhere(
        (e) => e.name == json['status'],
        orElse: () => ShuttleStatus.INACTIVE,
      ),
      latitude: (json['latitude'] as num).toDouble(),
      longitude: (json['longitude'] as num).toDouble(),
      lastUpdate: json['lastUpdate'] != null
          ? DateTime.parse(json['lastUpdate'] as String)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'shuttleId': shuttleId,
      'vehicleNumber': vehicleNumber,
      'routeName': routeName,
      'status': status.name,
      'latitude': latitude,
      'longitude': longitude,
      'lastUpdate': lastUpdate?.toIso8601String(),
    };
  }
}

class Shuttle {
  final int id;
  final String vehicleNumber;
  final String routeName;
  final String? driver;
  final int capacity;
  final ShuttleStatus status;
  final double? currentLatitude;
  final double? currentLongitude;
  final DateTime? lastLocationUpdate;

  Shuttle({
    required this.id,
    required this.vehicleNumber,
    required this.routeName,
    this.driver,
    required this.capacity,
    required this.status,
    this.currentLatitude,
    this.currentLongitude,
    this.lastLocationUpdate,
  });

  factory Shuttle.fromJson(Map<String, dynamic> json) {
    return Shuttle(
      id: json['id'] as int,
      vehicleNumber: json['vehicleNumber'] as String,
      routeName: json['routeName'] as String,
      driver: json['driver'] as String?,
      capacity: json['capacity'] as int,
      status: ShuttleStatus.values.firstWhere(
        (e) => e.name == json['status'],
        orElse: () => ShuttleStatus.INACTIVE,
      ),
      currentLatitude: json['currentLatitude'] != null
          ? (json['currentLatitude'] as num).toDouble()
          : null,
      currentLongitude: json['currentLongitude'] != null
          ? (json['currentLongitude'] as num).toDouble()
          : null,
      lastLocationUpdate: json['lastLocationUpdate'] != null
          ? DateTime.parse(json['lastLocationUpdate'] as String)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'vehicleNumber': vehicleNumber,
      'routeName': routeName,
      'driver': driver,
      'capacity': capacity,
      'status': status.name,
      'currentLatitude': currentLatitude,
      'currentLongitude': currentLongitude,
      'lastLocationUpdate': lastLocationUpdate?.toIso8601String(),
    };
  }
}
