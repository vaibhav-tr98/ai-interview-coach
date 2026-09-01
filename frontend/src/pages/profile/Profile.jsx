import React, { useState, useEffect } from 'react';
import { profileService } from '../../api/services';
import { useAuth } from '../../contexts/AuthContext';
import { PageHeader } from '../../components/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/Card';
import { Button } from '../../components/Button';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { User } from 'lucide-react';

export const Profile = () => {
  const { user, logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await profileService.getProfile();
        setProfile(response.data);
      } catch (err) {
        if (err.response?.status === 404) {
          // Fallback to auth context user
          setProfile(user);
        } else {
          setError('Unable to load profile data.');
          setProfile(user); // Still fallback
        }
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [user]);

  if (loading) return <LoadingScreen text="Loading profile..." />;

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <PageHeader 
        title="My Profile" 
        description="Manage your account settings and preferences."
      />

      {error && <Alert variant="error">{error}</Alert>}

      <Card>
        <CardHeader className="flex flex-row items-center gap-4 border-b border-gray-100 pb-4">
          <div className="h-16 w-16 bg-blue-100 rounded-full flex items-center justify-center text-blue-700">
            <User className="h-8 w-8" />
          </div>
          <div>
            <CardTitle className="text-xl">{profile?.name || 'User'}</CardTitle>
            <p className="text-sm text-gray-500">{profile?.email}</p>
          </div>
        </CardHeader>
        <CardContent className="pt-6 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h4 className="text-sm font-medium text-gray-500 mb-1">Full Name</h4>
              <p className="text-gray-900">{profile?.name || 'Not provided'}</p>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-500 mb-1">Email Address</h4>
              <p className="text-gray-900">{profile?.email || 'Not provided'}</p>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-500 mb-1">Role</h4>
              <p className="text-gray-900">{profile?.role || 'User'}</p>
            </div>
          </div>
          
          <div className="pt-6 border-t border-gray-100">
            <Button variant="outline" className="text-red-600 border-red-200 hover:bg-red-50" onClick={logout}>
              Sign Out
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
