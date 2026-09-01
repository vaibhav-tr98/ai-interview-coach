import React, { useState, useEffect } from 'react';
import { studyPlanService } from '../../api/services';
import { PageHeader } from '../../components/PageHeader';
import { EmptyState } from '../../components/EmptyState';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { BookOpen } from 'lucide-react';

export const StudyPlan = () => {
  const [plan, setPlan] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPlan = async () => {
      try {
        const response = await studyPlanService.getActivePlan();
        setPlan(response.data);
      } catch (err) {
        if (err.response?.status === 404) {
          setPlan(null);
        } else {
          setError('Unable to load study plan.');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchPlan();
  }, []);

  if (loading) return <LoadingScreen text="Loading study plan..." />;

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <PageHeader 
        title="Personalized Study Plan" 
        description="Your AI-generated roadmap to interview success."
      />

      {error && <Alert variant="error">{error}</Alert>}

      {!error && !plan ? (
        <EmptyState 
          icon={BookOpen}
          title="No active study plan"
          description="Generate a personalized study plan to guide your preparation."
        />
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {/* Render study plan */}
        </div>
      )}
    </div>
  );
};
