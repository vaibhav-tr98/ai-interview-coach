import React from 'react';
import { cn } from './Button';

export const ProgressBar = ({ progress, className, colorClass = "bg-blue-600", showLabel = false }) => {
  const safeProgress = Math.min(Math.max(progress || 0, 0), 100);
  
  return (
    <div className={cn("w-full", className)}>
      <div className="flex justify-between mb-1">
        {showLabel && <span className="text-xs font-medium text-gray-700">Progress</span>}
        {showLabel && <span className="text-xs font-medium text-gray-700">{Math.round(safeProgress)}%</span>}
      </div>
      <div className="w-full bg-gray-200 rounded-full h-2.5">
        <div 
          className={cn("h-2.5 rounded-full transition-all duration-500", colorClass)} 
          style={{ width: `${safeProgress}%` }}
        ></div>
      </div>
    </div>
  );
};
