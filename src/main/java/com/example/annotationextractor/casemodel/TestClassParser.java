package com.example.annotationextractor.casemodel;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Parser class to extract test method information from Java test classes
 */
public class TestClassParser {
    
    // Registry for extracting test case IDs from any annotation
    private static final TestCaseIdExtractorRegistry testCaseIdRegistry = new TestCaseIdExtractorRegistry();
    private static final Object PARSER_LOCK = new Object();
    private static JavaParser sharedParser;
    private static Path activeRepositoryRoot;
    private static Path cachedDependenciesDir;
    private static List<JarTypeSolver> cachedJarTypeSolvers = Collections.emptyList();
    
    /**
     * Parse a Java test class file and extract all test method information
     * 
     * @param filePath Path to the Java test class file
     * @return TestClassInfo object containing all extracted test method information
     * @throws IOException if file cannot be read
     */
    public static TestClassInfo parseTestClass(Path filePath) throws IOException {
        ParseResult result = parseTestClassWithHelpers(filePath);
        return result.getTestClassInfo();
    }
    
    /**
     * Parse a Java test class file and extract both test classes and helper classes
     * 
     * @param filePath Path to the Java test class file
     * @return ParseResult object containing both test class info and helper classes
     * @throws IOException if file cannot be read
     */
    public static ParseResult parseTestClassWithHelpers(Path filePath) throws IOException {
        File file = filePath.toFile();
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Cannot read file: " + filePath);
        }

        // Read the entire file content for storing later
        String fileContent = Files.readString(filePath, StandardCharsets.UTF_8);

