import React from 'react';

export const LoadingScreen = ({ text = "Loading..." }) => {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh]">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
      <p className="text-gray-600 font-medium">{text}</p>
    </div>
  );
};
