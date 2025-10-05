package com.example.annotationextractor.application;

import com.example.annotationextractor.domain.model.TestClass;
import com.example.annotationextractor.domain.model.TestMethod;
import com.example.annotationextractor.domain.model.TestMethodDetailRecord;
import com.example.annotationextractor.domain.port.TestClassPort;
import com.example.annotationextractor.domain.port.TestMethodPort;

import java.util.List;
import java.util.Optional;

public class TestArtifactQueryService {

    private final TestClassPort testClassPort;
    private final TestMethodPort testMethodPort;

    public TestArtifactQueryService(TestClassPort testClassPort, TestMethodPort testMethodPort) {
        this.testClassPort = testClassPort;
        this.testMethodPort = testMethodPort;
    }

    public List<TestClass> listClassesByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) {
        return testClassPort.findByRepositoryIdAndScanSessionId(repositoryId, scanSessionId);
    }

    public Optional<TestClass> listClassByRepositoryIdAndScanSessionIdAndFilePath(Long repositoryId, Long scanSessionId, String filePath) {
        return testClassPort.findByRepositoryIdAndScanSessionIdAndFilePath(repositoryId, scanSessionId, filePath);
    }

    public List<TestClass> listClassesByScanSessionId(Long scanSessionId) {
        return testClassPort.findAllByScanSessionId(scanSessionId);
    }

    public long countClassesByScanSessionId(Long scanSessionId) {
        return testClassPort.countAllByScanSessionId(scanSessionId);
    }

    public List<TestMethod> listMethodsByTestClassId(Long testClassId) {
        return testMethodPort.findByTestClassId(testClassId);
    }

    public List<TestMethod> listMethodsByScanSessionId(Long scanSessionId) {
        return testMethodPort.findByScanSessionId(scanSessionId);
    }

    public List<TestMethod> listAnnotatedMethodsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) {
        return testMethodPort.findAnnotatedByRepositoryAndScanSessionId(repositoryId, scanSessionId);
    }

    public List<TestMethodDetailRecord> listTestMethodDetailsByTeamIdAndScanSessionId(Long teamId, Long scanSessionId, Integer limit) {
        return testMethodPort.findTestMethodDetailsByTeamIdAndScanSessionId(teamId, scanSessionId, limit);
    }

    public long countTestMethodDetailsByTeamIdAndScanSessionId(Long teamId, Long scanSessionId) {
        return testMethodPort.countByTeamIdAndScanSessionId(teamId, scanSessionId);
    }

    public List<TestMethodDetailRecord> listTestMethodDetailsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId, Integer limit) {
        return testMethodPort.findTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, scanSessionId, limit);
    }

    public long countTestMethodDetailsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) {
        return testMethodPort.countByRepositoryIdAndScanSessionId(repositoryId, scanSessionId);
    }

    

    public List<TestMethodDetailRecord> listTestMethodDetailsByClassId(Long classId, Integer limit) {
        return testMethodPort.findTestMethodDetailsByClassId(classId, limit);
    }

    public long countTestMethodDetailsByClassId(Long classId) {
        return testMethodPort.countByClassId(classId);
    }
    

    public List<TestMethodDetailRecord> listTestMethodDetailsByScanSessionId(Long scanSessionId, Integer limit) {
        return testMethodPort.findTestMethodDetailsByScanSessionId(scanSessionId, limit);
    }
    
    public long countTestMethodDetailsByScanSessionId(Long scanSessionId) {
        return testMethodPort.countByScanSessionId(scanSessionId);
    }


}


