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

const router = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: <DashboardView />
      },
      {
        path: 'repositories',
        element: <RepositoriesView />
      },
      {
        path: 'repositories/:id',
        element: <RepositoryDetailView />
      },
      {
        path: 'repositories/:id/classes',
        element: <ClassLevelView />
      },
      {
        path: 'teams',
        element: <TeamsView />
      },
      {
        path: 'analytics',
        element: <AnalyticsView />
      },
              {
                path: 'settings',
                element: <SettingsView />
              },
              {
                path: 'test-methods',
                element: <TestMethodGroupedView />
              }
    ]
  }
]);

const AppRouter: React.FC = () => {
  return <RouterProvider router={router} />;
};

export default AppRouter;
