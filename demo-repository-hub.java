import com.example.annotationextractor.*;

/**
 * Demo script for Repository Hub Scanner
 * Run with: java -cp target/classes demo-repository-hub.java
 */
public class demo-repository-hub {
    
    public static void main(String[] args) {
        System.out.println("Repository Hub Scanner Demo");
        System.out.println("===========================");
        
        try {
            // Create a sample repository list
            String repoListFile = "demo-repos.txt";
            RepositoryListProcessor.createSampleRepositoryList(repoListFile);
            System.out.println("✓ Created sample repository list: " + repoListFile);
            
            // Show file statistics
            String stats = RepositoryListProcessor.getFileStatistics(repoListFile);
            System.out.println("\nFile Statistics:");
            System.out.println(stats);
            
            // Read the repository URLs
            System.out.println("\nRepository URLs:");
            var urls = RepositoryListProcessor.readRepositoryUrls(repoListFile);
            for (String url : urls) {
                System.out.println("  " + url);
            }
            
            // Create repository hub scanner
            System.out.println("\n✓ Repository Hub Scanner ready!");
            System.out.println("To run a full scan, use:");
            System.out.println("  java -jar target/annotation-extractor-1.0.0.jar ./repositories " + repoListFile);
            
        } catch (Exception e) {
            System.err.println("Demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
