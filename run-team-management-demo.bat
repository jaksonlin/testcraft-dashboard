@echo off
echo ========================================
echo Team Management Demo
echo ========================================
echo.

echo 1. Generating CSV template...
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.team.TeamManager generateCSVTemplate team-assignments.csv

echo.
echo 2. Validating current team assignments...
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.team.TeamManager validateTeamAssignments

echo.
echo 3. Instructions:
echo    - Edit team-assignments.csv with your repository URLs and team names
echo    - Run: java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.team.TeamManager loadTeamAssignmentsFromCSV team-assignments.csv
echo    - Then generate your weekly report to see team-based organization
echo.
echo ========================================
pause