        try (FileInputStream fis = new FileInputStream(file)) {
            JavaParser parser = getJavaParser();
            CompilationUnit cu = parser.parse(fis).getResult().orElse(null);
            if (cu == null) {
                throw new IOException("Failed to parse Java file: " + filePath);
            }
            
            TestClassVisitor visitor = new TestClassVisitor(filePath, fileContent);
            cu.accept(visitor, null);
            
            // Check if we found multiple public classes (invalid Java)
            if (visitor.hasMultiplePublicClasses()) {
                System.err.println("⚠️  WARNING: File contains multiple public classes: " + filePath);
                System.err.println("   Public classes found: " + visitor.getPublicClassNames());
                System.err.println("   This is invalid Java and will be skipped.");
                return new ParseResult(new TestClassInfo(), new ArrayList<>());
            }
            
            return new ParseResult(visitor.getTestClassInfo(), visitor.getHelperClasses());
        }
    }
    
    /**
     * Check if a method is a test method based on annotations
     */
    private static boolean isTestMethod(MethodDeclaration method) {
        for (AnnotationExpr annotation : method.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (annotationName.equals("Test") || 
                annotationName.equals("org.junit.Test") ||
                annotationName.equals("org.junit.jupiter.api.Test") ||
                annotationName.equals("junit.framework.TestCase")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Configure the shared JavaParser with symbol resolution for the current repository and dependency jars.
     *
     * @param repositoryRoot root directory of the repository being scanned (can be null)
     * @param dependenciesDir directory containing dependency jars (can be null)
     * @throws IOException if dependency jars cannot be read
     */
    public static void configureSymbolResolver(Path repositoryRoot, Path dependenciesDir) throws IOException {
        Path normalizedRepoRoot = repositoryRoot == null ? null : repositoryRoot.toAbsolutePath().normalize();
        Path normalizedDepsDir = dependenciesDir == null ? null : dependenciesDir.toAbsolutePath().normalize();

        synchronized (PARSER_LOCK) {
            boolean repoChanged = !Objects.equals(activeRepositoryRoot, normalizedRepoRoot);
            boolean depsChanged = !Objects.equals(cachedDependenciesDir, normalizedDepsDir);

            if (depsChanged) {
                cachedJarTypeSolvers = loadJarTypeSolvers(normalizedDepsDir);
                cachedDependenciesDir = normalizedDepsDir;
            }

            if (sharedParser == null || repoChanged || depsChanged) {
                CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
                combinedTypeSolver.add(new ReflectionTypeSolver());

                registerSourceDirectory(normalizedRepoRoot, combinedTypeSolver, "src/main/java");
                registerSourceDirectory(normalizedRepoRoot, combinedTypeSolver, "src/test/java");

                for (JarTypeSolver typeSolver : cachedJarTypeSolvers) {
                    combinedTypeSolver.add(typeSolver);
                }

                JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
                JavaParser parser = new JavaParser();
                parser.getParserConfiguration().setSymbolResolver(symbolSolver);
                sharedParser = parser;
                activeRepositoryRoot = normalizedRepoRoot;
            }
        }
    }

    private static void registerSourceDirectory(Path repositoryRoot, CombinedTypeSolver combinedTypeSolver, String relativePath) {
        if (repositoryRoot == null) {
            return;
        }
        Path sourceDir = repositoryRoot.resolve(relativePath);
        if (Files.isDirectory(sourceDir)) {
            try {
                combinedTypeSolver.add(new JavaParserTypeSolver(sourceDir.toFile()));
            } catch (IllegalArgumentException ex) {
                System.err.println("Failed to register source directory " + sourceDir + ": " + ex.getMessage());
            }
        }
    }

    private static List<JarTypeSolver> loadJarTypeSolvers(Path depsDir) throws IOException {
        if (depsDir == null || !Files.isDirectory(depsDir)) {
            return Collections.emptyList();
        }

        List<JarTypeSolver> solvers = new ArrayList<>();
        try (Stream<Path> pathStream = Files.walk(depsDir)) {
            pathStream
                .filter(path -> Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".jar"))
                .forEach(path -> {
                    try {
                        solvers.add(new JarTypeSolver(path.toFile()));
                    } catch (IOException e) {
                        System.err.println("Failed to load dependency jar " + path + ": " + e.getMessage());
                    }
                });
        }

        System.out.println("Loaded " + solvers.size() + " dependency jars from " + depsDir);
        return solvers;
    }

    private static JavaParser getJavaParser() {
        synchronized (PARSER_LOCK) {
            if (sharedParser == null) {
                sharedParser = new JavaParser();
            }
            return sharedParser;
        }
    }
    
    /**
     * Check if a class is a test class
     */
    private static boolean isTestClass(ClassOrInterfaceDeclaration classDecl) {
        // Only process PUBLIC classes (JUnit only executes public classes)
        if (!classDecl.isPublic()) {
            return false;
        }
        
        // Check if class name ends with "Test" or "Tests" or starts with "Test"
        String className = classDecl.getNameAsString();
        if (className.endsWith("Test") || className.toLowerCase().endsWith("Tests") || className.startsWith("Test")){
            return true;
        }
        
        // Check if class has test annotations
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (annotationName.equals("Test") || 
                annotationName.equals("org.junit.Test") ||
                annotationName.equals("org.junit.jupiter.api.Test")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Visitor class to traverse the AST and extract test method information
     */
    private static class TestClassVisitor extends VoidVisitorAdapter<Void> {
        private final Path filePath;
        private final String fileContent;
        private TestClassInfo testClassInfo;
        private String packageName = "";
        private List<String> publicClassNames = new ArrayList<>();
        private List<Integer> helperClassLineNumbers = new ArrayList<>();
        private boolean hasMultiplePublicClasses = false;
        private final List<TestHelperClassInfo> helperClasses = new ArrayList<>();
        private final List<String> importedTypes = new ArrayList<>();
        private final Set<String> referencedTypes = new HashSet<>();

        public TestClassVisitor(Path filePath, String fileContent) {
            this.filePath = filePath;
            this.fileContent = fileContent;
            this.testClassInfo = new TestClassInfo();
        }
        
        @Override
        public void visit(ImportDeclaration importDecl, Void arg) {
            importedTypes.add(importDecl.getNameAsString());
            super.visit(importDecl, arg);
        }

        @Override
        public void visit(CompilationUnit cu, Void arg) {
            // Extract package name
            Optional<com.github.javaparser.ast.PackageDeclaration> packageDecl = cu.getPackageDeclaration();
            if (packageDecl.isPresent()) {
                this.packageName = packageDecl.get().getNameAsString();
            }
            
            super.visit(cu, arg);
        }
        
        @Override
        public void visit(ClassOrInterfaceDeclaration classDecl, Void arg) {
            recordResolvedTypes(classDecl.getExtendedTypes());
            recordResolvedTypes(classDecl.getImplementedTypes());
            classDecl.getAnnotations().forEach(this::recordResolvedAnnotation);

            // Debug: Log all classes found
            String className = classDecl.getNameAsString();
            boolean isPublic = classDecl.isPublic();
            System.out.println("🔍 Found class: " + className + " (public: " + isPublic + ")");
            
            // Track public classes for validation (only top-level)
            boolean isTopLevel = !classDecl.isInnerClass();
            if (isPublic && isTopLevel) {
                publicClassNames.add(className);
                if (publicClassNames.size() > 1) {
                    hasMultiplePublicClasses = true;
                }
            }
            
            // Get the line number where the class is defined
            Integer classLineNumber = classDecl.getBegin().map(p -> p.line).orElse(null);
            
            // Only process TOP-LEVEL classes
            if (isTopLevel) {
                if (isTestClass(classDecl)) {
                    // This is a top-level test class
                    System.out.println("✅ Processing test class: " + className);
                    testClassInfo.setClassName(classDecl.getNameAsString());
                    testClassInfo.setPackageName(packageName);
                    testClassInfo.setFilePath(filePath.toString());
                    testClassInfo.setClassLineNumber(classLineNumber);
                    // Store the entire file content
                    testClassInfo.setTestClassContent(fileContent);
                    // Calculate class LOC from file content
                    if (fileContent != null) {
                        long lineCount = fileContent.lines().count();
                        testClassInfo.setClassLoc((int) lineCount);
                    }

                    
                    super.visit(classDecl, arg);
                } else {
                    // This is a top-level non-test class (helper class)
                    System.out.println("❌ Processing helper class: " + className);
                    // Track helper class line numbers
                    if (classLineNumber != null) {
                        helperClassLineNumbers.add(classLineNumber);
                    }
                    
                    // Create helper class info for non-test classes
                    TestHelperClassInfo helperClass = new TestHelperClassInfo();
                    helperClass.setClassName(className);
                    helperClass.setPackageName(packageName);
                    helperClass.setFilePath(filePath.toString());
                    helperClass.setClassLineNumber(classLineNumber);
                    // Store full file content for each helper class in the file
                    helperClass.setHelperClassContent(fileContent);
                    
                    // Calculate LOC for this specific class (class body only)
                    int startLine = classDecl.getBegin().map(p -> p.line).orElse(0);
                    int endLine = classDecl.getEnd().map(p -> p.line).orElse(startLine);
                    int classLoc = Math.max(0, endLine - startLine + 1);
                    helperClass.setLoc(classLoc);
                    
                    helperClasses.add(helperClass);
                    
                    // Don't visit children for helper classes (we only track the top-level class)
                    // super.visit(classDecl, arg); // Commented out to avoid processing inner classes
                }
            } else {
                // This is an inner/nested class - skip it (part of the parent class)
                System.out.println("⏭️  Skipping inner class: " + className);
                super.visit(classDecl, arg);
            }
        }
        
        @Override
        public void visit(MethodDeclaration methodDecl, Void arg) {
            recordResolvedType(methodDecl.getType());
            for (Parameter parameter : methodDecl.getParameters()) {
                recordResolvedType(parameter.getType());
            }
            methodDecl.getThrownExceptions().forEach(this::recordResolvedType);
            methodDecl.getAnnotations().forEach(this::recordResolvedAnnotation);

            // Only process test methods
            if (isTestMethod(methodDecl)) {
                TestMethodInfo testMethodInfo = new TestMethodInfo();
                testMethodInfo.setMethodName(methodDecl.getNameAsString());
                testMethodInfo.setClassName(testClassInfo.getClassName());
                testMethodInfo.setPackageName(testClassInfo.getPackageName());
                testMethodInfo.setFilePath(testClassInfo.getFilePath());
                testMethodInfo.setLineNumber(methodDecl.getBegin().get().line);
                
                // Calculate method lines of code (method body size)
                int methodLoc = 0;
                int startLine = methodDecl.getBegin().get().line;
                int endLine = methodDecl.getEnd().map(p -> p.line).orElse(startLine);
                methodLoc = endLine - startLine + 1; // +1 because both start and end are inclusive
                testMethodInfo.setMethodLoc(methodLoc);
                
                // Extract method body content from file content
                if (fileContent != null && startLine > 0 && endLine >= startLine) {
                    try {
                        String[] lines = fileContent.split("\n", -1); // -1 to preserve trailing empty lines
                        if (lines.length >= endLine) {
                            // Extract lines from startLine-1 to endLine-1 (0-indexed)
                            StringBuilder methodBody = new StringBuilder();
                            for (int i = startLine - 1; i < endLine && i < lines.length; i++) {
                                if (i > startLine - 1) {
                                    methodBody.append("\n");
                                }
                                methodBody.append(lines[i]);
                            }
                            testMethodInfo.setMethodBodyContent(methodBody.toString());
                        }
                    } catch (Exception e) {
                        // If extraction fails, leave method body empty
                        System.err.println("Warning: Failed to extract method body for " + 
                            testMethodInfo.getMethodName() + ": " + e.getMessage());
                        testMethodInfo.setMethodBodyContent("");
                    }
                }
                
                List<AnnotationExpr> annotations = methodDecl.getAnnotations();
                
                // Extract test case IDs from ALL annotations using the plugin-based registry
                String[] testCaseIds = testCaseIdRegistry.extractTestCaseIds(annotations);
                testMethodInfo.setTestCaseIds(testCaseIds);
                
                // Also extract UnittestCaseInfo annotation data for backward compatibility
                for (AnnotationExpr annotation : annotations) {
                    if (annotation.getNameAsString().equals("UnittestCaseInfo")) {
                        UnittestCaseInfoData annotationData = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
                        testMethodInfo.setAnnotationData(annotationData);
                        break;
                    }
                }
                
                testClassInfo.addTestMethod(testMethodInfo);
            }

            super.visit(methodDecl, arg);
        }

        @Override
        public void visit(ClassOrInterfaceType type, Void arg) {
            recordResolvedType(type);
            super.visit(type, arg);
        }

        @Override
        public void visit(ObjectCreationExpr expr, Void arg) {
            recordResolvedType(expr.getType());
            super.visit(expr, arg);
        }

        @Override
        public void visit(FieldDeclaration field, Void arg) {
            recordResolvedType(field.getElementType());
            field.getAnnotations().forEach(this::recordResolvedAnnotation);
            super.visit(field, arg);
        }

        @Override
        public void visit(VariableDeclarationExpr variableDecl, Void arg) {
            recordResolvedType(variableDecl.getElementType());
            variableDecl.getAnnotations().forEach(this::recordResolvedAnnotation);
            super.visit(variableDecl, arg);
        }

        @Override
        public void visit(MethodCallExpr methodCall, Void arg) {
            recordResolvedMethod(methodCall);
            super.visit(methodCall, arg);
        }

        public TestClassInfo getTestClassInfo() {
            // Set helper class line numbers before returning
            if (!helperClassLineNumbers.isEmpty()) {
                testClassInfo.setHelperClassesLineNumbers(
                    String.join(",", helperClassLineNumbers.stream()
                        .map(String::valueOf)
                        .toArray(String[]::new))
                );
            }
            if (!importedTypes.isEmpty()) {
                testClassInfo.setImportedTypes(new ArrayList<>(importedTypes));
            }
            if (!referencedTypes.isEmpty()) {
                testClassInfo.setReferencedTypes(new ArrayList<>(referencedTypes));
            }
            return testClassInfo;
        }

        private void recordResolvedTypes(List<ClassOrInterfaceType> types) {
            if (types == null) {
                return;
            }
            for (ClassOrInterfaceType type : types) {
                recordResolvedType(type);
            }
        }

        private void recordResolvedType(Type type) {
            if (type == null) {
                return;
            }
            try {
                ResolvedType resolvedType = type.resolve();
                addResolvedType(resolvedType);
            } catch (UnsolvedSymbolException | UnsupportedOperationException | IllegalStateException e) {
                // Ignore unresolved types; they may not be available on the configured classpath
            }
        }

        private void addResolvedType(ResolvedType resolvedType) {
            if (resolvedType == null) {
                return;
            }
            if (resolvedType.isPrimitive()) {
                return;
            }
            if (resolvedType.isArray()) {
                ResolvedArrayType arrayType = resolvedType.asArrayType();
                addResolvedType(arrayType.getComponentType());
                return;
            }
            if (resolvedType.isReferenceType()) {
                ResolvedReferenceType referenceType = resolvedType.asReferenceType();
                referencedTypes.add(referenceType.getQualifiedName());
                referenceType.typeParametersValues().forEach(this::addResolvedType);
                return;
            }
            if (resolvedType.isWildcard()) {
                if (resolvedType.asWildcard().isBounded()) {
                    addResolvedType(resolvedType.asWildcard().getBoundedType());
                }
            }
        }

        private void recordResolvedMethod(MethodCallExpr methodCall) {
            try {
                ResolvedMethodDeclaration resolvedMethod = methodCall.resolve();
                ResolvedReferenceTypeDeclaration declaringType = resolvedMethod.declaringType();
                if (declaringType != null) {
                    referencedTypes.add(declaringType.getQualifiedName());
                }
                addResolvedType(resolvedMethod.getReturnType());
                for (int i = 0; i < resolvedMethod.getNumberOfParams(); i++) {
                    addResolvedType(resolvedMethod.getParam(i).getType());
                }
            } catch (UnsolvedSymbolException | UnsupportedOperationException | IllegalStateException e) {
                // Ignore unresolved method calls
            }
        }

        private void recordResolvedAnnotation(AnnotationExpr annotation) {
            try {
                ResolvedAnnotationDeclaration resolvedAnnotation = annotation.resolve();
                referencedTypes.add(resolvedAnnotation.getQualifiedName());
            } catch (UnsolvedSymbolException | UnsupportedOperationException | IllegalStateException e) {
                // Ignore unresolved annotations
            }
        }
        
        public List<TestHelperClassInfo> getHelperClasses() {
            return helperClasses;
        }
        
        public boolean hasMultiplePublicClasses() {
            return hasMultiplePublicClasses;
        }
        
        public String getPublicClassNames() {
            return String.join(", ", publicClassNames);
        }
    }
}
