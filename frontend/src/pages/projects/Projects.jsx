import React, { useState, useEffect } from 'react';
import { projectService } from '../../api/services';
import { PageHeader } from '../../components/PageHeader';
import { EmptyState } from '../../components/EmptyState';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { Button } from '../../components/Button';
import { Briefcase, Plus } from 'lucide-react';

export const Projects = () => {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const response = await projectService.getProjects();
        setProjects(response.data || []);
      } catch (err) {
        if (err.response?.status === 404) {
          setProjects([]);
        } else {
          setError('Unable to load projects.');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchProjects();
  }, []);

  if (loading) return <LoadingScreen text="Loading projects..." />;

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <PageHeader 
        title="My Projects" 
        description="Manage projects you want to use for deep-dive interviews."
        action={
          <Button className="gap-2">
            <Plus className="h-4 w-4" /> Add Project
          </Button>
        }
      />

      {error && <Alert variant="error">{error}</Alert>}

      {!error && projects.length === 0 ? (
        <EmptyState 
          icon={Briefcase}
          title="No projects added"
          description="Add your projects here to unlock project-based deep dive interviews."
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Render projects */}
        </div>
      )}
    </div>
  );
};
