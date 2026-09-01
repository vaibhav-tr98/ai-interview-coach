import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { analyticsService } from '../../api/services';
import { Card, CardContent } from '../../components/Card';
import { Button } from '../../components/Button';
import { PageHeader } from '../../components/PageHeader';
import { Badge } from '../../components/Badge';
import { EmptyState } from '../../components/EmptyState';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { FileText, ArrowRight, Calendar, Target, Clock } from 'lucide-react';

export const InterviewHistory = () => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const response = await analyticsService.getInterviewHistory();
        setHistory(response.data || []);
      } catch (err) {
        if (err.response?.status === 404) {
          // If backend history is not yet implemented
          setHistory([]);
        } else {
          setError('Unable to load interview history. Please try again later.');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchHistory();
  }, []);

  if (loading) return <LoadingScreen text="Loading your history..." />;

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <PageHeader 
        title="Interview History" 
        description="Review your past mock interviews and track progress."
        action={
          <Button onClick={() => navigate('/interviews/new')}>
            New Interview
          </Button>
        }
      />

      {error && <Alert variant="error">{error}</Alert>}

      {!error && history.length === 0 ? (
        <EmptyState 
          icon={FileText}
          title="No interviews yet"
          description="You haven't completed any mock interviews yet. Start one to see your history here."
          action={<Button onClick={() => navigate('/interviews/new')}>Start Practice</Button>}
        />
      ) : (
        <div className="space-y-4">
          {history.map((session, index) => (
            <Card key={index} className="hover:shadow-md transition-shadow">
              <CardContent className="p-5">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-lg font-semibold text-gray-900">
                        {session.type || 'Mock Interview'}
                      </h3>
                      <Badge variant={session.score >= 70 ? 'success' : 'warning'}>
                        {session.status || 'Completed'}
                      </Badge>
                    </div>
                    <div className="flex flex-wrap items-center gap-4 text-sm text-gray-500">
                      <div className="flex items-center gap-1">
                        <Calendar className="h-4 w-4" />
                        {new Date(session.date || Date.now()).toLocaleDateString()}
                      </div>
                      <div className="flex items-center gap-1">
                        <Target className="h-4 w-4" />
                        {session.difficulty || 'Medium'}
                      </div>
                      <div className="flex items-center gap-1">
                        <Clock className="h-4 w-4" />
                        {session.duration || '30 mins'}
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-4 border-t sm:border-t-0 sm:border-l border-gray-100 pt-4 sm:pt-0 sm:pl-6">
                    <div className="text-center">
                      <div className="text-2xl font-bold text-gray-900">{session.score || 0}%</div>
                      <div className="text-xs text-gray-500 uppercase tracking-wide">Score</div>
                    </div>
                    <Button 
                      variant="outline" 
                      className="gap-2"
                      onClick={() => navigate(`/interviews/${session.id || index}/result`)}
                    >
                      View Report <ArrowRight className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};
