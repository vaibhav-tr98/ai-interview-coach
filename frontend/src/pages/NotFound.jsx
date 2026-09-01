import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/Button';
import { FileQuestion } from 'lucide-react';

export const NotFound = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-[80vh] flex flex-col items-center justify-center text-center px-4">
      <div className="bg-gray-50 p-6 rounded-full mb-6">
        <FileQuestion className="h-16 w-16 text-blue-600" />
      </div>
      <h1 className="text-4xl font-bold text-gray-900 mb-2">404</h1>
      <h2 className="text-xl font-semibold text-gray-700 mb-4">Page Not Found</h2>
      <p className="text-gray-500 max-w-md mb-8">
        The page you are looking for doesn't exist or has been moved. Let's get you back on track.
      </p>
      <Button onClick={() => navigate('/')}>
        Return to Dashboard
      </Button>
    </div>
  );
};
