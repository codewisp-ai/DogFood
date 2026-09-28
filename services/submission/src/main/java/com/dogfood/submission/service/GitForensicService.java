package com.dogfood.submission.service;

import com.dogfood.submission.entity.Submission;
import com.dogfood.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitForensicService {

    private final SubmissionRepository submissionRepository;

    @Async
    public void analyzeRepository(UUID submissionId, String repositoryUrl, Instant eventStartDate) {
        log.info("Starting forensic analysis for submission: {} repo: {}", submissionId, repositoryUrl);
        
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) return;
        
        File tempDir = null;
        try {
            tempDir = Files.createTempDirectory("jgit_analysis_" + submissionId).toFile();
            
            try (Git git = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(tempDir)
                    .setCloneAllBranches(false)
                    .call()) {
                
                Iterable<RevCommit> commits = git.log().call();
                
                int totalCommits = 0;
                int commitsBeforeStart = 0;
                
                for (RevCommit commit : commits) {
                    totalCommits++;
                    Instant commitTime = Instant.ofEpochSecond(commit.getCommitTime());
                    if (commitTime.isBefore(eventStartDate)) {
                        commitsBeforeStart++;
                    }
                }
                
                List<String> flags = new ArrayList<>();
                double riskScore = 0.0;
                
                if (totalCommits == 0) {
                    flags.add("No commits found");
                    riskScore = 100.0;
                } else if (totalCommits == 1) {
                    flags.add("Single monolithic commit detected");
                    riskScore = 80.0;
                } else {
                    double earlyRatio = (double) commitsBeforeStart / totalCommits;
                    if (earlyRatio > 0.90) {
                        flags.add(">90% of commits made before hackathon started");
                        riskScore = earlyRatio * 100.0;
                    } else if (earlyRatio > 0.50) {
                        flags.add("Majority of commits made before hackathon started");
                        riskScore = earlyRatio * 80.0;
                    }
                }
                
                submission.setForensicStatus("SCANNED");
                submission.setRiskScore(Math.min(100.0, riskScore));
                submission.setRiskFlags(flags);
                submissionRepository.save(submission);
                
                log.info("Finished forensic analysis for submission {}: Score {}", submissionId, riskScore);
            }
        } catch (Exception e) {
            log.error("Failed to analyze repository {}", repositoryUrl, e);
            submission.setForensicStatus("FAILED");
            submission.setRiskFlags(List.of("Forensic scan failed: " + e.getMessage()));
            submissionRepository.save(submission);
        } finally {
            if (tempDir != null && tempDir.exists()) {
                deleteDirectory(tempDir);
            }
        }
    }
    
    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
