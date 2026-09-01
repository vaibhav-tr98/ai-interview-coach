import React from 'react';
import { cn } from './Button';
import { AlertCircle, CheckCircle, Info, XCircle } from 'lucide-react';

export const Alert = ({ children, variant = 'info', className }) => {
  const variants = {
    info: 'bg-blue-50 text-blue-800 border-blue-200',
    success: 'bg-green-50 text-green-800 border-green-200',
    warning: 'bg-yellow-50 text-yellow-800 border-yellow-200',
    error: 'bg-red-50 text-red-800 border-red-200',
  };

  const icons = {
    info: <Info className="h-5 w-5 text-blue-400" />,
    success: <CheckCircle className="h-5 w-5 text-green-400" />,
    warning: <AlertCircle className="h-5 w-5 text-yellow-400" />,
    error: <XCircle className="h-5 w-5 text-red-400" />,
  };

  return (
    <div className={cn("rounded-md border p-4 flex", variants[variant], className)}>
      <div className="flex-shrink-0">{icons[variant]}</div>
      <div className="ml-3 flex-1 md:flex md:justify-between">
        <p className="text-sm">{children}</p>
      </div>
    </div>
  );
};
