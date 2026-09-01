import React, { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { authService } from '../../api/services';
import { Button } from '../../components/Button';
import { Input } from '../../components/Input';
import { Eye, EyeOff } from 'lucide-react';
import { Alert } from '../../components/Alert';

export const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const validate = () => {
    if (!email || !password) {
      setError('Email and password are required.');
      return false;
    }
    if (!/^\S+@\S+\.\S+$/.test(email)) {
      setError('Please enter a valid email address.');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!validate()) return;
    
    setLoading(true);

    try {
      const response = await authService.login({ email, password });
      login(
        { name: response.data.name, role: response.data.role, email },
        response.data.token
      );
      
      const from = location.state?.from?.pathname || '/';
      navigate(from, { replace: true });
    } catch (err) {
      if (!err.response) {
        setError('Unable to connect to the server. Please try again later.');
      } else if (err.response.status === 404) {
        setError('The requested service could not be found.');
      } else if (err.response.status === 401) {
        setError('Invalid email or password.');
      } else if (err.response.status === 500) {
        setError('Something went wrong on the server.');
      } else {
        setError(err.response?.data?.message || 'Failed to login. Please check your credentials.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="space-y-6" onSubmit={handleSubmit}>
      {error && (
        <Alert variant="error">{error}</Alert>
      )}
      <Input
        label="Email address"
        type="email"
        required
        value={email}
        onChange={(e) => {
          setEmail(e.target.value);
          if (error) setError('');
        }}
        placeholder="you@example.com"
      />
      <Input
        label="Password"
        type={showPassword ? "text" : "password"}
        required
        value={password}
        onChange={(e) => {
          setPassword(e.target.value);
          if (error) setError('');
        }}
        placeholder="••••••••"
        rightElement={
          <button
            type="button"
            className="text-gray-400 hover:text-gray-600 focus:outline-none"
            onClick={() => setShowPassword(!showPassword)}
          >
            {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
          </button>
        }
      />
      <Button type="submit" className="w-full" disabled={loading}>
        {loading ? (
          <div className="flex items-center justify-center">
            <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Signing in...
          </div>
        ) : (
          'Sign in'
        )}
      </Button>
      <div className="text-sm text-center mt-4">
        <Link to="/register" className="font-medium text-blue-600 hover:text-blue-500">
          Don't have an account? Register
        </Link>
      </div>
    </form>
  );
};
