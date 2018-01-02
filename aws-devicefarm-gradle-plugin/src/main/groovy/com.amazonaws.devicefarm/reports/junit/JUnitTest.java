package com.amazonaws.devicefarm.reports.junit;

import java.util.Date;
import com.amazonaws.services.devicefarm.model.Test;

public class JUnitTest
{
    private enum TestState {
        FAILED,
        SKIPPED,
        SUCCESS
    };

    private String name;
    private String classname;
    private String failureMessage;

    private Date started;
    private Date stopped;

    private TestState state;

    public JUnitTest(String name)
    {
        this.name = name;
        this.classname = name;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public void setStateFromString(String state) {
        switch(state) {
            case "SUCCESS":
            case "PASSED":
                this.state = TestState.SUCCESS;
                break;
            case "SKIPPED":
            case "PENDING":
            case "STOPPED":
                this.state = TestState.SKIPPED;
                break;
            case "FAILED":
            case "ERRORED":
            case "WARNED":
                this.state = TestState.FAILED;
                break;
        }
    }

    public TestState getState() {
        return this.state;
    }
    
    public void setStarted(Date started) {
        this.started = started;
    }

    public void setStopped(Date stopped) {
        this.stopped = stopped;
    }

    public static JUnitTest fromTest (Test test) {
        JUnitTest unitTest = new JUnitTest(test.getName());
        unitTest.setStarted(test.getStarted());
        unitTest.setStopped(test.getStopped());
        unitTest.setStateFromString(test.getResult());
        switch (unitTest.getState()) {
            case FAILED:
                unitTest.setFailureMessage(test.getMessage());
                break;
            case SUCCESS:
            case SKIPPED:
                break;
            default:
                break;
        }

        return unitTest;
    }

    @Override
    public String toString()
    {
        Long time = null;
        if (this.stopped != null && this.started != null) {
            time = new Long(this.stopped.getTime() - this.started.getTime());
        }

        String str = "<testcase classname=\"" + this.classname + "\" name=\"" + this.name + "\"" + (time != null ? " time=\"" + time.toString() + "\"" : "") + ">\n";
        switch (state) {
            case SKIPPED:
                str += "<skipped />\n";
                break;
            case FAILED:
                str += "<failure message=\"" + this.failureMessage + "\"/>\n";
                break;
            case SUCCESS:
                break;
        };

        str += "</testcase>\n";
        return str;
    }
}
