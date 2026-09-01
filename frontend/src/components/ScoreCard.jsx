import React from 'react';
import { Card, CardContent } from './Card';
import { cn } from './Button';

export const ScoreCard = ({ title, score, maxScore = 100, className }) => {
  const percentage = (score / maxScore) * 100;
  
  let colorClass = 'text-blue-600';
  let strokeClass = 'stroke-blue-600';
  if (percentage >= 80) {
    colorClass = 'text-green-600';
    strokeClass = 'stroke-green-600';
  } else if (percentage < 50) {
    colorClass = 'text-red-600';
    strokeClass = 'stroke-red-600';
  } else if (percentage < 70) {
    colorClass = 'text-yellow-600';
    strokeClass = 'stroke-yellow-600';
  }

  return (
    <Card className={cn("flex flex-col items-center justify-center p-6", className)}>
      <h3 className="text-sm font-medium text-gray-500 mb-4">{title}</h3>
      <div className="relative w-24 h-24 flex items-center justify-center">
        <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
          <path
            className="stroke-gray-200"
            strokeWidth="3"
            strokeDasharray="100, 100"
            fill="none"
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
          />
          <path
            className={cn("transition-all duration-1000 ease-out", strokeClass)}
            strokeWidth="3"
            strokeDasharray={`${percentage}, 100`}
            fill="none"
            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
          />
        </svg>
        <div className="absolute flex flex-col items-center justify-center text-center">
          <span className={cn("text-2xl font-bold", colorClass)}>{score}</span>
          {maxScore !== 100 && <span className="text-xs text-gray-400">/ {maxScore}</span>}
        </div>
      </div>
    </Card>
  );
};
