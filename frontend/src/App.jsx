import React, { useEffect, useState } from 'react';
import api from './api/client';
import {
  Bot, CheckCircle2, Clock, LogOut, MessageSquare,
  PlusCircle, Send, Shield, Sparkles, Star, Ticket as TicketIcon, BarChart3, Users
} from 'lucide-react';

const badgeColors = {
  CRITICAL: 'bg-red-100 text-red-800 border-red-200',
  HIGH: 'bg-orange-100 text-orange-800 border-orange-200',
  MEDIUM: 'bg-amber-100 text-amber-800 border-amber-200',
  LOW: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  ANGRY: 'bg-rose-100 text-rose-800 border-rose-200',
  NEGATIVE: 'bg-orange-100 text-orange-800 border-orange-200',
  NEUTRAL: 'bg-slate-100 text-slate-700 border-slate-200',
  POSITIVE: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  OPEN: 'bg-blue-100 text-blue-800 border-blue-200',
  IN_PROGRESS: 'bg-purple-100 text-purple-800 border-purple-200',
  RESOLVED: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  CLOSED: 'bg-slate-200 text-slate-700 border-slate-300',
};

export default function App() {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [authMode, setAuthMode] = useState('login');
  const [authForm, setAuthForm] = useState({ name: '', email: '', password: '', role: 'CUSTOMER' });
  const [authError, setAuthError] = useState('');

  const [tab, setTab] = useState('tickets'); // tickets | analytics
  const [tickets, setTickets] = useState([]);
  const [agents, setAgents] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [complaintForm, setComplaintForm] = useState({ title: '', description: '' });
  const [submitting, setSubmitting] = useState(false);
  const [sendingComment, setSendingComment] = useState(false);
  const [feedback, setFeedback] = useState({ rating: 5, comment: '' });

  useEffect(() => {
    if (user) {
      loadTickets();
      loadAnalytics();
      if (user.role !== 'CUSTOMER') loadAgents();
    }
  }, [user]);

  const loadTickets = async () => {
    const { data } = await api.get('/tickets');
    setTickets(data);
    if (selectedTicket) {
      const updated = data.find((t) => t.id === selectedTicket.id);
      if (updated) setSelectedTicket(updated);
    }
  };

  const loadAgents = async () => {
    const { data } = await api.get('/agents');
    setAgents(data);
  };

  const loadAnalytics = async () => {
    const { data } = await api.get('/analytics');
    setAnalytics(data);
  };

  const openTicket = async (ticket) => {
    setSelectedTicket(ticket);
    const { data } = await api.get(`/tickets/${ticket.id}/comments`);
    setComments(data);
  };

  const handleAuth = async (e) => {
    e.preventDefault();
    setAuthError('');
    try {
      const endpoint = authMode === 'login' ? '/auth/login' : '/auth/register';
      const { data } = await api.post(endpoint, authForm);
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data.user));
      setUser(data.user);
    } catch (err) {
      setAuthError(err.response?.data?.error || 'Authentication failed');
    }
  };

  const demoLogin = async (email) => {
    setAuthError('');
    try {
      const { data } = await api.post('/auth/login', { email, password: 'password123' });
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data.user));
      setUser(data.user);
    } catch {
      setAuthError('Backend not reachable. Start Spring Boot on port 8080.');
    }
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
    setSelectedTicket(null);
  };

  const submitComplaint = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const { data } = await api.post('/tickets', complaintForm);
      setComplaintForm({ title: '', description: '' });
      await loadTickets();
      await loadAnalytics();
      openTicket(data);
    } finally {
      setSubmitting(false);
    }
  };

  const updateStatus = async (id, status) => {
    await api.patch(`/tickets/${id}/status`, { status });
    await loadTickets();
    await loadAnalytics();
  };

  const assignAgent = async (id, agentId) => {
    await api.post(`/tickets/${id}/assign`, { agentId: Number(agentId) });
    await loadTickets();
  };

  const postComment = async (textOverride) => {
    const msg = textOverride ?? newComment;
    if (!msg.trim() || !selectedTicket) return;
    await api.post(`/tickets/${selectedTicket.id}/comments`, { message: msg, isInternal: false });
    if (!textOverride) setNewComment('');
    const { data } = await api.get(`/tickets/${selectedTicket.id}/comments`);
    setComments(data);
  };

  const submitFeedback = async (e) => {
    e.preventDefault();
    await api.post(`/tickets/${selectedTicket.id}/feedback`, feedback);
    await loadTickets();
    await loadAnalytics();
  };

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 p-4">
        <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl p-8 space-y-6">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-indigo-600 rounded-xl text-white">
              <Bot className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-slate-900">SmartSupport AI</h1>
              <p className="text-xs text-slate-500">AI-Powered Complaint &amp; Ticket Platform</p>
            </div>
          </div>

          <div className="flex gap-2 bg-slate-100 p-1 rounded-lg">
            <button
              onClick={() => setAuthMode('login')}
              className={`flex-1 py-1.5 text-sm font-medium rounded-md ${authMode === 'login' ? 'bg-white shadow text-indigo-600' : 'text-slate-600'}`}
            >
              Sign In
            </button>
            <button
              onClick={() => setAuthMode('register')}
              className={`flex-1 py-1.5 text-sm font-medium rounded-md ${authMode === 'register' ? 'bg-white shadow text-indigo-600' : 'text-slate-600'}`}
            >
              Register
            </button>
          </div>

          {authError && <p className="text-sm text-red-600 bg-red-50 p-2.5 rounded-lg">{authError}</p>}

          <form onSubmit={handleAuth} className="space-y-4">
            {authMode === 'register' && (
              <>
                <input
                  type="text"
                  placeholder="Full Name"
                  required
                  className="w-full px-3.5 py-2.5 border rounded-lg text-sm"
                  value={authForm.name}
                  onChange={(e) => setAuthForm({ ...authForm, name: e.target.value })}
                />
                <select
                  className="w-full px-3.5 py-2.5 border rounded-lg text-sm bg-white"
                  value={authForm.role}
                  onChange={(e) => setAuthForm({ ...authForm, role: e.target.value })}
                >
                  <option value="CUSTOMER">Customer</option>
                  <option value="AGENT">Support Agent</option>
                </select>
              </>
            )}
            <input
              type="email"
              placeholder="Email Address"
              required
              className="w-full px-3.5 py-2.5 border rounded-lg text-sm"
              value={authForm.email}
              onChange={(e) => setAuthForm({ ...authForm, email: e.target.value })}
            />
            <input
              type="password"
              placeholder="Password"
              required
              className="w-full px-3.5 py-2.5 border rounded-lg text-sm"
              value={authForm.password}
              onChange={(e) => setAuthForm({ ...authForm, password: e.target.value })}
            />
            <button
              type="submit"
              className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg text-sm transition"
            >
              {authMode === 'login' ? 'Sign In' : 'Create Account'}
            </button>
          </form>

          <div className="pt-4 border-t space-y-2">
            <p className="text-xs text-center text-slate-500 font-medium">Instant Hackathon Demo Login</p>
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => demoLogin('customer@demo.com')}
                className="py-2 px-3 bg-slate-100 hover:bg-slate-200 rounded-lg text-xs font-medium text-slate-700"
              >
                Demo Customer
              </button>
              <button
                onClick={() => demoLogin('agent1@demo.com')}
                className="py-2 px-3 bg-indigo-50 hover:bg-indigo-100 rounded-lg text-xs font-medium text-indigo-700"
              >
                Demo Support Agent
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-indigo-600 rounded-lg text-white">
              <Bot className="w-5 h-5" />
            </div>
            <span className="font-bold text-slate-900">SmartSupport AI</span>
            <span className="text-xs px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-700 font-medium">
              {user.role}
            </span>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => setTab('tickets')}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium flex items-center gap-1.5 ${tab === 'tickets' ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-100'}`}
            >
              <TicketIcon className="w-4 h-4" /> Tickets
            </button>
            <button
              onClick={() => setTab('analytics')}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium flex items-center gap-1.5 ${tab === 'analytics' ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:bg-slate-100'}`}
            >
              <BarChart3 className="w-4 h-4" /> Analytics
            </button>
            <span className="hidden sm:inline text-sm text-slate-500">| {user.name}</span>
            <button onClick={logout} className="p-2 text-slate-500 hover:text-red-600 rounded-lg" title="Logout">
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl w-full mx-auto p-4 sm:p-6 flex-1">
        {tab === 'analytics' && analytics ? (
          <div className="space-y-6">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="bg-white p-5 rounded-xl border shadow-sm">
                <p className="text-xs text-slate-500 font-medium uppercase">Total Tickets</p>
                <p className="text-3xl font-bold mt-1">{analytics.totalTickets}</p>
              </div>
              <div className="bg-white p-5 rounded-xl border shadow-sm">
                <p className="text-xs text-slate-500 font-medium uppercase">Customer CSAT Avg</p>
                <p className="text-3xl font-bold mt-1 text-amber-500">{analytics.averageRating || '—'} / 5</p>
              </div>
              <div className="bg-white p-5 rounded-xl border shadow-sm">
                <p className="text-xs text-slate-500 font-medium uppercase">Open / Active</p>
                <p className="text-3xl font-bold mt-1 text-indigo-600">
                  {(analytics.byStatus?.OPEN || 0) + (analytics.byStatus?.IN_PROGRESS || 0)}
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {[
                { title: 'By AI Category', data: analytics.byCategory },
                { title: 'By AI Sentiment', data: analytics.bySentiment },
                { title: 'By AI Priority', data: analytics.byPriority },
              ].map((section) => (
                <div key={section.title} className="bg-white p-5 rounded-xl border shadow-sm space-y-3">
                  <h3 className="font-semibold text-sm text-slate-800">{section.title}</h3>
                  {Object.entries(section.data || {}).length === 0 ? (
                    <p className="text-xs text-slate-400">No tickets yet</p>
                  ) : (
                    Object.entries(section.data).map(([k, v]) => (
                      <div key={k} className="flex items-center justify-between text-sm">
                        <span className="text-slate-600">{k}</span>
                        <span className="font-semibold px-2 py-0.5 bg-slate-100 rounded">{v}</span>
                      </div>
                    ))
                  )}
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            <div className="lg:col-span-5 space-y-5">
              {user.role === 'CUSTOMER' && (
                <form onSubmit={submitComplaint} className="bg-white p-5 rounded-xl border shadow-sm space-y-3">
                  <h2 className="font-semibold text-slate-900 flex items-center gap-2">
                    <PlusCircle className="w-4 h-4 text-indigo-600" /> Submit New Complaint
                  </h2>
                  <input
                    type="text"
                    required
                    placeholder="Short subject (e.g., Charged twice for order #4921)"
                    className="w-full px-3 py-2 border rounded-lg text-sm"
                    value={complaintForm.title}
                    onChange={(e) => setComplaintForm({ ...complaintForm, title: e.target.value })}
                  />
                  <textarea
                    rows={3}
                    required
                    placeholder="Describe your issue in detail..."
                    className="w-full px-3 py-2 border rounded-lg text-sm"
                    value={complaintForm.description}
                    onChange={(e) => setComplaintForm({ ...complaintForm, description: e.target.value })}
                  />
                  <button
                    disabled={submitting}
                    className="w-full py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-60 text-white rounded-lg text-sm font-medium flex items-center justify-center gap-2"
                  >
                    <Sparkles className="w-4 h-4" />
                    {submitting ? 'AI Triage Running...' : 'Submit & Auto-Triage with AI'}
                  </button>
                </form>
              )}

              <div className="bg-white rounded-xl border shadow-sm divide-y">
                <div className="p-4 flex items-center justify-between">
                  <h2 className="font-semibold text-sm text-slate-800">
                    {user.role === 'CUSTOMER' ? 'My Support Tickets' : 'All Support Tickets'} ({tickets.length})
                  </h2>
                </div>
                {tickets.length === 0 ? (
                  <p className="p-6 text-center text-sm text-slate-400">No tickets yet. Submit one to see AI triage in action!</p>
                ) : (
                  tickets.map((t) => (
                    <button
                      key={t.id}
                      onClick={() => openTicket(t)}
                      className={`w-full text-left p-4 hover:bg-slate-50 transition space-y-2 ${selectedTicket?.id === t.id ? 'bg-indigo-50/60' : ''}`}
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-xs font-mono text-slate-500">{t.ticketNumber}</span>
                        <span className={`text-[11px] px-2 py-0.5 rounded-full border font-medium ${badgeColors[t.status]}`}>
                          {t.status}
                        </span>
                      </div>
                      <p className="font-medium text-sm text-slate-900 line-clamp-1">{t.title}</p>
                      <div className="flex flex-wrap gap-1.5">
                        <span className="text-[11px] px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 font-medium">
                          {t.categoryName}
                        </span>
                        <span className={`text-[11px] px-2 py-0.5 rounded border font-medium ${badgeColors[t.priority]}`}>
                          {t.priority}
                        </span>
                        <span className={`text-[11px] px-2 py-0.5 rounded border font-medium ${badgeColors[t.sentiment]}`}>
                          {t.sentiment}
                        </span>
                      </div>
                    </button>
                  ))
                )}
              </div>
            </div>

            <div className="lg:col-span-7">
              {!selectedTicket ? (
                <div className="bg-white rounded-xl border shadow-sm p-12 text-center text-slate-400">
                  Select a ticket from the left to view AI triage, resolution suggestions, and comments.
                </div>
              ) : (
                <div className="bg-white rounded-xl border shadow-sm p-6 space-y-6">
                  <div className="flex flex-wrap items-start justify-between gap-4 border-b pb-4">
                    <div>
                      <span className="text-xs font-mono text-slate-500">{selectedTicket.ticketNumber}</span>
                      <h2 className="text-lg font-bold text-slate-900">{selectedTicket.title}</h2>
                      <p className="text-xs text-slate-500 mt-1">
                        Customer: <strong>{selectedTicket.customerName}</strong> • Assigned to: <strong>{selectedTicket.assignedAgentName}</strong>
                      </p>
                    </div>
                    {user.role !== 'CUSTOMER' && (
                      <div className="flex flex-wrap gap-2">
                        <select
                          value={selectedTicket.status}
                          onChange={(e) => updateStatus(selectedTicket.id, e.target.value)}
                          className="px-2.5 py-1.5 border rounded-lg text-xs font-medium bg-white"
                        >
                          <option value="OPEN">OPEN</option>
                          <option value="IN_PROGRESS">IN_PROGRESS</option>
                          <option value="RESOLVED">RESOLVED</option>
                          <option value="CLOSED">CLOSED</option>
                        </select>
                        <select
                          onChange={(e) => e.target.value && assignAgent(selectedTicket.id, e.target.value)}
                          defaultValue=""
                          className="px-2.5 py-1.5 border rounded-lg text-xs font-medium bg-white"
                        >
                          <option value="" disabled>Reassign Agent...</option>
                          {agents.map((a) => (
                            <option key={a.id} value={a.id}>{a.name}</option>
                          ))}
                        </select>
                      </div>
                    )}
                  </div>

                  <p className="text-sm text-slate-700 whitespace-pre-line">{selectedTicket.description}</p>

                  {user.role !== 'CUSTOMER' && (
                    <div className="bg-indigo-50/70 border border-indigo-100 rounded-xl p-4 space-y-3">
                      <div className="flex items-center gap-2 text-indigo-800 font-semibold text-sm">
                        <Sparkles className="w-4 h-4" /> AI Suggested Resolution (Internal)
                      </div>
                      <p className="text-xs text-slate-700 whitespace-pre-line">{selectedTicket.aiSuggestedResolution}</p>
                      <div className="pt-2 border-t border-indigo-100 flex items-start justify-between gap-3">
                        <div>
                          <p className="text-xs font-semibold text-indigo-800">AI Draft Customer Response</p>
                          <p className="text-xs text-slate-700 mt-1">{selectedTicket.aiCustomerResponse}</p>
                        </div>
                        <button
                          onClick={() => postComment(selectedTicket.aiCustomerResponse)}
                          className="shrink-0 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-medium"
                        >
                          Send AI Reply
                        </button>
                      </div>
                    </div>
                  )}

                  <div className="space-y-3">
                    <h3 className="text-sm font-semibold text-slate-800 flex items-center gap-1.5">
                      <MessageSquare className="w-4 h-4" /> Conversation
                    </h3>
                    <div className="space-y-2 max-h-60 overflow-y-auto">
                      {comments.map((c) => (
                        <div key={c.id} className="p-3 rounded-lg bg-slate-50 border text-sm">
                          <div className="flex justify-between text-xs text-slate-500 mb-1">
                            <span className="font-semibold text-slate-700">{c.authorName} ({c.authorRole})</span>
                          </div>
                          <p className="text-slate-800">{c.message}</p>
                        </div>
                      ))}
                    </div>
                    <div className="flex gap-2">
                      <input
                        type="text"
                        placeholder="Write a reply..."
                        className="flex-1 px-3 py-2 border rounded-lg text-sm"
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                      />
                      <button
                        onClick={() => postComment()}
                        className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-sm font-medium flex items-center gap-1.5"
                      >
                        <Send className="w-3.5 h-3.5" /> Send
                      </button>
                    </div>
                  </div>

                  {user.role === 'CUSTOMER' && (selectedTicket.status === 'RESOLVED' || selectedTicket.status === 'CLOSED') && (
                    <form onSubmit={submitFeedback} className="pt-4 border-t space-y-2">
                      <p className="text-sm font-semibold flex items-center gap-1.5">
                        <Star className="w-4 h-4 text-amber-500" />
                        {selectedTicket.customerFeedbackRating ? `Rated ${selectedTicket.customerFeedbackRating}/5` : 'Rate Support Experience'}
                      </p>
                      <div className="flex gap-2">
                        <select
                          value={feedback.rating}
                          onChange={(e) => setFeedback({ ...feedback, rating: Number(e.target.value) })}
                          className="px-3 py-1.5 border rounded-lg text-sm bg-white"
                        >
                          {[5, 4, 3, 2, 1].map((n) => (
                            <option key={n} value={n}>{n} Stars</option>
                          ))}
                        </select>
                        <input
                          type="text"
                          placeholder="Optional feedback..."
                          className="flex-1 px-3 py-1.5 border rounded-lg text-sm"
                          value={feedback.comment}
                          onChange={(e) => setFeedback({ ...feedback, comment: e.target.value })}
                        />
                        <button className="px-4 py-1.5 bg-amber-500 hover:bg-amber-600 text-white rounded-lg text-sm font-medium">
                          Submit
                        </button>
                      </div>
                    </form>
                  )}
                </div>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
