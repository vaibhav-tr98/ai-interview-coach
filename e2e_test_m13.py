import urllib.request
import urllib.parse
import json
import time
import sys

base_url = "http://localhost:8080/api/v1"

def make_request(url, payload=None, headers=None, method=None):
    if headers is None:
        headers = {}
    headers["Content-Type"] = "application/json"
    
    data = json.dumps(payload).encode('utf-8') if payload else None
    if not method:
        method = "POST" if data else "GET"
        
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as response:
            return response.status, json.loads(response.read().decode()) if response.length or True else {}
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        try:
            return e.code, json.loads(body) if body else {}
        except json.JSONDecodeError:
            return e.code, {"error_body": body}

def run_tests():
    email1 = f"user1_{int(time.time())}@example.com"
    email2 = f"user2_{int(time.time())}@example.com"
    password = "password123"

    print("--- 1. Register User 1 ---")
    st, resp = make_request(f"{base_url}/users/register", {"email": email1, "password": password, "name": "User 1", "role": "USER"})
    print(st, resp)

    print("--- 2. Login User 1 ---")
    st, resp = make_request(f"{base_url}/users/login", {"email": email1, "password": password})
    print(st, resp)
    token1 = resp["token"]
    h1 = {"Authorization": f"Bearer {token1}"}

    print("--- 3. Empty Project List ---")
    st, resp = make_request(f"{base_url}/projects", headers=h1)
    print(st, resp)
    assert st == 200
    assert len(resp) == 0

    print("--- 4. Create Project 1 ---")
    st, resp = make_request(f"{base_url}/projects", {"name": "Project A", "description": "Desc", "techStack": "Java"}, headers=h1)
    print(st, resp)
    assert st == 200
    project_id = resp["id"]

    print("--- 5. Start Deep Project Interview ---")
    st, start_resp = make_request(f"{base_url}/deep-interview/project/{project_id}/start", headers=h1, method="POST")
    print(st, start_resp)
    assert st == 200
    session_id = start_resp["sessionId"]
    
    print("--- 6. Answer Question 1 ---")
    st, ans_resp = make_request(f"{base_url}/deep-interview/{session_id}/answer", {"answer": "I used Java."}, headers=h1)
    print(st, ans_resp)
    assert st == 200

    print("--- 7. Fast forward complete interview (Answer Q2-Q5) ---")
    for i in range(2, 6):
        st, ans_resp = make_request(f"{base_url}/deep-interview/{session_id}/answer", {"answer": "Answer " + str(i)}, headers=h1)
        if i == 5:
            assert ans_resp["isComplete"] == True
            assert ans_resp["nextQuestion"] is None

    print("--- 8. Get Final Result ---")
    st, res_resp = make_request(f"{base_url}/deep-interview/{session_id}/result", headers=h1)
    print(st, res_resp)
    assert st == 200
    assert "projectOwnershipScore" in res_resp

    print("--- 9. Register & Login User 2 ---")
    make_request(f"{base_url}/users/register", {"email": email2, "password": password, "name": "User 2", "role": "USER"})
    _, resp = make_request(f"{base_url}/users/login", {"email": email2, "password": password})
    token2 = resp["token"]
    h2 = {"Authorization": f"Bearer {token2}"}

    print("--- 10. User 2 accessing User 1's project ---")
    st, resp = make_request(f"{base_url}/projects/{project_id}", headers=h2)
    print(st, resp)
    assert st in (403, 404)

    print("--- 11. User 2 accessing User 1's interview session ---")
    st, resp = make_request(f"{base_url}/deep-interview/{session_id}/result", headers=h2)
    print(st, resp)
    assert st in (403, 404)
    
    print("All E2E checks passed!")

if __name__ == "__main__":
    run_tests()
