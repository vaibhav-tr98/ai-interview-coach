import api from './axios';

// Auth Services
export const authService = {
  login: (data) => api.post('/api/v1/users/login', data),
  register: (data) => api.post('/api/v1/users/register', data),
};

// Dashboard Services
export const dashboardService = {
  getDashboard: () => api.get('/api/v1/dashboard'),
};

// Interview Services
export const interviewService = {
  start: (data) => api.post('/api/interview/start', data),
  answer: (sessionId, data) => api.post(`/api/interview/${sessionId}/answer`, data),
  getResult: (sessionId) => api.get(`/api/interview/${sessionId}/result`),
  getTranscript: (sessionId) => api.get(`/api/interview/${sessionId}/transcript`),
};

// Coding Services
export const codingService = {
  getProblems: () => api.get('/api/v1/coding/problems'),
  getProblem: (id) => api.get(`/api/v1/coding/problems/${id}`),
  generateProblem: (data) => api.post('/api/v1/coding/problems/generate', data),
  submitCode: (id, data) => api.post(`/api/v1/coding/problems/${id}/submit`, data),
  getProgress: () => api.get('/api/v1/coding/progress'),
};

// Gate Services
export const gateService = {
  getSubjects: () => api.get('/api/v1/gate/subjects'),
  getTopics: (subject) => api.get(`/api/v1/gate/subjects/${subject}/topics`),
  generateQuestion: (data) => api.post('/api/v1/gate/questions/generate', data),
  attemptQuestion: (id, data) => api.post(`/api/v1/gate/questions/${id}/attempt`, data),
  getProgress: () => api.get('/api/v1/gate/progress'),
};

// Deep Interview Services
export const deepInterviewService = {
  startResume: (resumeId) => api.post(`/api/v1/deep-interview/resume/${resumeId}/start`),
  startProject: (projectId) => api.post(`/api/v1/deep-interview/project/${projectId}/start`),
  submitAnswer: (sessionId, data) => api.post(`/api/v1/deep-interview/${sessionId}/answer`, data),
  getResult: (sessionId) => api.get(`/api/v1/deep-interview/${sessionId}/result`),
};

// English Practice Services
export const englishService = {
  getSessions: () => api.get('/api/v1/english/sessions'),
  createSession: (data) => api.post('/api/v1/english/sessions', data),
  addMessage: (sessionId, data) => api.post(`/api/v1/english/sessions/${sessionId}/messages`, data),
  evaluate: (sessionId) => api.post(`/api/v1/english/sessions/${sessionId}/evaluate`),
};

// Communication Services
export const communicationService = {
  assess: (interviewId) => api.post(`/api/v1/communication/assess/${interviewId}`),
  getOverview: () => api.get('/api/v1/communication/overview'),
};

// Study Plan Services
export const studyPlanService = {
  getActivePlan: () => api.get('/api/v1/planner/active'),
  generatePlan: () => api.post('/api/v1/planner/generate'),
};

// Analytics Services
export const analyticsService = {
  getOverview: () => api.get('/api/analytics/overview'),
  getInterviewHistory: () => api.get('/api/analytics/interviews'),
};

// Projects Services
export const projectService = {
  getProjects: () => api.get('/api/v1/projects'),
  createProject: (data) => api.post('/api/v1/projects', data),
};

// Profile / Resume Services
export const profileService = {
  getProfile: () => api.get('/api/v1/users/profile'),
  getResumes: () => api.get('/api/resumes'),
  createResume: (data) => api.post('/api/resumes', data),
};
