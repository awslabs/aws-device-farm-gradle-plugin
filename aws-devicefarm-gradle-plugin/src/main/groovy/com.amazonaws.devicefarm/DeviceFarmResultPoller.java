//
// Copyright 2015-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// Copyright 2017 Andreas Marschke. All Rights Reserved.
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

import java.util.ArrayList;
import java.lang.Thread;

import com.amazonaws.services.devicefarm.AWSDeviceFarm;
import com.amazonaws.services.devicefarm.AWSDeviceFarmClient;
import com.amazonaws.devicefarm.extension.DeviceFarmExtension;

import com.amazonaws.services.devicefarm.model.ListRunsResult;
import com.amazonaws.services.devicefarm.model.ListRunsRequest;
import com.amazonaws.services.devicefarm.model.GetRunResult;
import com.amazonaws.services.devicefarm.model.GetRunRequest;
import com.amazonaws.services.devicefarm.model.Run;
import com.amazonaws.services.devicefarm.model.ExecutionStatus;
import com.amazonaws.services.devicefarm.model.Counters;

import com.android.builder.testing.api.TestServer;
import org.gradle.api.logging.Logger;

public class DeviceFarmResultPoller {

    private DeviceFarmExtension extension;
    private Logger logger;
    private AWSDeviceFarm api;
    private DeviceFarmUtils utils;
    private final int MAX_TIMEOUT = 3000000;
    private final int SLEEP_DURATION = 2000;
    
    public DeviceFarmResultPoller(DeviceFarmExtension extension,
                                  Logger logger,
                                  AWSDeviceFarm deviceFarmClient,
                                  DeviceFarmUtils utils) {
        this.extension = extension;
        this.logger = logger;
        this.api = deviceFarmClient;
        this.utils = utils;
    }

    public Run pollForRunCompletedStatus(String arn) throws Exception {
        logger.info("Monitoring Run from Arn: " + arn);
        String lastStatus = "";
        int currentWait = 0;
        
        while(MAX_TIMEOUT > currentWait) {
            currentWait += SLEEP_DURATION;
            Run run = api.getRun(new GetRunRequest().withArn(arn)).getRun();

            if (!lastStatus.equals(run.getStatus().toString())) {
                logger.info(String.format("Currently testing RUN with Arn: %s and status: %s (URL: %s )",
                                          run.getArn(),
                                          run.getStatus().toString(),
                                          utils.getRunUrlFromArn(run.getArn())));

                lastStatus = run.getStatus().toString();
            }

            if (run.getStatus().equals(ExecutionStatus.COMPLETED.toString())) {
                logger.info("Run reached COMPLETED!");
                return run;
            }

            Thread.sleep(SLEEP_DURATION);
        }

        return null;
    }
}
