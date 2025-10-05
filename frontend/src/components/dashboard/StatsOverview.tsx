import React from 'react';
import { GitBranch, Users, TestTube, CheckCircle } from 'lucide-react';
import StatCard from '../shared/StatCard';
import { type DashboardOverview } from '../../lib/api';

interface StatsOverviewProps {
  overview: DashboardOverview | null;
}

const StatsOverview: React.FC<StatsOverviewProps> = ({ overview }) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <StatCard
        title="Total Repositories"
        value={overview?.totalRepositories || 0}
        icon={<GitBranch className="h-6 w-6 text-white" />}
        color="bg-blue-500"
      />
      <StatCard
        title="Teams"
        value={overview?.totalTeams || 0}
        icon={<Users className="h-6 w-6 text-white" />}
        color="bg-green-500"
      />
      <StatCard
        title="Test Methods"
        value={overview?.totalTestMethods || 0}
        icon={<TestTube className="h-6 w-6 text-white" />}
        color="bg-purple-custom"
      />
      <StatCard
        title="Coverage Rate"
        value={`${overview?.overallCoverageRate.toFixed(1) || 0}%`}
        icon={<CheckCircle className="h-6 w-6 text-white" />}
        color="bg-orange-custom"
      />
    </div>
  );
};

export default StatsOverview;
