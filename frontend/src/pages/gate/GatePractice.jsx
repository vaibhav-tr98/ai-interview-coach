import React, { useState, useEffect } from 'react';
import { gateService } from '../../api/services';
import { PageHeader } from '../../components/PageHeader';
import { EmptyState } from '../../components/EmptyState';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { BookOpen } from 'lucide-react';

export const GatePractice = () => {
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchSubjects = async () => {
      try {
        const response = await gateService.getSubjects();
        setSubjects(response.data || []);
      } catch (err) {
        if (err.response?.status === 404) {
          setSubjects([]);
        } else {
          setError('Unable to load GATE subjects.');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchSubjects();
  }, []);

  if (loading) return <LoadingScreen text="Loading GATE prep module..." />;

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <PageHeader 
        title="GATE Preparation" 
        description="Topic-wise practice questions for GATE CS/IT."
      />

      {error && <Alert variant="error">{error}</Alert>}

      {!error && subjects.length === 0 ? (
        <EmptyState 
          icon={BookOpen}
          title="GATE prep coming soon"
          description="The GATE preparation module is currently under development."
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Render subjects when available */}
        </div>
      )}
    </div>
  );
};
