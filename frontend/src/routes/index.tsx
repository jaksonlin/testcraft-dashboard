import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import MainLayout from '../components/layout/MainLayout';
import DashboardView from '../views/DashboardView';
import RepositoriesView from '../views/RepositoriesView';
import RepositoryDetailView from '../views/RepositoryDetailView';
import ClassLevelView from '../views/ClassLevelView';
import TeamsView from '../views/TeamsView';
import AnalyticsView from '../views/AnalyticsView';
import SettingsView from '../views/SettingsView';
import TestMethodGroupedView from '../views/TestMethodGroupedView';
import TestMethodsView from '../views/TestMethodsView';
import TestMethodHierarchicalView from '../views/TestMethodHierarchicalView';
import { TestCasesView } from '../views/TestCasesView';
import LoginView from '../views/LoginView';
import ChangePasswordView from '../views/ChangePasswordView';
import { ProtectedRoute } from './ProtectedRoute';

const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginView />,
  },
  {
    path: '/change-password',
    element: (
      <ProtectedRoute allowDefaultPassword>
        <ChangePasswordView />
      </ProtectedRoute>
    ),
  },
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: (
          <ProtectedRoute>
            <DashboardView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'repositories',
        element: (
          <ProtectedRoute>
            <RepositoriesView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'repositories/:id',
        element: (
          <ProtectedRoute>
            <RepositoryDetailView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'repositories/:id/classes',
        element: (
          <ProtectedRoute>
            <ClassLevelView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'teams',
        element: (
          <ProtectedRoute>
            <TeamsView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'analytics',
        element: (
          <ProtectedRoute>
            <AnalyticsView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'testcases',
        element: (
          <ProtectedRoute>
            <TestCasesView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'settings',
        element: (
          <ProtectedRoute requireAdmin>
            <SettingsView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'test-methods',
        element: (
          <ProtectedRoute>
            <TestMethodsView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'test-methods-grouped',
        element: (
          <ProtectedRoute>
            <TestMethodGroupedView />
          </ProtectedRoute>
        ),
      },
      {
        path: 'test-methods-hierarchy',
        element: (
          <ProtectedRoute>
            <TestMethodHierarchicalView />
          </ProtectedRoute>
        ),
      },
    ],
  },
]);

const AppRouter: React.FC = () => {
  return <RouterProvider router={router} />;
};

export default AppRouter;
