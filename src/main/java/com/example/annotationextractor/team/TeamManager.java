package com.example.annotationextractor.team;

import com.example.annotationextractor.database.DataPersistenceService;
import com.example.annotationextractor.database.DatabaseConfig;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Utility class for managing team assignments and repository mappings
 */
public class TeamManager {
    

   
    
    
    
    
    /**
     * Generate sample CSV template for team assignments
     */
    public static void generateCSVTemplate(String outputPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("git_url,team_name,team_code");
            writer.println("https://github.com/company/frontend-app,Frontend Team,FE");
            writer.println("https://github.com/company/backend-api,Backend Team,BE");
            writer.println("https://github.com/company/mobile-app,Mobile Team,MOB");
            writer.println("https://github.com/company/data-pipeline,Data Team,DATA");
            writer.println("# Add your repository URLs and team assignments above");
            writer.println("# Remove the example lines when you're ready to use");
            writer.println("# team_code should match your DevOps system team codes");
        }
        
        System.out.println("✅ CSV template generated: " + outputPath);
    }
    
    
}
