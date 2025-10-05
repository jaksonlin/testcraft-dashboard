# Reporting & Scan Details Enhancement Plan

## 🎯 **Current State Analysis**

### **What We Have:**
- ✅ Basic dashboard with overview metrics
- ✅ Team performance charts
- ✅ Top repositories table
- ✅ Scan configuration management
- ✅ Manual scan triggering

### **What's Missing for Comprehensive Reporting:**
- ❌ Detailed scan results drill-down
- ❌ Historical data and trends
- ❌ File-level scan details
- ❌ Export capabilities
- ❌ Advanced filtering

## 🚀 **Recommended Implementation Priority**

### **Phase 1: Data Drill-Down (High Priority)**
1. **Repository Detail Modal/Page**
   - File-level scan results
   - Test method annotations
   - Coverage breakdown
   - Recent scan history for that repo

2. **Enhanced Data Tables**
   - Clickable rows for drill-down
   - Sortable columns
   - Basic filtering

### **Phase 2: Historical Data (Medium Priority)**
1. **Scan History Timeline**
   - List of all past scans
   - Scan results comparison
   - Success/failure tracking

2. **Trend Charts**
   - Coverage trends over time
   - Repository growth tracking
   - Team performance trends

### **Phase 3: Advanced Reporting (Lower Priority)**
1. **Export Functionality**
   - Excel export with detailed data
   - PDF report generation
   - Custom report templates

2. **Advanced Filtering**
   - Date range filters
   - Multi-criteria filtering
   - Saved filter presets

## 📊 **Backend API Requirements**

### **New Endpoints Needed:**
```typescript
// Repository Details
GET /api/repositories/{id}/details
GET /api/repositories/{id}/files
GET /api/repositories/{id}/history

// Scan History
GET /api/scan/history?limit=50&offset=0
GET /api/scan/{scanId}/details
GET /api/scan/{scanId}/results

// Export
GET /api/reports/excel?repositoryId={id}&dateFrom={date}&dateTo={date}
GET /api/reports/pdf?repositoryId={id}

// Trends & Analytics
GET /api/analytics/trends?period=30d
GET /api/analytics/coverage-history?repositoryId={id}
```

### **Database Schema Enhancements:**
```sql
-- Scan Sessions Table (already exists, may need enhancements)
ALTER TABLE scan_sessions ADD COLUMN files_scanned INTEGER;
ALTER TABLE scan_sessions ADD COLUMN test_methods_found INTEGER;
ALTER TABLE scan_sessions ADD COLUMN annotated_methods_found INTEGER;

-- File-level Results Table
CREATE TABLE scan_file_results (
    id BIGINT PRIMARY KEY,
    scan_session_id BIGINT,
    repository_id BIGINT,
    file_path VARCHAR(500),
    test_methods_count INTEGER,
    annotated_methods_count INTEGER,
    coverage_rate DECIMAL(5,2),
    scan_timestamp TIMESTAMP
);

-- Historical Metrics Table
CREATE TABLE historical_metrics (
    id BIGINT PRIMARY KEY,
    repository_id BIGINT,
    scan_date DATE,
    total_test_methods INTEGER,
    annotated_methods INTEGER,
    coverage_rate DECIMAL(5,2),
    files_count INTEGER
);
```

## 🎨 **Frontend Component Structure**

### **New Components Needed:**
```
components/
├── reports/
│   ├── RepositoryDetailModal.tsx      # Repository drill-down
│   ├── ScanHistoryTimeline.tsx        # Historical scans
│   ├── TrendCharts.tsx               # Time series charts
│   ├── ExportOptions.tsx             # Export controls
│   └── ReportBuilder.tsx             # Custom reports
├── filters/
│   ├── DateRangeFilter.tsx           # Date filtering
│   ├── MultiSelectFilter.tsx         # Multi-criteria filtering
│   └── FilterPresets.tsx             # Saved filters
└── tables/
    ├── EnhancedRepositoriesTable.tsx  # With drill-down
    ├── ScanHistoryTable.tsx          # Historical data
    └── FileResultsTable.tsx          # File-level results
```

## 📋 **Detailed Implementation Plan**

### **1. Repository Detail Modal (Week 1-2)**
- **Purpose**: Show detailed scan results for a specific repository
- **Features**:
  - File-level breakdown
  - Test method details
  - Coverage visualization
  - Recent scan comparison
- **API**: `GET /api/repositories/{id}/details`

### **2. Scan History Timeline (Week 3)**
- **Purpose**: Show historical scan results and trends
- **Features**:
  - Timeline view of all scans
  - Success/failure indicators
  - Quick comparison with previous scans
- **API**: `GET /api/scan/history`

### **3. Enhanced Data Tables (Week 4)**
- **Purpose**: Make existing tables more interactive
- **Features**:
  - Clickable rows for drill-down
  - Sortable columns
  - Basic filtering
  - Loading states

### **4. Export Functionality (Week 5-6)**
- **Purpose**: Generate detailed Excel/PDF reports
- **Features**:
  - Excel export with multiple sheets
  - PDF reports with charts
  - Custom date ranges
  - Repository-specific exports

## 🎯 **User Stories**

### **As a Team Lead, I want to:**
- Click on any repository to see detailed scan results
- View historical trends to track team progress
- Export comprehensive reports for stakeholders
- Compare current results with previous scans

### **As a Developer, I want to:**
- See which specific files need more test coverage
- View my team's progress over time
- Understand what test annotations are missing
- Access detailed reports for my repositories

### **As a Manager, I want to:**
- Generate executive summaries in PDF format
- Track organization-wide test coverage trends
- Identify teams/repositories that need attention
- Export data for further analysis in Excel

## 🚀 **Quick Wins (Can Implement First)**

1. **Make Repository Table Clickable**
   - Add onClick handlers to table rows
   - Show basic repository details in a modal

2. **Add Scan History Section**
   - Simple list of recent scans
   - Basic success/failure indicators

3. **Basic Export Button**
   - Export current dashboard data to CSV
   - Simple but immediately useful

## 📊 **Success Metrics**

- **User Engagement**: Time spent drilling down into details
- **Report Usage**: Number of exports generated
- **Data Accuracy**: User feedback on report completeness
- **Performance**: Page load times for detailed views

This plan provides a comprehensive roadmap for transforming your dashboard from a basic overview into a powerful reporting and analysis tool! 🌟
