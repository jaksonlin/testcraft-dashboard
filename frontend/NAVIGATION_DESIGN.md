# TestCraft Dashboard Navigation Design

## 🎯 **Overview**
Transform the current single-page dashboard into a multi-view application with sidebar navigation to better organize and scale the growing feature set.

## 📊 **Current Issues**
- Dashboard is becoming heavy with too much information
- Limited repository visibility (only top repositories shown)
- No dedicated team management view
- Missing detailed class-level analysis
- Poor scalability for future features

## 🗂️ **Proposed Navigation Structure**

### **Main Navigation Items**

#### 1. **📊 Dashboard** (Current Overview)
- **Purpose**: High-level overview and key metrics
- **Content**: 
  - Summary statistics (current stats cards)
  - Team performance chart
  - Test coverage chart
  - Top 5 repositories table
  - Recent scan history timeline
- **Access**: Default landing page

#### 2. **📁 Repositories** (New Dedicated View)
- **Purpose**: Complete repository management and analysis
- **Content**:
  - Full repository list (all repositories, not just top 5)
  - Repository filtering and search
  - Sortable columns (name, team, coverage, last scan, etc.)
  - Repository detail modals
  - Bulk operations (scan multiple repos)
  - Repository statistics and trends
- **Sub-views**:
  - **Repository List**: Table view with all repositories
  - **Repository Detail**: Individual repository analysis
  - **Class Level View**: Drill-down to individual test classes

#### 3. **👥 Teams** (New Dedicated View)
- **Purpose**: Team-focused analysis and management
- **Content**:
  - Team performance metrics
  - Team comparison charts
  - Team repository assignments
  - Team coverage trends
  - Team-specific reports
- **Sub-views**:
  - **Team Overview**: All teams summary
  - **Team Detail**: Individual team deep-dive
  - **Team Comparison**: Side-by-side team metrics

#### 4. **📈 Analytics** (Enhanced Reports)
- **Purpose**: Advanced analytics and reporting
- **Content**:
  - Historical trends and patterns
  - Coverage evolution over time
  - Scan frequency analysis
  - Performance metrics
  - Export capabilities
- **Sub-views**:
  - **Trends**: Time-series analysis
  - **Reports**: Generated reports
  - **Exports**: Data export tools

#### 5. **⚙️ Settings** (Configuration)
- **Purpose**: System configuration and management
- **Content**:
  - Scan configuration (current modal content)
  - System settings
  - User preferences
  - Integration settings

## 🏗️ **Technical Architecture**

### **Layout Structure**
```
┌─────────────────────────────────────────────────────────┐
│ Header (TestCraft Dashboard)                            │
├─────────────┬───────────────────────────────────────────┤
│             │                                           │
│  Sidebar    │              Main Content Area            │
│             │                                           │
│  📊 Dashboard│  [Dynamic Content Based on Selection]    │
│  📁 Repos    │                                           │
│  👥 Teams    │                                           │
│  📈 Analytics│                                           │
│  ⚙️ Settings │                                           │
│             │                                           │
└─────────────┴───────────────────────────────────────────┘
```

### **Component Hierarchy**
```
App
├── SidebarNavigation
├── MainLayout
│   ├── Header
│   └── ContentArea
│       ├── DashboardView (current dashboard content)
│       ├── RepositoriesView
│       │   ├── RepositoryList
│       │   ├── RepositoryDetail
│       │   └── ClassLevelView
│       ├── TeamsView
│       │   ├── TeamOverview
│       │   ├── TeamDetail
│       │   └── TeamComparison
│       ├── AnalyticsView
│       │   ├── TrendsView
│       │   ├── ReportsView
│       │   └── ExportView
│       └── SettingsView
```

## 📱 **Detailed View Specifications**

### **Repository View**
- **Repository List Table**:
  - All repositories (paginated if needed)
  - Columns: Name, Team, Test Classes, Test Methods, Coverage %, Last Scan, Actions
  - Filtering: By team, coverage range, last scan date
  - Sorting: All columns sortable
  - Actions: View detail, trigger scan, export

- **Class Level View**:
  - Drill-down from repository to individual test classes
  - Class-level metrics: method count, annotation count, coverage
  - Method-level details within each class
  - Navigation breadcrumb: Dashboard > Repositories > [Repo Name] > [Class Name]

### **Team View**
- **Team Overview**:
  - Team performance cards
  - Team comparison charts
  - Team repository assignments
  - Team coverage trends

- **Team Detail**:
  - Individual team deep-dive
  - Team member repositories
  - Team-specific metrics and trends
  - Team performance history

### **Analytics View**
- **Trends**:
  - Coverage evolution over time
  - Repository growth trends
  - Scan frequency patterns
  - Performance metrics

- **Reports**:
  - Automated report generation
  - Custom report builder
  - Scheduled reports
  - Report templates

## 🎨 **UI/UX Considerations**

### **Sidebar Design**
- **Collapsible**: Can be minimized to save space
- **Icons + Labels**: Clear visual hierarchy
- **Active State**: Highlight current selection
- **Responsive**: Mobile-friendly collapse behavior

### **Navigation Flow**
- **Breadcrumbs**: Clear navigation path
- **Back Navigation**: Easy return to parent views
- **Deep Linking**: URL routing for each view
- **State Persistence**: Remember user selections

### **Data Loading**
- **Lazy Loading**: Load data only when view is accessed
- **Caching**: Cache frequently accessed data
- **Progressive Loading**: Show skeleton/loading states
- **Error Handling**: Graceful error states for each view

## 🚀 **Implementation Phases**

### **Phase 1: Core Navigation**
1. Create sidebar navigation component
2. Implement routing system
3. Refactor main dashboard into DashboardView
4. Create basic layout structure

### **Phase 2: Repository Management**
1. Create RepositoriesView with full repository list
2. Implement repository filtering and search
3. Add repository detail drill-down
4. Create class-level view

### **Phase 3: Team Management**
1. Create TeamsView with team overview
2. Implement team detail views
3. Add team comparison features
4. Team-specific analytics

### **Phase 4: Advanced Analytics**
1. Create AnalyticsView with trends
2. Implement report generation
3. Add export capabilities
4. Advanced filtering and search

### **Phase 5: Polish & Optimization**
1. Performance optimization
2. Mobile responsiveness
3. Accessibility improvements
4. User experience refinements

## 📋 **Benefits**

### **User Experience**
- **Better Organization**: Clear separation of concerns
- **Scalability**: Easy to add new features
- **Navigation**: Intuitive navigation between views
- **Focus**: Each view has a specific purpose

### **Technical Benefits**
- **Maintainability**: Smaller, focused components
- **Performance**: Lazy loading and caching
- **Testability**: Isolated component testing
- **Extensibility**: Easy to add new views

### **Business Benefits**
- **User Adoption**: Better user experience
- **Feature Growth**: Scalable architecture
- **Data Insights**: Deeper analysis capabilities
- **Productivity**: Faster access to specific information

## 🎯 **Next Steps**
1. Review and approve this design
2. Start with Phase 1: Core Navigation implementation
3. Create component wireframes and mockups
4. Implement routing and basic layout structure
