import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { interviewService } from '../../api/services';
import { Card, CardHeader, CardContent } from '../../components/Card';
import { Button } from '../../components/Button';
import { ProgressBar } from '../../components/ProgressBar';
import { LoadingScreen } from '../../components/LoadingScreen';
import { Alert } from '../../components/Alert';
import { Bot, User, Send, StopCircle } from 'lucide-react';

export const InterviewSession = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const sessionId = id || 'active'; // Fallback if no ID in URL
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [answer, setAnswer] = useState('');
  
  // Mock initial state since we don't have the exact backend response shape
  // In a real scenario, this would be populated from the initial `/start` response
  const [sessionData, setSessionData] = useState({
    currentQuestionIndex: 1,
    totalQuestions: 5,
    question: location.state?.initialData?.question || "Tell me about yourself and your experience.",
    transcript: []
  });

  const messagesEndRef = useRef(null);

  useEffect(() => {
    // If we have existing transcript, scroll to bottom
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [sessionData.transcript]);

  const handleSubmitAnswer = async () => {
    if (!answer.trim()) return;
    
    setLoading(true);
    setError(null);
    
    // Add user answer to local transcript immediately for UI responsiveness
    const newTranscript = [
      ...sessionData.transcript,
      { sender: 'USER', text: answer }
    ];
    
    setSessionData(prev => ({ ...prev, transcript: newTranscript }));
    const currentAnswer = answer;
    setAnswer('');

    try {
      // Send answer to backend
      const response = await interviewService.answer(sessionId, { answer: currentAnswer });
      
      if (response.data?.isComplete || sessionData.currentQuestionIndex >= sessionData.totalQuestions) {
        navigate(`/interviews/${sessionId}/result`);
      } else {
        // Add AI next question
        setSessionData(prev => ({
          ...prev,
          currentQuestionIndex: prev.currentQuestionIndex + 1,
          question: response.data?.nextQuestion || "What is your greatest strength?",
          transcript: [
            ...prev.transcript,
            { sender: 'AI', text: response.data?.nextQuestion || "What is your greatest strength?" }
          ]
        }));
      }
    } catch (err) {
      setError('Failed to submit answer. Please try again.');
      // Revert answer if failed
      setAnswer(currentAnswer);
      setSessionData(prev => ({
        ...prev,
        transcript: prev.transcript.filter((_, i) => i !== prev.transcript.length - 1)
      }));
    } finally {
      setLoading(false);
    }
  };

  const handleEndInterview = () => {
    if (window.confirm('Are you sure you want to end this interview early? Your progress will be saved.')) {
      navigate(`/interviews/${sessionId}/result`);
    }
  };

  return (
    <div className="max-w-4xl mx-auto h-[calc(100vh-8rem)] flex flex-col">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-bold text-gray-900">Live Interview Session</h1>
        <Button variant="outline" className="text-red-600 border-red-200 hover:bg-red-50" onClick={handleEndInterview}>
          <StopCircle className="h-4 w-4 mr-2" /> End Interview
        </Button>
      </div>

      <div className="mb-4">
        <div className="flex justify-between text-sm text-gray-500 mb-1">
          <span>Question {sessionData.currentQuestionIndex} of {sessionData.totalQuestions}</span>
        </div>
        <ProgressBar progress={(sessionData.currentQuestionIndex / sessionData.totalQuestions) * 100} />
      </div>

      {error && <Alert variant="error" className="mb-4">{error}</Alert>}

      {/* Transcript Area */}
      <Card className="flex-1 flex flex-col overflow-hidden mb-4 border-gray-200">
        <div className="flex-1 overflow-y-auto p-4 space-y-6">
          {sessionData.transcript.map((msg, idx) => (
            <div key={idx} className={`flex gap-4 ${msg.sender === 'USER' ? 'flex-row-reverse' : ''}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
                msg.sender === 'USER' ? 'bg-blue-100 text-blue-600' : 'bg-emerald-100 text-emerald-600'
              }`}>
                {msg.sender === 'USER' ? <User className="h-5 w-5" /> : <Bot className="h-5 w-5" />}
              </div>
              <div className={`max-w-[80%] rounded-lg p-4 ${
                msg.sender === 'USER' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-800'
              }`}>
                <p className="whitespace-pre-wrap text-sm leading-relaxed">{msg.text}</p>
              </div>
            </div>
          ))}
          
          {/* Current Question */}
          <div className="flex gap-4">
            <div className="w-8 h-8 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center shrink-0">
              <Bot className="h-5 w-5" />
            </div>
            <div className="max-w-[80%] rounded-lg p-4 bg-gray-100 text-gray-800 border-l-4 border-emerald-500">
              <p className="whitespace-pre-wrap text-sm font-medium leading-relaxed">{sessionData.question}</p>
            </div>
          </div>
          <div ref={messagesEndRef} />
        </div>
      </Card>

      {/* Input Area */}
      <div className="bg-white rounded-lg border border-gray-200 p-2 focus-within:ring-2 focus-within:ring-blue-500 focus-within:border-transparent transition-all">
        <textarea
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          placeholder="Type your answer here..."
          className="w-full resize-none border-0 focus:ring-0 p-2 text-sm text-gray-900 bg-transparent min-h-[100px]"
          disabled={loading}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
              handleSubmitAnswer();
            }
          }}
        />
        <div className="flex justify-between items-center px-2 pt-2 border-t border-gray-100 mt-2">
          <span className="text-xs text-gray-400">Press Ctrl+Enter to submit</span>
          <Button 
            onClick={handleSubmitAnswer} 
            disabled={loading || !answer.trim()}
            className="gap-2"
          >
            {loading ? 'Submitting...' : 'Submit Answer'}
            {!loading && <Send className="h-4 w-4" />}
          </Button>
        </div>
      </div>
    </div>
  );
};
