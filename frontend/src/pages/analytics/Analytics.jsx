import React, { useState, useEffect } from 'react';
import { analyticsService } from '../../api/services';
import { PageHeader } from '../../components/PageHeader';
import { EmptyState } from '../../components/EmptyState';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { BarChart3 } from 'lucide-react';

export const Analytics = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const response = await analyticsService.getOverview();
        setData(response.data);
      } catch (err) {
        if (err.response?.status === 404) {
          setData(null);
        } else {
          setError('Unable to load analytics.');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchAnalytics();
  }, []);

  if (loading) return <LoadingScreen text="Loading analytics..." />;

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <PageHeader 
        title="Performance Analytics" 
        description="Detailed insights into your interview readiness."
      />

      {error && <Alert variant="error">{error}</Alert>}

      {!error && !data ? (
        <EmptyState 
          icon={BarChart3}
          title="No analytics available yet"
          description="Complete your first mock interview to see your performance metrics here."
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Render charts/stats */}
        </div>
      )}
    </div>
  );
};
