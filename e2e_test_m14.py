import requests
import json
import time

base_url = "http://localhost:8080/api/v1"

def make_request(url, method="GET", data=None, headers=None):
    if method == "GET":
        response = requests.get(url, headers=headers)
    elif method == "POST":
        response = requests.post(url, json=data, headers=headers)
    
    try:
        return response.status_code, response.json()
    except:
        return response.status_code, response.text

def run_tests():
    suffix = str(int(time.time()))
    email1 = f"gate1_{suffix}@test.com"
    email2 = f"gate2_{suffix}@test.com"

    # 1. Register User 1
    print("--- 1. Register User 1 ---")
    st, resp = make_request(f"{base_url}/users/register", method="POST", data={
        "name": "Gate User 1", "email": email1, "password": "password", "role": "USER"
    })
    print(st, resp)
    assert st == 200

    # 2. Login
    print("--- 2. Login User 1 ---")
    st, resp = make_request(f"{base_url}/users/login", method="POST", data={
        "email": email1, "password": "password"
    })
    print(st, resp)
    assert st == 200
    token1 = resp["token"]
    h1 = {"Authorization": f"Bearer {token1}"}

    # 3. GET Subjects
    print("--- 3. GET Subjects ---")
    st, resp = make_request(f"{base_url}/gate/subjects", headers=h1)
    print(st, resp)
    assert st == 200
    assert "DBMS" in resp

    # 4. Generate Question
    print("--- 4. Generate Question ---")
    st, resp = make_request(f"{base_url}/gate/questions/generate", method="POST", headers=h1, data={
        "subject": "DBMS", "topic": "SQL", "difficulty": "MEDIUM", "questionType": "MCQ"
    })
    print(st, resp)
    assert st == 200
    q_id = resp["id"]
    
    # 5. GET Question
    print("--- 5. GET Question ---")
    st, resp = make_request(f"{base_url}/gate/questions/{q_id}", headers=h1)
    print(st, resp)
    assert st == 200
    assert resp.get("correctOption") is None

    # 6. Attempt Question (Wrong)
    print("--- 6. Attempt Question (Wrong) ---")
    st, resp = make_request(f"{base_url}/gate/questions/{q_id}/attempt", method="POST", headers=h1, data={"selectedAnswer": "X"})
    print(st, resp)
    assert st == 200
    assert resp["isCorrect"] is False

    # 7. Attempt Question (Right)
    print("--- 7. Attempt Question (Right) ---")
    st, resp = make_request(f"{base_url}/gate/questions/{q_id}/attempt", method="POST", headers=h1, data={"selectedAnswer": "C"})
    print(st, resp)
    assert st == 200
    assert resp["isCorrect"] is True

    # 8. Hint
    print("--- 8. Hint ---")
    st, resp = make_request(f"{base_url}/gate/questions/{q_id}/hint", method="POST", headers=h1)
    print(st, resp)
    assert st == 200
    assert "hint" in resp

    # 9. Explain
    print("--- 9. Explain ---")
    st, resp = make_request(f"{base_url}/gate/questions/{q_id}/explain", method="POST", headers=h1)
    print(st, resp)
    assert st == 200
    assert "explanation" in resp

    # 10. GET Progress
    print("--- 10. GET Progress ---")
    st, resp = make_request(f"{base_url}/gate/progress", headers=h1)
    print(st, resp)
    assert st == 200
    assert resp["totalAttempts"] == 2
    assert resp["accuracy"] == 50.0

    # 11. Practice 
    print("--- 11. Practice ---")
    st, resp = make_request(f"{base_url}/gate/practice?subject=DBMS&limit=5", headers=h1)
    print(st, len(resp))
    assert st == 200
    assert len(resp) >= 1

    # 12. Register User 2
    print("--- 12. Register User 2 ---")
    st, resp = make_request(f"{base_url}/users/register", method="POST", data={
        "name": "Gate User 2", "email": email2, "password": "password", "role": "USER"
    })
    st, resp = make_request(f"{base_url}/users/login", method="POST", data={
        "email": email2, "password": "password"
    })
    token2 = resp["token"]
    h2 = {"Authorization": f"Bearer {token2}"}

    # 13. Progress Isolation
    print("--- 13. Progress Isolation User 2 ---")
    st, resp = make_request(f"{base_url}/gate/progress", headers=h2)
    print(st, resp)
    assert st == 200
    assert resp["totalAttempts"] == 0

    print("All E2E checks passed!")

if __name__ == "__main__":
    run_tests()
