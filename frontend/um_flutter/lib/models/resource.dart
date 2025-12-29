class Resource {
  final int id;
  final String name;
  final String? description;
  final String type;
  final String location;
  final int capacity;
  final String status;
  final DateTime createdAt;
  final DateTime? updatedAt;
  final String? createdBy;

  Resource({
    required this.id,
    required this.name,
    this.description,
    required this.type,
    required this.location,
    required this.capacity,
    required this.status,
    required this.createdAt,
    this.updatedAt,
    this.createdBy,
  });

  factory Resource.fromJson(Map<String, dynamic> json) {
    return Resource(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      type: json['type'],
      location: json['location'],
      capacity: json['capacity'],
      status: json['status'],
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: json['updatedAt'] != null
          ? DateTime.parse(json['updatedAt'])
          : null,
      createdBy: json['createdBy'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'type': type,
      'location': location,
      'capacity': capacity,
      'status': status,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
      'createdBy': createdBy,
    };
  }
}

enum ResourceType {
  ROOM,
  LAB,
  EQUIPMENT,
  SHUTTLE,
  STUDY_SPACE,
  SPORTS_FACILITY,
}

extension ResourceTypeExtension on ResourceType {
  String get value {
    return name;
  }

  String get displayName {
    switch (this) {
      case ResourceType.ROOM:
        return 'Room';
      case ResourceType.LAB:
        return 'Laboratory';
      case ResourceType.EQUIPMENT:
        return 'Equipment';
      case ResourceType.SHUTTLE:
        return 'Shuttle';
      case ResourceType.STUDY_SPACE:
        return 'Study Space';
      case ResourceType.SPORTS_FACILITY:
        return 'Sports Facility';
    }
  }
}

enum ResourceStatus { AVAILABLE, BOOKED, MAINTENANCE, UNAVAILABLE }
