import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '../../components/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/Card';
import { Button } from '../../components/Button';
import { FileText, Briefcase } from 'lucide-react';

export const DeepDive = () => {
  const navigate = useNavigate();

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <PageHeader 
        title="Deep Dive Interviews" 
        description="Specialized interviews focused on your specific resume or projects."
      />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="hover:border-blue-500 transition-colors">
          <CardHeader>
            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4">
              <FileText className="h-6 w-6 text-blue-600" />
            </div>
            <CardTitle>Resume-Based Deep Dive</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-6 text-sm">
              Upload your resume and our AI will grill you on every bullet point, technology, and experience listed. Perfect for final round preparation.
            </p>
            <Button className="w-full" variant="outline" onClick={() => alert('Resume parsing is coming soon!')}>
              Upload Resume & Start
            </Button>
          </CardContent>
        </Card>

        <Card className="hover:border-indigo-500 transition-colors">
          <CardHeader>
            <div className="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center mb-4">
              <Briefcase className="h-6 w-6 text-indigo-600" />
            </div>
            <CardTitle>Project-Based Deep Dive</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-6 text-sm">
              Select one of your saved projects. The AI will act as a senior engineer doing a deep architectural and technical review of your project.
            </p>
            <Button className="w-full" variant="outline" onClick={() => navigate('/projects')}>
              Select Project & Start
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
