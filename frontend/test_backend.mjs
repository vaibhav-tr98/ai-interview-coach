import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

async function test() {
  console.log("Testing API integration with deployed backend:", api.defaults.baseURL);
  
  const testEmail = `test_${Date.now()}@example.com`;
  
  try {
    console.log("1. Testing Registration...");
    await api.post('/api/v1/users/register', {
      name: "Test User",
      email: testEmail,
      password: "password123",
      role: "USER"
    });
    console.log("Registration successful.");
    
    console.log("2. Testing Login...");
    const loginResp = await api.post('/api/v1/users/login', {
      email: testEmail,
      password: "password123"
    });
    console.log("Login successful. Token received.");
    
    const token = loginResp.data.token;
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    
    console.log("3. Testing Protected Route (Dashboard)...");
    const dashResp = await api.get('/api/v1/dashboard');
    console.log("Dashboard fetch successful:", dashResp.data);
    
    console.log("\nAll API integration tests passed!");
  } catch (error) {
    console.error("API Test Failed:");
    if (error.response) {
      console.error(error.response.status, error.response.data);
    } else {
      console.error(error.message);
    }
  }
}

test();
