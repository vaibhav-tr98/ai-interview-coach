import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../../api/services';
import { Button } from '../../components/Button';
import { Input } from '../../components/Input';
import { Eye, EyeOff } from 'lucide-react';
import { Alert } from '../../components/Alert';

export const Register = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'USER'
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const navigate = useNavigate();

  const validate = () => {
    if (!formData.name || !formData.email || !formData.password || !formData.confirmPassword) {
      setError('All fields are required.');
      return false;
    }
    if (!/^\S+@\S+\.\S+$/.test(formData.email)) {
      setError('Please enter a valid email address.');
      return false;
    }
    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return false;
    }
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
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
      // Backend probably doesn't expect confirmPassword, so omit it
      const { confirmPassword, ...submitData } = formData;
      await authService.register(submitData);
      // Automatically redirect to login on success
      navigate('/login', { state: { message: 'Registration successful! Please sign in.' } });
    } catch (err) {
      if (!err.response) {
        setError('Unable to connect to the server. Please try again later.');
      } else if (err.response.status === 404) {
        setError('The requested service could not be found.');
      } else if (err.response.status === 400 || err.response.status === 409) {
        setError(err.response?.data?.message || 'Email already exists or invalid data provided.');
      } else if (err.response.status === 500) {
        setError('Something went wrong on the server.');
      } else {
        setError('Failed to register. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (error) setError('');
  };

  return (
    <form className="space-y-6" onSubmit={handleSubmit}>
      {error && (
        <Alert variant="error">{error}</Alert>
      )}
      <Input
        label="Full Name"
        name="name"
        type="text"
        required
        value={formData.name}
        onChange={handleChange}
        placeholder="John Doe"
      />
      <Input
        label="Email address"
        name="email"
        type="email"
        required
        value={formData.email}
        onChange={handleChange}
        placeholder="you@example.com"
      />
      <Input
        label="Password"
        name="password"
        type={showPassword ? "text" : "password"}
        required
        value={formData.password}
        onChange={handleChange}
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
      <Input
        label="Confirm Password"
        name="confirmPassword"
        type={showPassword ? "text" : "password"}
        required
        value={formData.confirmPassword}
        onChange={handleChange}
        placeholder="••••••••"
      />
      <Button type="submit" className="w-full" disabled={loading}>
        {loading ? (
          <div className="flex items-center justify-center">
            <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Registering...
          </div>
        ) : (
          'Register'
        )}
      </Button>
      <div className="text-sm text-center mt-4">
        <Link to="/login" className="font-medium text-blue-600 hover:text-blue-500">
          Already have an account? Sign in
        </Link>
      </div>
    </form>
  );
};
