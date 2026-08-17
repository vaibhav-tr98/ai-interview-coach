import urllib.request
import urllib.parse
import json
import time

base_url = "http://localhost:8080/api"

def make_request(url, payload=None, headers=None):
    if headers is None:
        headers = {}
    headers["Content-Type"] = "application/json"
    
    data = json.dumps(payload).encode('utf-8') if payload else None
    req = urllib.request.Request(url, data=data, headers=headers, method="POST" if data else "GET")
    
    try:
        with urllib.request.urlopen(req) as response:
            return response.status, json.loads(response.read().decode())
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        try:
            return e.code, json.loads(body) if body else {}
        except json.JSONDecodeError:
            return e.code, {"error_body": body}

email = f"test_{int(time.time())}@example.com"

# 1. Register a test user
register_payload = {
    "email": email,
    "password": "password123",
    "name": "Test User",
    "role": "USER"
}
r_status, r_resp = make_request(f"{base_url}/v1/users/register", payload=register_payload)
print(f"Register status: {r_status}, resp: {r_resp}")

# 2. Login to get token
login_payload = {
    "email": email,
    "password": "password123"
}
status, login_resp = make_request(f"{base_url}/v1/users/login", payload=login_payload)
print(f"Login status: {status}, Login response: {login_resp}")
token = login_resp.get("token")
headers = {
    "Authorization": f"Bearer {token}" if token else ""
}

# 3. Test Simple Java Interview
print("--- A. Simple Java interview ---")
payload_a = {"interviewType": "JAVA", "experienceLevel": "MID"}
status_a, resp_a = make_request(f"{base_url}/interview/start", payload=payload_a, headers=headers)
print("Status:", status_a)
print(json.dumps(resp_a, indent=2))

# 4. Test Resume + Project + Role Interview
print("\n--- B. Resume + project + role interview ---")
payload_b = {
    "interviewType": "SPRING_BOOT",
    "role": "Senior Spring Developer",
    "experienceLevel": "SENIOR",
    "resume": "5 years of experience building microservices with Spring Boot.",
    "projectDescription": "Built a scalable e-commerce backend handling 1000 TPS."
}
status_b, resp_b = make_request(f"{base_url}/interview/start", payload=payload_b, headers=headers)
print("Status:", status_b)
print(json.dumps(resp_b, indent=2))

# 5. Test Technical Persona
print("\n--- C. Technical persona ---")
payload_c = {
    "interviewType": "REACT",
    "experienceLevel": "JUNIOR",
    "interviewerPersona": "TECHNICAL"
}
status_c, resp_c = make_request(f"{base_url}/interview/start", payload=payload_c, headers=headers)
print("Status:", status_c)
print(json.dumps(resp_c, indent=2))

# 6. Test Friendly Persona
print("\n--- D. Friendly persona ---")
payload_d = {
    "interviewType": "PYTHON",
    "experienceLevel": "MID",
    "interviewerPersona": "FRIENDLY"
}
status_d, resp_d = make_request(f"{base_url}/interview/start", payload=payload_d, headers=headers)
print("Status:", status_d)
print(json.dumps(resp_d, indent=2))

