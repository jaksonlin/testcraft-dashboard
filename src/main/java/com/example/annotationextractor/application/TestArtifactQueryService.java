package com.example.annotationextractor.application;

import com.example.annotationextractor.domain.model.TestClass;
import com.example.annotationextractor.domain.model.TestMethod;
import com.example.annotationextractor.domain.port.TestClassPort;
import com.example.annotationextractor.domain.port.TestMethodPort;

import java.util.List;

public class TestArtifactQueryService {

    private final TestClassPort testClassPort;
    private final TestMethodPort testMethodPort;

    public TestArtifactQueryService(TestClassPort testClassPort, TestMethodPort testMethodPort) {
        this.testClassPort = testClassPort;
        this.testMethodPort = testMethodPort;
    }

    public List<TestClass> listClassesByRepository(Long repositoryId) {
        return testClassPort.findByRepositoryId(repositoryId);
    }

    public List<TestMethod> listAnnotatedMethodsByRepository(Long repositoryId) {
        return testMethodPort.findAnnotatedByRepository(repositoryId);
    }

    public long countClasses() { return testClassPort.count(); }
    public long countMethods() { return testMethodPort.count(); }
}


