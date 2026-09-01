import React, { useState, useEffect } from 'react';
import { englishService } from '../../api/services';
import { PageHeader } from '../../components/PageHeader';
import { EmptyState } from '../../components/EmptyState';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { MessageSquare } from 'lucide-react';

export const EnglishPractice = () => {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchSessions = async () => {
      try {
        const response = await englishService.getSessions();
        setSessions(response.data || []);
      } catch (err) {
        if (err.response?.status === 404) {
          setSessions([]);
        } else {
          setError('Unable to load English practice sessions.');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchSessions();
  }, []);

  if (loading) return <LoadingScreen text="Loading English module..." />;

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <PageHeader 
        title="English & Communication Practice" 
        description="Improve your professional communication and fluency."
      />

      {error && <Alert variant="error">{error}</Alert>}

      {!error && sessions.length === 0 ? (
        <EmptyState 
          icon={MessageSquare}
          title="English practice coming soon"
          description="We are currently building out the interactive English communication module."
        />
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {/* Render sessions */}
        </div>
      )}
    </div>
  );
};
