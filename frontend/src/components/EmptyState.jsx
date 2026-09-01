import React from 'react';
import { cn } from './Button';
import { FileQuestion } from 'lucide-react';

export const EmptyState = ({ 
  icon: Icon = FileQuestion, 
  title = 'No data found', 
  description = 'There is currently no data to display here.',
  action,
  className
}) => {
  return (
    <div className={cn("flex flex-col items-center justify-center p-8 text-center bg-white rounded-lg border border-dashed border-gray-300", className)}>
      <Icon className="h-12 w-12 text-gray-400 mb-4" />
      <h3 className="text-lg font-medium text-gray-900">{title}</h3>
      <p className="mt-1 text-sm text-gray-500 max-w-sm">{description}</p>
      {action && <div className="mt-6">{action}</div>}
    </div>
  );
};
