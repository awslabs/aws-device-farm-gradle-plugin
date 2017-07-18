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
package com.amazonaws.devicefarm.extension

import com.amazonaws.services.devicefarm.model.ExecutionConfiguration;

/**
 * Execution Configuration parameters
 */
class ExecutionConfiguration {

    def maxExecutionTime = 60
    def videoRecording = true
    def performanceMonitoring = true

    // These methods make the '=' optional when configuring the plugin
    void maxExecutionTime(int i) { maxExecutionTime = i }

    void videoRecording(String onOff) { videoRecording = ConfigurationOnOff.valueOf(onOff).bool }

    void performanceMonitoring(String onOff) { performanceMonitoring = ConfigurationOnOff.valueOf(onOff).bool }

    int getMaxExecutionTime() {
        maxExecutionTime
    }

    boolean getVideoRecording() {
        videoRecording
    }

    boolean getPerformanceMonitoring() {
        performanceMonitoring
    }

}
