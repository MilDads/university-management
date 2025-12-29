import requests
import json
import time

BASE_URL = "http://127.0.0.1:8080/api"

def register_admin():
    print("Registering faculty user (as admin substitute)...")
    payload = {
        "username": "faculty_tracker",
        "password": "password123",
        "role": "FACULTY"
    }
    try:
        response = requests.post(f"{BASE_URL}/auth/register", json=payload)
        if response.status_code in [200, 201]:
            print("Faculty registered successfully.")
        else:
            print(f"Registration status: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"Registration failed: {e}")

def login_admin():
    print("Logging in faculty user...")
    payload = {
        "username": "faculty_tracker",
        "password": "password123"
    }
    response = requests.post(f"{BASE_URL}/auth/login", json=payload)
    if response.status_code == 200:
        token = response.json()["token"]
        print("Login successful.")
        return token
    else:
        print(f"Login failed: {response.status_code} - {response.text}")
        return None

def register_shuttle(token):
    print("Registering shuttle...")
    headers = {"Authorization": f"Bearer {token}"}
    params = {
        "vehicleNumber": "BUS-001",
        "routeName": "Campus Loop A",
        "capacity": 30
    }
    # Note: Using params as the controller expects @RequestParam
    response = requests.post(f"{BASE_URL}/tracking/shuttles", headers=headers, params=params)
    if response.status_code in [200, 201]:
        print("Shuttle registered successfully.")
        return response.json()
    else:
        print(f"Shuttle registration failed: {response.status_code} - {response.text}")
        return None

def update_shuttle_status(token, shuttle_id):
    print(f"Updating shuttle {shuttle_id} status to ACTIVE...")
    headers = {"Authorization": f"Bearer {token}"}
    params = {"status": "ACTIVE"}
    response = requests.patch(f"{BASE_URL}/tracking/shuttles/{shuttle_id}/status", headers=headers, params=params)
    if response.status_code == 200:
        print("Shuttle status updated to ACTIVE.")
    else:
        print(f"Status update failed: {response.status_code} - {response.text}")

def update_shuttle_location(token, shuttle_id):
    print(f"Updating shuttle {shuttle_id} location...")
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "shuttleId": shuttle_id,
        "latitude": 34.052235,
        "longitude": -118.243683,
        "speed": 25.5,
        "heading": 90.0,
        "accuracy": 5.0
    }
    response = requests.post(f"{BASE_URL}/tracking/location", headers=headers, json=payload)
    if response.status_code == 200:
        print("Location updated successfully.")
        print(json.dumps(response.json(), indent=2))
    else:
        print(f"Location update failed: {response.status_code} - {response.text}")

def main():
    try:
        register_admin()
        token = login_admin()
        if not token:
            return

        shuttle = register_shuttle(token)
        if not shuttle:
            # Try to fetch existing shuttles if registration failed (maybe already exists)
            print("Fetching existing shuttles...")
            headers = {"Authorization": f"Bearer {token}"}
            response = requests.get(f"{BASE_URL}/tracking/shuttles", headers=headers)
            if response.status_code == 200:
                shuttles = response.json()
                if shuttles:
                    shuttle = shuttles[0]
                    print(f"Using existing shuttle ID: {shuttle['id']}")
            
        if shuttle:
            shuttle_id = shuttle['id']
            update_shuttle_status(token, shuttle_id)
            update_shuttle_location(token, shuttle_id)
            print("\nSetup complete. Check the Flutter app now.")
        else:
            print("Could not get a shuttle to update.")

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    main()
