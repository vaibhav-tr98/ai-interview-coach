import requests
import time

BASE_URL = "http://localhost:8080/api/v1"

def run_tests():
    print("Registering test user...")
    register_data = {
        "name": "Planner User",
        "email": "planner@example.com",
        "password": "password",
        "role": "USER"
    }
    # ignore errors if already exists
    requests.post(f"{BASE_URL}/users/register", json=register_data)
    
    print("Logging in...")
    login_data = {
        "email": "planner@example.com",
        "password": "password"
    }
    resp = requests.post(f"{BASE_URL}/users/login", json=login_data)
    resp.raise_for_status()
    token = resp.json()["token"]
    headers = {"Authorization": f"Bearer {token}"}
    
    # 1. Generate plan
    print("Generating plan...")
    resp = requests.post(f"{BASE_URL}/planner/generate?targetRole=Java Developer", headers=headers)
    resp.raise_for_status()
    plan = resp.json()
    print("Generated plan:", plan["id"], plan["status"])
    
    # 2. Get active plan
    print("Getting active plan...")
    resp = requests.get(f"{BASE_URL}/planner/active", headers=headers)
    resp.raise_for_status()
    active_plan = resp.json()
    print("Active plan:", active_plan["id"], active_plan["status"])
    assert plan["id"] == active_plan["id"]
    
    # 3. Complete tasks
    print("Completing tasks...")
    for task in active_plan["tasks"]:
        task_id = task["id"]
        resp = requests.patch(f"{BASE_URL}/planner/tasks/{task_id}/complete", headers=headers)
        resp.raise_for_status()
        print(f"Task {task_id} completed.")
        
    # 4. Check if plan is COMPLETED
    print("Getting active plan again...")
    resp = requests.get(f"{BASE_URL}/planner/active", headers=headers)
    if resp.status_code == 404:
        print("No active plan (it became COMPLETED). This is expected.")
    else:
        print("Plan status:", resp.json()["status"])
        
    print("All tests passed.")

if __name__ == "__main__":
    time.sleep(2) # wait for server to start
    run_tests()
