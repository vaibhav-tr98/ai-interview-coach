import React, { useEffect, useState } from 'react';
import { dashboardService } from '../../api/services';
import { Card, CardHeader, CardTitle, CardContent } from '../../components/Card';
import { Activity, Target, Code, BookOpen, ArrowRight, Play, FileText, MessageSquare } from 'lucide-react';
import { Button } from '../../components/Button';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { StatCard } from '../../components/StatCard';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';

export const Dashboard = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const response = await dashboardService.getDashboard();
        setData(response.data);
      } catch (err) {
        if (err.response?.status === 404) {
          // Backend might not have this endpoint yet, provide empty state instead of failing
          setData({});
        } else {
          setError('Unable to load dashboard data. Please try again later.');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchDashboard();
  }, []);

  if (loading) return <LoadingScreen text="Loading dashboard..." />;
  
  const quickActions = [
    { title: 'Start Interview', icon: Play, href: '/interviews/new', color: 'bg-blue-100 text-blue-600' },
    { title: 'Coding Practice', icon: Code, href: '/coding', color: 'bg-purple-100 text-purple-600' },
    { title: 'Deep Dive', icon: FileText, href: '/deep-dive', color: 'bg-indigo-100 text-indigo-600' },
    { title: 'English Practice', icon: MessageSquare, href: '/english', color: 'bg-emerald-100 text-emerald-600' },
  ];

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {error && <Alert variant="error">{error}</Alert>}
      
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-gray-900">
            Welcome back, {user?.name?.split(' ')[0] || 'User'}! 👋
          </h2>
          <p className="text-gray-500 mt-1">Ready to improve your interview performance today?</p>
        </div>
        <Button onClick={() => navigate('/interviews/new')} className="gap-2 shrink-0">
          <Play className="h-4 w-4" fill="currentColor" />
          Start New Interview
        </Button>
      </div>
      
      {/* Quick Actions */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        {quickActions.map((action, index) => (
          <button
            key={index}
            onClick={() => navigate(action.href)}
            className="flex flex-col items-center justify-center p-6 bg-white border border-gray-200 rounded-xl hover:shadow-md transition-shadow group focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <div className={`p-3 rounded-full mb-3 group-hover:scale-110 transition-transform ${action.color}`}>
              <action.icon className="h-6 w-6" />
            </div>
            <span className="text-sm font-medium text-gray-900">{action.title}</span>
          </button>
        ))}
      </div>
      
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard 
          title="Readiness Score" 
          value={`${data?.overallReadinessScore?.toFixed(1) || '0'}%`}
          icon={Target}
          description="Based on recent mock interviews"
        />
        <StatCard 
          title="Interviews Completed" 
          value={data?.completedInterviews || 0}
          icon={Activity}
          description="Total sessions completed"
        />
        <StatCard 
          title="Coding Success" 
          value={`${data?.codingSuccessRate?.toFixed(1) || '0'}%`}
          icon={Code}
          description="Average test cases passed"
        />
        <StatCard 
          title="GATE Accuracy" 
          value={`${data?.gateAccuracy?.toFixed(1) || '0'}%`}
          icon={BookOpen}
          description="In recent practice tests"
        />
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        <Card className="col-span-1 lg:col-span-2">
          <CardHeader className="border-b border-gray-100 pb-4">
            <CardTitle>Recent Activity</CardTitle>
          </CardHeader>
          <CardContent className="pt-4">
            {data?.recentActivity && data.recentActivity.length > 0 ? (
              <div className="space-y-4">
                {data.recentActivity.map((activity, index) => (
                  <div key={index} className="flex items-start gap-4">
                    <div className="w-2 h-2 mt-2 rounded-full bg-blue-500 shrink-0" />
                    <div>
                      <p className="text-sm font-medium text-gray-900">{activity.title || activity.description}</p>
                      <p className="text-xs text-gray-500">{activity.date || 'Recently'}</p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-8 text-center text-gray-500">
                <Activity className="h-8 w-8 text-gray-300 mx-auto mb-3" />
                <p>No recent activity found.</p>
                <p className="text-sm mt-1">Start a practice session to see your progress here.</p>
              </div>
            )}
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="border-b border-gray-100 pb-4">
            <CardTitle>Recommended for You</CardTitle>
          </CardHeader>
          <CardContent className="pt-4">
            <div className="bg-blue-50 p-4 rounded-lg border border-blue-100">
              <h4 className="font-medium text-blue-900 mb-2">Suggested Next Step</h4>
              <p className="text-sm text-blue-700 mb-4">
                {data?.recommendedNextAction || "Try a technical interview to establish your baseline score."}
              </p>
              <Button size="sm" variant="outline" className="w-full bg-white text-blue-700 border-blue-200 hover:bg-blue-50" onClick={() => navigate('/interviews/new')}>
                Start Now
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
