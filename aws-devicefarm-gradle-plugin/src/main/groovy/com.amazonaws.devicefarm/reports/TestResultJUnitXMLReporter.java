package com.amazonaws.devicefarm.reports;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.PrintWriter;

import com.amazonaws.services.devicefarm.AWSDeviceFarm;

import com.amazonaws.devicefarm.DeviceFarmUtils;
import com.amazonaws.devicefarm.extension.DeviceFarmExtension;

import com.amazonaws.devicefarm.reports.junit.JUnitSuite;
import com.amazonaws.devicefarm.reports.junit.JUnitTest;

import com.amazonaws.services.devicefarm.model.Run;
import com.amazonaws.services.devicefarm.model.ListJobsRequest;
import com.amazonaws.services.devicefarm.model.Job;
import com.amazonaws.services.devicefarm.model.ListSuitesRequest;
import com.amazonaws.services.devicefarm.model.Suite;
import com.amazonaws.services.devicefarm.model.ListTestsRequest;
import com.amazonaws.services.devicefarm.model.Test;
import com.amazonaws.services.devicefarm.model.Counters;

import org.gradle.api.logging.Logger;

public class TestResultJUnitXMLReporter
{
    private Logger logger;
    private AWSDeviceFarm api;
    private DeviceFarmUtils utils;
    private Run run;
    private String junitXmlStr = "<?xml version=\"1.0\" ?>";
    
    public TestResultJUnitXMLReporter(Run run,
                                      DeviceFarmExtension extension,
                                      Logger logger,
                                      AWSDeviceFarm deviceFarmClient,
                                      DeviceFarmUtils utils) {
        this.logger = logger;
        this.api = deviceFarmClient;
        this.utils = utils;
        this.run = run;
    }
    
    private List<Job> getJobs(Run run) {
        List<Job> jobs = api.listJobs(new ListJobsRequest().withArn(run.getArn())).getJobs();
        logger.info("Found " + new Integer(jobs.size()).toString() + " jobs for run with url: " + utils.getRunUrlFromArn(run.getArn()));
        for (int jobIndex = 0; jobIndex < jobs.size(); jobIndex++) {
            Job curJob = jobs.get(jobIndex);
            logger.info( "(Type: " + curJob.getType() + ")"
                         + curJob.getName() + ": "
                         + curJob.getMessage() + " - "
                         + curJob.getResult());
        }
        
        return jobs;
    }
    
    public void writeResults(String destination) throws Exception {
        List<Job> jobs = getJobs(this.run);
        for (Job job : jobs) {
            List<Suite> suites = getSuites(job);
            ArrayList<JUnitSuite> junitSuites = new ArrayList<JUnitSuite>();

            for (int suiteIndex = 0; suiteIndex < suites.size(); suiteIndex++) {
                junitSuites.add(buildTestSuite(suites.get(suiteIndex)));
            }

            for (int junitSuiteIndex = 0; junitSuiteIndex < junitSuites.size(); junitSuiteIndex++) {
                JUnitSuite suite = junitSuites.get(junitSuiteIndex);

                if (destination != null) {
                    logger.info("Found JUnitXMLReporter Destination Path: " + destination);

                    File testsDirectory = new File(destination).getAbsoluteFile();
                    logger.debug("Resolving to: " + testsDirectory.getAbsolutePath());
            
                    try {
                
                        if (!testsDirectory.exists()) {
                            logger.info("Did not exist, creating new directory: " + testsDirectory.getAbsolutePath());
                            testsDirectory.mkdirs();
                        }
                
                        if (testsDirectory.exists()) {

                            String xmlFilePath = new File(destination + File.separator + "TEST-" + job.getDevice().getArn() + "-" + job.getDevice().getName() + "-" + suite.getClassName() + ".xml").getAbsoluteFile().getAbsolutePath();
                            logger.info("Writing File " + xmlFilePath);
                            String resultXmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                + suite.toString();

                            logger.debug("Result XML in File: " + xmlFilePath);
                            logger.debug(resultXmlStr);
                
                            PrintWriter writer = new PrintWriter(xmlFilePath);
                            writer.write(resultXmlStr);
                            writer.close();
                        }
                    }
                    catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
        
    public JUnitSuite buildTestSuite(Suite suite) {
        JUnitSuite junitSuite = new JUnitSuite(suite.getName());
        ArrayList<JUnitTest> junitTests = new ArrayList<JUnitTest>();
        
        Counters counters = suite.getCounters();
        List<Test> tests;
        try {
            tests = getTests(suite);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
            junitTests.add(JUnitTest.fromTest(tests.get(testIndex)));
        }
        
        junitSuite.setTests(junitTests);
        junitSuite.setErrors(counters.getErrored());
        junitSuite.setFailures(counters.getFailed());
         
        return junitSuite;
    }
    
    public List<Suite> getSuites(Job job) throws Exception
    {
        logger.debug("Looking up Suites for Job:" + job.getName());
        return api.listSuites(new ListSuitesRequest().withArn(job.getArn())).getSuites();
    }

    public List<Test> getTests(Suite suite) throws Exception
    {
        logger.debug("Looking up Tests for Suite:" + suite.getName());
        return api.listTests(new ListTestsRequest().withArn(suite.getArn())).getTests();
    }
}


