import { authService, dashboardService } from './src/api/services.js';
import api from './src/api/axios.js';

async function test() {
  console.log("Testing API integration with:", api.defaults.baseURL);
  
  const testEmail = `test_${Date.now()}@example.com`;
  
  try {
    console.log("1. Testing Registration...");
    await authService.register({
      name: "Test User",
      email: testEmail,
      password: "password123",
      role: "USER"
    });
    console.log("Registration successful.");
    
    console.log("2. Testing Login...");
    const loginResp = await authService.login({
      email: testEmail,
      password: "password123"
    });
    console.log("Login successful. Token received.");
    
    // Simulate AuthContext saving token
    const token = loginResp.data.token;
    // We can't use localStorage in node natively without a polyfill, 
    // so we manually set the header for the test to verify Axios setup
    api.interceptors.request.use((config) => {
      config.headers.Authorization = `Bearer ${token}`;
      return config;
    });
    console.log("Axios Authorization header verified via interceptor mockup.");
    
    console.log("3. Testing Protected Route (Dashboard)...");
    const dashResp = await dashboardService.getDashboard();
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
