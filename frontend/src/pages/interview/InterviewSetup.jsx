import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { interviewService } from '../../api/services';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/Card';
import { Button } from '../../components/Button';
import { PageHeader } from '../../components/PageHeader';
import { Alert } from '../../components/Alert';
import { Play } from 'lucide-react';

export const InterviewSetup = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    interviewType: 'HR',
    difficulty: 'MEDIUM',
    topic: 'General',
    experienceLevel: 'JUNIOR',
    role: '',
    resume: '',
    jobDescription: '',
    projectDescription: '',
    interviewerPersona: 'FRIENDLY'
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await interviewService.start(formData);
      // Assuming response contains the sessionId
      const sessionId = response.data?.id || response.data?.sessionId;
      if (sessionId) {
        navigate(`/interviews/${sessionId}`);
      } else {
        // Fallback if ID is in a different format
        navigate('/interviews/session-active', { state: { config: formData, initialData: response.data }});
      }
    } catch (err) {
      if (err.response?.status === 404) {
        setError('This interview type is currently not available on the server. Try a different type.');
      } else {
        setError('Failed to start interview. Please try again later.');
      }
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div className="max-w-3xl mx-auto">
      <PageHeader 
        title="Start New Interview" 
        description="Configure your AI mock interview session."
      />

      {error && <Alert variant="error" className="mb-6">{error}</Alert>}

      <Card>
        <CardHeader>
          <CardTitle>Interview Configuration</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Interview Type</label>
                <select
                  name="interviewType"
                  value={formData.interviewType}
                  onChange={handleChange}
                  className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="HR">HR & Behavioral</option>
                  <option value="JAVA">Java Technical</option>
                  <option value="SPRING_BOOT">Spring Boot</option>
                  <option value="MERN">MERN Stack</option>
                  <option value="SQL">Database & SQL</option>
                  <option value="DSA">Data Structures (DSA)</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Experience Level</label>
                <select
                  name="experienceLevel"
                  value={formData.experienceLevel}
                  onChange={handleChange}
                  className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="FRESHER">Fresher (0 years)</option>
                  <option value="JUNIOR">Junior (1-3 years)</option>
                  <option value="MID_LEVEL">Mid-Level (3-5 years)</option>
                  <option value="SENIOR">Senior (5+ years)</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Difficulty</label>
                <select
                  name="difficulty"
                  value={formData.difficulty}
                  onChange={handleChange}
                  className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="EASY">Easy</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HARD">Hard</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Specific Topic (Optional)</label>
                <input
                  type="text"
                  name="topic"
                  value={formData.topic}
                  onChange={handleChange}
                  placeholder="e.g., Multithreading, React Hooks"
                  className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Role</label>
                <input
                  type="text"
                  name="role"
                  value={formData.role}
                  onChange={handleChange}
                  placeholder="e.g. Java Backend Developer"
                  className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Interviewer Persona</label>
                <select
                  name="interviewerPersona"
                  value={formData.interviewerPersona}
                  onChange={handleChange}
                  className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="FRIENDLY">FRIENDLY</option>
                  <option value="PROFESSIONAL">PROFESSIONAL</option>
                  <option value="STRICT">STRICT</option>
                  <option value="TECHNICAL">TECHNICAL</option>
                </select>
              </div>
            </div>

            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Resume (Plain Text)</label>
              <textarea
                name="resume"
                value={formData.resume}
                onChange={handleChange}
                placeholder="Paste your resume here..."
                rows={4}
                className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Job Description (Plain Text)</label>
              <textarea
                name="jobDescription"
                value={formData.jobDescription}
                onChange={handleChange}
                placeholder="Paste the target job description here..."
                rows={4}
                className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Project Description (Plain Text)</label>
              <textarea
                name="projectDescription"
                value={formData.projectDescription}
                onChange={handleChange}
                placeholder="Paste details of a project you want to be interviewed on..."
                rows={4}
                className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="pt-4 border-t border-gray-100 flex justify-end gap-3">
              <Button type="button" variant="outline" onClick={() => navigate(-1)}>
                Cancel
              </Button>
              <Button type="submit" disabled={loading} className="gap-2">
                {loading ? (
                  <>Starting...</>
                ) : (
                  <>
                    <Play className="h-4 w-4" fill="currentColor" /> Start Interview
                  </>
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};
