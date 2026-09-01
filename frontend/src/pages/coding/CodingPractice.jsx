import React, { useState, useEffect } from 'react';
import { codingService } from '../../api/services';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/Card';
import { Button } from '../../components/Button';
import { PageHeader } from '../../components/PageHeader';
import { Badge } from '../../components/Badge';
import { EmptyState } from '../../components/EmptyState';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { Code, Play } from 'lucide-react';

export const CodingPractice = () => {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProblems = async () => {
      try {
        const response = await codingService.getProblems();
        setProblems(response.data || []);
      } catch (err) {
        if (err.response?.status === 404) {
          setProblems([]); // API not ready
        } else {
          setError('Unable to load coding problems. Please try again later.');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchProblems();
  }, []);

  if (loading) return <LoadingScreen text="Loading problems..." />;

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <PageHeader 
        title="Coding Practice" 
        description="Master Data Structures and Algorithms with AI-assisted feedback."
        action={
          <Button className="gap-2">
            <Play className="h-4 w-4" /> Start Random Problem
          </Button>
        }
      />

      {error && <Alert variant="error">{error}</Alert>}

      {!error && problems.length === 0 ? (
        <EmptyState 
          icon={Code}
          title="Coding practice is coming soon"
          description="We are currently building out the coding practice module. Check back later!"
        />
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {problems.map((prob, idx) => (
            <Card key={idx} className="hover:shadow-md transition-shadow">
              <CardContent className="p-4 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-3 mb-1">
                    <h3 className="font-semibold text-gray-900">{prob.title}</h3>
                    <Badge variant={prob.difficulty === 'Easy' ? 'success' : prob.difficulty === 'Hard' ? 'danger' : 'warning'}>
                      {prob.difficulty}
                    </Badge>
                  </div>
                  <p className="text-sm text-gray-500 line-clamp-1">{prob.description}</p>
                </div>
                <Button variant="outline">Solve</Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};
