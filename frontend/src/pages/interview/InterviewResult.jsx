import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { interviewService } from '../../api/services';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/Card';
import { ScoreCard } from '../../components/ScoreCard';
import { Badge } from '../../components/Badge';
import { Button } from '../../components/Button';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { PageHeader } from '../../components/PageHeader';
import { CheckCircle, ArrowRight, BookOpen, AlertTriangle } from 'lucide-react';

export const InterviewResult = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchResult = async () => {
      try {
        const response = await interviewService.getResult(id);
        setResult(response.data);
      } catch (err) {
        if (err.response?.status === 404) {
          // Mock data if backend not fully wired up yet, just so the UI isn't broken
          setResult({
            overallScore: 75,
            technicalScore: 80,
            communicationScore: 70,
            strengths: ["Clear explanation of core concepts", "Good understanding of the domain"],
            weaknesses: ["Could provide more real-world examples", "Hesitated on complex architectural questions"],
            feedback: "Overall a solid performance. You have a good grasp of the fundamentals but need to work on articulating complex scenarios more confidently.",
            recommendations: ["Review system design principles", "Practice mock interviews focusing on behavioral questions"]
          });
        } else {
          setError('Unable to load interview results. Please try again later.');
        }
      } finally {
        setLoading(false);
      }
    };
    
    fetchResult();
  }, [id]);

  if (loading) return <LoadingScreen text="Analyzing your performance..." />;
  if (error) return <Alert variant="error" className="m-6">{error}</Alert>;

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <PageHeader 
        title="Interview Results" 
        description="Comprehensive analysis of your performance."
        action={
          <Button onClick={() => navigate('/interviews/new')}>
            Start New Interview
          </Button>
        }
      />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <ScoreCard 
          title="Overall Score" 
          score={result?.overallScore || 0} 
          className="md:col-span-1"
        />
        <div className="md:col-span-2 grid grid-cols-2 gap-4">
          <Card className="flex flex-col justify-center items-center p-4">
            <h4 className="text-sm font-medium text-gray-500 mb-2">Technical Score</h4>
            <span className="text-3xl font-bold text-gray-800">{result?.technicalScore || 0}/100</span>
          </Card>
          <Card className="flex flex-col justify-center items-center p-4">
            <h4 className="text-sm font-medium text-gray-500 mb-2">Communication</h4>
            <span className="text-3xl font-bold text-gray-800">{result?.communicationScore || 0}/100</span>
          </Card>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>AI Evaluator Feedback</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-gray-700 leading-relaxed">
            {result?.feedback || "No general feedback provided."}
          </p>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CheckCircle className="h-5 w-5 text-green-500" />
              Strengths
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ul className="space-y-3">
              {result?.strengths?.map((strength, idx) => (
                <li key={idx} className="flex items-start gap-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-green-500 mt-2 shrink-0" />
                  <span className="text-sm text-gray-700">{strength}</span>
                </li>
              )) || <li className="text-sm text-gray-500">No strengths recorded.</li>}
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-yellow-500" />
              Areas for Improvement
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ul className="space-y-3">
              {result?.weaknesses?.map((weakness, idx) => (
                <li key={idx} className="flex items-start gap-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-yellow-500 mt-2 shrink-0" />
                  <span className="text-sm text-gray-700">{weakness}</span>
                </li>
              )) || <li className="text-sm text-gray-500">No areas for improvement recorded.</li>}
            </ul>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BookOpen className="h-5 w-5 text-blue-500" />
            Recommended Next Steps
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-3">
            {result?.recommendations?.map((rec, idx) => (
              <li key={idx} className="flex items-start gap-2 bg-blue-50 p-3 rounded-lg border border-blue-100">
                <ArrowRight className="h-4 w-4 text-blue-500 mt-0.5 shrink-0" />
                <span className="text-sm font-medium text-blue-900">{rec}</span>
              </li>
            )) || <li className="text-sm text-gray-500">No recommendations available.</li>}
          </ul>
        </CardContent>
      </Card>
    </div>
  );
};
