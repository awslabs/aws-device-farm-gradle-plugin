package com.amazonaws.devicefarm.reports.junit;

import java.util.List;

public class JUnitSuite
{
    private String name;
    private int errors;
    private int failures;
    private List<JUnitTest> tests;
    
    public JUnitSuite(String name) {
        this.name = name;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public void setTests(List<JUnitTest> tests) {
        this.tests = tests;
    }

    public void setFailures(int failures) {
        this.failures = failures;
    }

    public String getClassName() {
        return this.name;
    }
    
    @Override
    public String toString()
    {
        String str = "<testsuite errors=\"" + new Integer(this.errors).toString()
            + "\" failures=\"" + new Integer(this.failures).toString()
            + "\" name=\"" + this.name
            + "\" tests=\"" + new Integer(this.tests.size()).toString() + "\">\n";
        for (int testIndex = 0; testIndex < this.tests.size(); testIndex++) {
            JUnitTest test = this.tests.get(testIndex);
            str += test.toString();
        }
        str += "</testsuite>\n";
        return str;
    }
}
