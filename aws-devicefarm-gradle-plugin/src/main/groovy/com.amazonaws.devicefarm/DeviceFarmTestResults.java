//
// Copyright 2015-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License").
// You may not use this file except in compliance with the License.
// A copy of the License is located at
//
// http://aws.amazon.com/apache2.0
//
// or in the "license" file accompanying this file. This file is distributed
// on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package com.amazonaws.devicefarm;

import java.util.List;

import com.amazonaws.services.devicefarm.AWSDeviceFarm;

import com.amazonaws.services.devicefarm.model.Run;
import com.amazonaws.services.devicefarm.model.ListJobsRequest;
import com.amazonaws.services.devicefarm.model.Job;
import com.amazonaws.services.devicefarm.model.ListSuitesRequest;
import com.amazonaws.services.devicefarm.model.Suite;
import com.amazonaws.services.devicefarm.model.ListTestsRequest;

import com.amazonaws.services.devicefarm.model.Test;

import com.amazonaws.devicefarm.extension.DeviceFarmExtension;

import org.gradle.api.logging.Logger;

public class DeviceFarmTestResults
{
    private Logger logger;
    private AWSDeviceFarm api;
    private DeviceFarmUtils utils;
    
    public DeviceFarmTestResults(Run run, DeviceFarmExtension extension,
                                 Logger logger,
                                 AWSDeviceFarm deviceFarmClient,
                                 DeviceFarmUtils utils) {
        this.logger = logger;
        this.api = deviceFarmClient;
        this.utils = utils;
    }

    public List<Suite> getSuites(Job job) throws Exception
    {
        logger.info("Looking up Suites for Job:" + job.getName());
        return api.listSuites(new ListSuitesRequest().withArn(job.getArn())).getSuites();
    }

    public List<Test> getTests(Suite suite) throws Exception
    {
        logger.info("Looking up Tests for Suite:" + suite.getName());
        return api.listTests(new ListTestsRequest().withArn(suite.getArn())).getTests();
    }
}

