# TestCraft Dashboard Navigation Structure

## 🏗️ **Layout Diagram**

```
┌─────────────────────────────────────────────────────────────────┐
│                    TestCraft Dashboard                         │
│  [Settings] [Scan Now] [Last Scan: 2025/10/5 10:38:01]        │
├─────────────┬───────────────────────────────────────────────────┤
│             │                                                   │
│  📊 Dashboard│  ┌─────────────────────────────────────────────┐  │
│  📁 Repos    │  │              MAIN CONTENT                   │  │
│  👥 Teams    │  │                                             │  │
│  📈 Analytics│  │  ┌─────────────────────────────────────┐   │  │
│  ⚙️ Settings │  │  │         Current View               │   │  │
│             │  │  │                                     │   │  │
│  [Collapse] │  │  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ │   │  │
│             │  │  │  │  Card 1 │ │  Card 2 │ │  Card 3 │ │   │  │
│             │  │  │  └─────────┘ └─────────┘ └─────────┘ │   │  │
│             │  │  │                                     │   │  │
│             │  │  │  ┌─────────────────────────────────┐ │   │  │
│             │  │  │  │        Data Table/Chart        │ │   │  │
│             │  │  │  └─────────────────────────────────┘ │   │  │
│             │  │  └─────────────────────────────────────┘   │  │
│             │  └─────────────────────────────────────────────┘  │
└─────────────┴───────────────────────────────────────────────────┘
```

## 📊 **Navigation Flow**

### **Dashboard View** (Current Overview)
```
📊 Dashboard
├── Summary Stats (4 cards)
├── Team Performance Chart
├── Coverage Chart
├── Top 5 Repositories Table
└── Recent Scan History
```

### **Repositories View** (New - Full Repository Management)
```
📁 Repositories
├── Repository List (All Repositories)
│   ├── Search & Filter Bar
│   ├── Sortable Table
│   │   ├── Repository Name
│   │   ├── Team
│   │   ├── Test Classes
│   │   ├── Test Methods
│   │   ├── Coverage %
│   │   ├── Last Scan
│   │   └── Actions
│   └── Pagination
├── Repository Detail (Modal/Page)
│   ├── Repository Info
│   ├── Test Metrics
│   ├── Coverage Analysis
│   └── Class Level View →
│       ├── Test Classes List
│       ├── Class Metrics
│       └── Method Details
└── Bulk Operations
    ├── Multi-select
    ├── Bulk Scan
    └── Bulk Export
```

### **Teams View** (New - Team-Focused Analysis)
```
👥 Teams
├── Team Overview
│   ├── Team Performance Cards
│   ├── Team Comparison Chart
│   └── Team Statistics
├── Team Detail
│   ├── Team Info
│   ├── Assigned Repositories
│   ├── Team Metrics
│   └── Performance History
└── Team Comparison
    ├── Side-by-side Metrics
    ├── Trend Comparison
    └── Performance Analysis
```

### **Analytics View** (Enhanced Reports)
```
📈 Analytics
├── Trends
│   ├── Coverage Evolution
│   ├── Repository Growth
│   ├── Scan Frequency
│   └── Performance Metrics
├── Reports
│   ├── Generated Reports
│   ├── Custom Reports
│   └── Scheduled Reports
└── Export
    ├── CSV Export
    ├── PDF Reports
    └── API Access
```

### **Settings View** (Configuration)
```
⚙️ Settings
├── Scan Configuration
├── System Settings
├── User Preferences
└── Integrations
```

## 🔄 **Navigation States**

### **URL Routing**
```
/                          → Dashboard View
/repositories              → Repository List
/repositories/:id          → Repository Detail
/repositories/:id/classes  → Class Level View
/teams                     → Team Overview
/teams/:id                 → Team Detail
/analytics                 → Analytics Overview
/analytics/trends          → Trends View
/analytics/reports         → Reports View
/settings                  → Settings View
```

### **Breadcrumb Navigation**
```
Dashboard
Dashboard > Repositories
Dashboard > Repositories > testcraft-dashboard.git
Dashboard > Repositories > testcraft-dashboard.git > TestClasses
Dashboard > Teams
Dashboard > Teams > team_a
Dashboard > Analytics > Trends
Dashboard > Settings
```

## 📱 **Responsive Behavior**

### **Desktop (≥1024px)**
- Full sidebar visible
- Main content area optimized
- Hover states and tooltips

### **Tablet (768px - 1023px)**
- Collapsible sidebar
- Touch-friendly navigation
- Optimized table layouts

### **Mobile (<768px)**
- Hidden sidebar (hamburger menu)
- Stacked card layouts
- Touch-optimized interactions

## 🎨 **Visual Design**

### **Sidebar**
- **Width**: 240px (expanded), 64px (collapsed)
- **Background**: White with subtle border
- **Active State**: Blue highlight with icon + text
- **Hover State**: Light gray background
- **Icons**: Lucide React icons

### **Content Area**
- **Background**: Light gray (#f8fafc)
- **Padding**: 24px
- **Max Width**: 1200px (centered)
- **Responsive**: Adapts to sidebar state

### **Header**
- **Height**: 64px
- **Background**: White
- **Content**: Logo, current view title, actions
- **Border**: Bottom border for separation

## 🚀 **Implementation Priority**

### **Phase 1: Foundation** (Week 1)
1. ✅ Create sidebar navigation component
2. ✅ Implement basic routing
3. ✅ Refactor current dashboard into DashboardView
4. ✅ Create main layout wrapper

### **Phase 2: Repository Management** (Week 2)
1. ✅ Create RepositoriesView
2. ✅ Implement repository list with filtering
3. ✅ Add repository detail modal/page
4. ✅ Create class-level drill-down

### **Phase 3: Team Management** (Week 3)
1. ✅ Create TeamsView
2. ✅ Implement team overview and detail
3. ✅ Add team comparison features
4. ✅ Team-specific analytics

### **Phase 4: Analytics Enhancement** (Week 4)
1. ✅ Create AnalyticsView
2. ✅ Implement trends and reports
3. ✅ Add export capabilities
4. ✅ Advanced filtering

### **Phase 5: Polish** (Week 5)
1. ✅ Performance optimization
2. ✅ Mobile responsiveness
3. ✅ Accessibility improvements
4. ✅ User testing and refinements

## 💡 **Key Benefits**

### **For Users**
- **Better Organization**: Clear separation of different data views
- **Faster Navigation**: Quick access to specific information
- **Deeper Analysis**: Class-level and method-level details
- **Scalable Interface**: Room for future features

### **For Developers**
- **Modular Architecture**: Easier to maintain and extend
- **Performance**: Lazy loading and caching
- **Testing**: Isolated component testing
- **Code Reuse**: Shared components across views

### **For Business**
- **User Adoption**: Better user experience
- **Feature Growth**: Scalable foundation
- **Data Insights**: Deeper analysis capabilities
- **Competitive Advantage**: Professional interface
