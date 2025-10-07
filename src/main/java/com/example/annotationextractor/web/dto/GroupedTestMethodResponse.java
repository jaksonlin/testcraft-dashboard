package com.example.annotationextractor.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for grouped test method data
 * Provides hierarchical structure: Teams -> Classes -> Methods
 */
public class GroupedTestMethodResponse {

    private final List<TeamGroupDto> teams;
    private final SummaryDto summary;

    public GroupedTestMethodResponse(List<TeamGroupDto> teams, SummaryDto summary) {
        this.teams = teams;
        this.summary = summary;
    }

    @JsonProperty("teams")
    public List<TeamGroupDto> getTeams() {
        return teams;
    }

    @JsonProperty("summary")
    public SummaryDto getSummary() {
        return summary;
    }

    /**
     * Team-level grouping data
     */
    public static class TeamGroupDto {
        private final String teamName;
        private final String teamCode;
        private final List<ClassGroupDto> classes;
        private final TeamSummaryDto summary;

        public TeamGroupDto(String teamName, String teamCode, List<ClassGroupDto> classes, TeamSummaryDto summary) {
            this.teamName = teamName;
            this.teamCode = teamCode;
            this.classes = classes;
            this.summary = summary;
        }

        @JsonProperty("teamName")
        public String getTeamName() {
            return teamName;
        }

        @JsonProperty("teamCode")
        public String getTeamCode() {
            return teamCode;
        }

        @JsonProperty("classes")
        public List<ClassGroupDto> getClasses() {
            return classes;
        }

        @JsonProperty("summary")
        public TeamSummaryDto getSummary() {
            return summary;
        }
    }

    /**
     * Class-level grouping data
     */
    public static class ClassGroupDto {
        private final String className;
        private final String packageName;
        private final String repository;
        private final List<TestMethodDetailDto> methods;
        private final ClassSummaryDto summary;

        public ClassGroupDto(String className, String packageName, String repository, 
                           List<TestMethodDetailDto> methods, ClassSummaryDto summary) {
            this.className = className;
            this.packageName = packageName;
            this.repository = repository;
            this.methods = methods;
            this.summary = summary;
        }

        @JsonProperty("className")
        public String getClassName() {
            return className;
        }

        @JsonProperty("packageName")
        public String getPackageName() {
            return packageName;
        }

        @JsonProperty("repository")
        public String getRepository() {
            return repository;
        }

        @JsonProperty("methods")
        public List<TestMethodDetailDto> getMethods() {
            return methods;
        }

        @JsonProperty("summary")
        public ClassSummaryDto getSummary() {
            return summary;
        }
    }

    /**
     * Overall summary statistics
     */
    public static class SummaryDto {
        private final int totalTeams;
        private final int totalClasses;
        private final int totalMethods;
        private final int totalAnnotatedMethods;
        private final double overallCoverageRate;

        public SummaryDto(int totalTeams, int totalClasses, int totalMethods, 
                         int totalAnnotatedMethods, double overallCoverageRate) {
            this.totalTeams = totalTeams;
            this.totalClasses = totalClasses;
            this.totalMethods = totalMethods;
            this.totalAnnotatedMethods = totalAnnotatedMethods;
            this.overallCoverageRate = overallCoverageRate;
        }

        @JsonProperty("totalTeams")
        public int getTotalTeams() {
            return totalTeams;
        }

        @JsonProperty("totalClasses")
        public int getTotalClasses() {
            return totalClasses;
        }

        @JsonProperty("totalMethods")
        public int getTotalMethods() {
            return totalMethods;
        }

        @JsonProperty("totalAnnotatedMethods")
        public int getTotalAnnotatedMethods() {
            return totalAnnotatedMethods;
        }

        @JsonProperty("overallCoverageRate")
        public double getOverallCoverageRate() {
            return overallCoverageRate;
        }
    }

    /**
     * Team-level summary statistics
     */
    public static class TeamSummaryDto {
        private final int totalClasses;
        private final int totalMethods;
        private final int annotatedMethods;
        private final double coverageRate;

        public TeamSummaryDto(int totalClasses, int totalMethods, int annotatedMethods, double coverageRate) {
            this.totalClasses = totalClasses;
            this.totalMethods = totalMethods;
            this.annotatedMethods = annotatedMethods;
            this.coverageRate = coverageRate;
        }

        @JsonProperty("totalClasses")
        public int getTotalClasses() {
            return totalClasses;
        }

        @JsonProperty("totalMethods")
        public int getTotalMethods() {
            return totalMethods;
        }

        @JsonProperty("annotatedMethods")
        public int getAnnotatedMethods() {
            return annotatedMethods;
        }

        @JsonProperty("coverageRate")
        public double getCoverageRate() {
            return coverageRate;
        }
    }

    /**
     * Class-level summary statistics
     */
    public static class ClassSummaryDto {
        private final int totalMethods;
        private final int annotatedMethods;
        private final double coverageRate;

        public ClassSummaryDto(int totalMethods, int annotatedMethods, double coverageRate) {
            this.totalMethods = totalMethods;
            this.annotatedMethods = annotatedMethods;
            this.coverageRate = coverageRate;
        }

        @JsonProperty("totalMethods")
        public int getTotalMethods() {
            return totalMethods;
        }

        @JsonProperty("annotatedMethods")
        public int getAnnotatedMethods() {
            return annotatedMethods;
        }

        @JsonProperty("coverageRate")
        public double getCoverageRate() {
            return coverageRate;
        }
    }
}
