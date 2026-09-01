import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';

import { AuthLayout } from './layouts/AuthLayout';
import { DashboardLayout } from './layouts/DashboardLayout';

import { Login } from './pages/auth/Login';
import { Register } from './pages/auth/Register';
import { Dashboard } from './pages/dashboard/Dashboard';

import { InterviewSetup } from './pages/interview/InterviewSetup';
import { InterviewSession } from './pages/interview/InterviewSession';
import { InterviewResult } from './pages/interview/InterviewResult';
import { InterviewHistory } from './pages/interview/InterviewHistory';

import { CodingPractice } from './pages/coding/CodingPractice';
import { GatePractice } from './pages/gate/GatePractice';
import { DeepDive } from './pages/deep-dive/DeepDive';
import { EnglishPractice } from './pages/english/EnglishPractice';
import { StudyPlan } from './pages/study-plan/StudyPlan';
import { Projects } from './pages/projects/Projects';
import { Analytics } from './pages/analytics/Analytics';
import { Profile } from './pages/profile/Profile';
import { NotFound } from './pages/NotFound';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Auth Routes */}
          <Route element={<AuthLayout />}>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
          </Route>

          {/* Protected Routes */}
          <Route element={<ProtectedRoute><DashboardLayout /></ProtectedRoute>}>
            <Route path="/" element={<Dashboard />} />
            
            {/* Interview Module */}
            <Route path="/interviews" element={<InterviewHistory />} />
            <Route path="/interviews/new" element={<InterviewSetup />} />
            <Route path="/interviews/:id" element={<InterviewSession />} />
            <Route path="/interviews/:id/result" element={<InterviewResult />} />
            
            {/* Feature Modules */}
            <Route path="/coding" element={<CodingPractice />} />
            <Route path="/gate" element={<GatePractice />} />
            <Route path="/deep-dive" element={<DeepDive />} />
            <Route path="/english" element={<EnglishPractice />} />
            <Route path="/study-plan" element={<StudyPlan />} />
            <Route path="/projects" element={<Projects />} />
            <Route path="/analytics" element={<Analytics />} />
            <Route path="/profile" element={<Profile />} />
            
            {/* Catch-all */}
            <Route path="*" element={<NotFound />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
