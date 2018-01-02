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
    
    public void writeResults(String destination) throws Exception{

        if (destination != null) {
            logger.info("Found JUnitXMLReporter Destination Path: " + destination);

            File parentDirectory = new File(destination).getParentFile().getAbsoluteFile();
            logger.debug("Resolving to: " + parentDirectory.getAbsolutePath());
            
            try {

                if (!parentDirectory.exists()) {
                    logger.info("Did not exist, creating new directory: " + parentDirectory.getAbsolutePath());
                    parentDirectory.mkdirs();
                }
                
                if (parentDirectory.exists()) {
                    String xmlFilePath = new File(destination).getAbsoluteFile().getAbsolutePath();
                    logger.info("Writing File " + xmlFilePath);
                    String resultXmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
                    resultXmlStr += buildResultsString();

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
        // NO-OP if destination is null
    }
    
    public String buildResultsString() {
        List<Suite> suites;
        try {
            suites = getSuites(getJobs(this.run).get(0));
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "";
        }
        String resultStr = "";
        for (int suiteIndex = 0; suiteIndex < suites.size(); suiteIndex++) {
            resultStr += buildTestSuiteString(suites.get(suiteIndex));
        }
        return resultStr;
    }
    
    public String buildTestSuiteString(Suite suite) {
        JUnitSuite junitSuite = new JUnitSuite(suite.getName());
        ArrayList<JUnitTest> junitTests = new ArrayList<JUnitTest>();
        
        Counters counters = suite.getCounters();
        List<Test> tests;
        try {
            tests = getTests(suite);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "";
        }

        for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
            junitTests.add(JUnitTest.fromTest(tests.get(testIndex)));
        }
        
        junitSuite.setTests(junitTests);
        junitSuite.setErrors(counters.getErrored());
        junitSuite.setFailures(counters.getFailed());
         
        return junitSuite.toString();
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


