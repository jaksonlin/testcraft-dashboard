package com.example.annotationextractor.runner;

import java.nio.file.Paths;
import java.util.List;
import com.example.annotationextractor.util.GitRepositoryManager;

/**
 * Main command-line interface for Repository Hub Scanner
 * Usage: java RepositoryHubRunner <repository_hub_path> <repository_list_file>
 * [username] [password] [ssh_key_path] [--db-host host] [--db-port port]
 * [--db-name database] [--db-user dbuser] [--db-pass dbpass]
 */
public class RepositoryHubRunner {

    public static void main(String[] args) {
        System.out.println("Repository Hub Scanner");
        System.out.println("======================");

        // Check for help or SSH guidance options
        if (args.length > 0) {
            if (args[0].equals("--help") || args[0].equals("-h")) {
                printUsage();
                return;
            } else if (args[0].equals("--ssh-help")) {
                GitRepositoryManager.printSshKeyGuidance();
                return;
            }
        }

        if (args.length < 1) {
            printUsage();
            return;
        }

        String repositoryHubPath = args[0];
        String username = null;
        String password = null;
        String sshKeyPath = null;
        boolean tempCloneMode = false;

        // Database connection parameters
        String dbHost = null;
        String dbPort = null;
        String dbName = null;
        String dbUser = null;
        String dbPass = null;

        // Shadow database connection parameters (optional)
        String sHost = null;
        String sPort = null;
        String sName = null;
        String sUser = null;
        String sPass = null;

        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--db-host") && i + 1 < args.length) {
                dbHost = args[++i];
            } else if (args[i].equals("--db-port") && i + 1 < args.length) {
                dbPort = args[++i];
            } else if (args[i].equals("--db-name") && i + 1 < args.length) {
                dbName = args[++i];
            } else if (args[i].equals("--db-user") && i + 1 < args.length) {
                dbUser = args[++i];
            } else if (args[i].equals("--db-pass") && i + 1 < args.length) {
                dbPass = args[++i];
            } else if (args[i].equals("--temp-clone")) {
                tempCloneMode = true;
            } else if (args[i].equals("--shadow-db-host") && i + 1 < args.length) {
                sHost = args[++i];
            } else if (args[i].equals("--shadow-db-port") && i + 1 < args.length) {
                sPort = args[++i];
            } else if (args[i].equals("--shadow-db-name") && i + 1 < args.length) {
                sName = args[++i];
            } else if (args[i].equals("--shadow-db-user") && i + 1 < args.length) {
                sUser = args[++i];
            } else if (args[i].equals("--shadow-db-pass") && i + 1 < args.length) {
                sPass = args[++i];
            } else if (sshKeyPath == null && !args[i].startsWith("--")) {
                // First non-flag argument is username, second is password, third is ssh key
                if (username == null) {
                    username = args[i];
                } else if (password == null) {
                    password = args[i];
                } else if (sshKeyPath == null) {
                    sshKeyPath = args[i];
                }
            }
        }

        try {
            // Validate paths
            if (!validatePaths(repositoryHubPath)) {
                return;
            }

            // Initialize database connection if CLI parameters are provided
            if (dbHost != null || dbPort != null || dbName != null || dbUser != null || dbPass != null) {
                // Use CLI parameters, fall back to properties file for missing values
                String finalDbHost = dbHost != null ? dbHost : "localhost";
                String finalDbPort = dbPort != null ? dbPort : "5432";
                String finalDbName = dbName != null ? dbName : "test_analytics";
                String finalDbUser = dbUser != null ? dbUser : "postgres";
                String finalDbPass = dbPass != null ? dbPass : "postgres";

                System.out.println("Initializing database connection with CLI parameters...");
                com.example.annotationextractor.database.DatabaseConfig.initializeFromCli(
                        finalDbHost, finalDbPort, finalDbName, finalDbUser, finalDbPass);
                // Initialize shadow database if provided
                if (sHost != null || sPort != null || sName != null || sUser != null || sPass != null) {
                    String finalSHost = sHost != null ? sHost : finalDbHost;
                    String finalSPort = sPort != null ? sPort : finalDbPort;
                    String finalSName = sName != null ? sName : finalDbName + "_shadow";
                    String finalSUser = sUser != null ? sUser : finalDbUser;
                    String finalSPass = sPass != null ? sPass : finalDbPass;
                    System.out.println("Initializing SHADOW database connection with CLI parameters...");
                    com.example.annotationextractor.database.DatabaseConfig.initializeShadowFromCli(
                            finalSHost, finalSPort, finalSName, finalSUser, finalSPass);
                }
            } else {
                // Use properties file
                System.out.println("Initializing database connection from properties file...");
                com.example.annotationextractor.database.DatabaseConfig.initialize();
                // Initialize shadow from properties if present
                com.example.annotationextractor.database.DatabaseConfig.initializeShadowIfConfigured();
            }

            // Parse repository list file
            String repositoryListFile = args[1];
            System.out.println("Reading repository list from: " + repositoryListFile);

            List<com.example.annotationextractor.runner.RepositoryHubRunnerConfig> runnerConfigs = com.example.annotationextractor.runner.RepositoryListProcessor
                    .readRepositoryHubRunnerConfigs(repositoryListFile);

            // Convert to ScanRepositoryEntry list
            List<com.example.annotationextractor.domain.model.ScanRepositoryEntry> scanEntries = new java.util.ArrayList<>();
            for (com.example.annotationextractor.runner.RepositoryHubRunnerConfig config : runnerConfigs) {
                scanEntries.add(new com.example.annotationextractor.domain.model.ScanRepositoryEntry(
                        config.getRepositoryUrl(),
                        config.getTeamName(),
                        config.getTeamCode(),
                        true // Default to active
                ));
            }

            System.out.println("Loaded " + scanEntries.size() + " repositories to scan");

            // Create and run the scanner
            GitRepositoryManager gitManager = new GitRepositoryManager(repositoryHubPath, username, password,
                    sshKeyPath);
            // Default max repositories per scan to a reasonable number if not specified
            int maxRepositoriesPerScan = 100;
            RepositoryHubScanner scanner = new RepositoryHubScanner(gitManager, scanEntries, maxRepositoriesPerScan);

            // Execute the full scan
            boolean success = scanner.executeFullScan(tempCloneMode);

            if (success) {
                System.exit(0);
            } else {
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println(
                "Usage: java RepositoryHubRunner <repository_hub_path> [username] [password] [ssh_key_path] [database_options] [--temp-clone]");
        System.out.println("       java RepositoryHubRunner --ssh-help");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println(
                "  repository_hub_path    Directory where repositories will be cloned/updated, and it should contain a file called scanConfig.txt");
        System.out.println("  username             Git username for private repositories (optional)");
        System.out.println("  password             Git password/token for private repositories (optional)");
        System.out.println("  ssh_key_path         Path to SSH private key for SSH authentication (optional)");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --temp-clone          Clone, scan, and delete repositories to save disk space");
        System.out.println("  --help, -h            Show this help message");
        System.out.println("  --ssh-help            Show SSH key format guidance and conversion instructions");
        System.out.println();
        System.out.println("Database Options (override database.properties):");
        System.out.println("  --db-host <host>     Database host (default: localhost)");
        System.out.println("  --db-port <port>     Database port (default: 5432)");
        System.out.println("  --db-name <name>     Database name (default: test_analytics)");
        System.out.println("  --db-user <user>     Database username (default: postgres)");
        System.out.println("  --db-pass <pass>     Database password (default: postgres)");
        System.out.println("Shadow Database Options (optional, for shadow write & perf validation):");
        System.out.println("  --shadow-db-host <host>    Shadow DB host (default: db-host)");
        System.out.println("  --shadow-db-port <port>    Shadow DB port (default: db-port)");
        System.out.println("  --shadow-db-name <name>    Shadow DB name (default: <db-name>_shadow)");
        System.out.println("  --shadow-db-user <user>    Shadow DB username (default: db-user)");
        System.out.println("  --shadow-db-pass <pass>    Shadow DB password (default: db-pass)");
        System.out.println();
        System.out.println("Authentication Methods:");
        System.out.println("  1. SSH (recommended if you have SSH keys configured):");
        System.out.println("     java RepositoryHubRunner ./repos ./repo-list.txt");
        System.out.println("     java RepositoryHubRunner ./repos ./repo-list.txt ~/.ssh/id_rsa");
        System.out.println("  2. HTTPS with username/password:");
        System.out.println("     java RepositoryHubRunner ./repos ./repo-list.txt myuser mytoken");
        System.out.println("  3. No authentication (public repos only):");
        System.out.println("     java RepositoryHubRunner ./repos ./repo-list.txt");
        System.out.println();
        System.out.println("Database Connection Examples:");
        System.out.println(
                "  java RepositoryHubRunner ./repos ./repo-list.txt --db-host mydb.example.com --db-port 5433 --db-name mydb --db-user myuser --db-pass mypass");
        System.out.println(
                "  java RepositoryHubRunner ./repos ./repo-list.txt --db-host mydb --db-name primary --shadow-db-host myshadow --shadow-db-name shadow --hex.write.shadow=true");
        System.out.println(
                "  java RepositoryHubRunner ./repos ./repo-list.txt --db-host localhost --db-name production_db");
        System.out.println();
        System.out.println("Temporary Clone Mode Examples:");
        System.out.println("  java RepositoryHubRunner ./repos ./repo-list.txt --temp-clone");
        System.out.println(
                "  java RepositoryHubRunner ./repos ./repo-list.txt --temp-clone --db-host localhost --db-name test_db");
        System.out.println("  java RepositoryHubRunner ./repos ./repo-list.txt myuser mytoken --temp-clone");
        System.out.println();
        System.out.println("Note: Database parameters not specified via CLI will use values from database.properties");
        System.out.println(
                "Note: --temp-clone mode processes repositories one by one, deleting each after scanning to save disk space");
    }

    private static boolean validatePaths(String repositoryHubPath) {

        // Check if repository hub path is writable
        try {
            java.nio.file.Path hubPath = Paths.get(repositoryHubPath);
            if (java.nio.file.Files.exists(hubPath)) {
                if (!java.nio.file.Files.isDirectory(hubPath)) {
                    System.err
                            .println("Error: Repository hub path exists but is not a directory: " + repositoryHubPath);
                    return false;
                }
                if (!java.nio.file.Files.isWritable(hubPath)) {
                    System.err.println("Error: Repository hub directory is not writable: " + repositoryHubPath);
                    return false;
                }
                if (!java.nio.file.Files.exists(hubPath.resolve("scanConfig.txt"))) {
                    System.err.println(
                            "Error: scanConfig.txt file not found in repository hub path: " + repositoryHubPath);
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("Error: Cannot access repository hub path: " + repositoryHubPath);
            return false;
        }

        return true;
    }

}
