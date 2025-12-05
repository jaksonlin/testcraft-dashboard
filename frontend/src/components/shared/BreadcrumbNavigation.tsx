import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';

const BreadcrumbNavigation: React.FC = () => {
  const location = useLocation();

  const getBreadcrumbs = () => {
    const pathSegments = location.pathname.split('/').filter(segment => segment !== '');
    const breadcrumbs: { name: string; path: string; icon: React.ElementType | null }[] = [
      { name: 'Dashboard', path: '/', icon: Home }
    ];

    let currentPath = '';
    pathSegments.forEach((segment) => {
      currentPath += `/${segment}`;

      // Map segments to readable names
      let name = segment;
      if (segment === 'repositories') {
        name = 'Repositories';
      } else if (segment === 'teams') {
        name = 'Teams';
      } else if (segment === 'analytics') {
        name = 'Analytics';
      } else if (segment === 'settings') {
        name = 'Settings';
      } else if (segment === 'classes') {
        name = 'Class Analysis';
      } else if (!isNaN(Number(segment))) {
        // This is likely an ID, try to get repository name
        name = `Repository ${segment}`;
      }

      breadcrumbs.push({
        name,
        path: currentPath,
        icon: null
      });
    });

    return breadcrumbs;
  };

  const breadcrumbs = getBreadcrumbs();

  if (breadcrumbs.length <= 1) {
    return null; // Don't show breadcrumbs on dashboard
  }

  return (
    <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
      {breadcrumbs.map((breadcrumb, index) => (
        <React.Fragment key={breadcrumb.path}>
          {index > 0 && <ChevronRight className="h-4 w-4 text-gray-400" />}
          {index === breadcrumbs.length - 1 ? (
            <span className="font-medium text-gray-900">{breadcrumb.name}</span>
          ) : (
            <Link
              to={breadcrumb.path}
              className="flex items-center hover:text-gray-900 transition-colors"
            >
              {breadcrumb.icon && <breadcrumb.icon className="h-4 w-4 mr-1" />}
              {breadcrumb.name}
            </Link>
          )}
        </React.Fragment>
      ))}
    </nav>
  );
};

export default BreadcrumbNavigation;
