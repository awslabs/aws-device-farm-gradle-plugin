/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amazonaws.devicefarm.extension

import org.gradle.api.Project

class DeviceFarmExtension {

    private final Project project

    /**
     * [Required] Name of Device Farm project which contains this application.
     */
    String projectName

    /**
     * [Optional] Name of custom device pool to use.
     * Default: Top Devices
     */
    String devicePool = "Top Devices"

    /**
     * [Optional] Name of Run.
     */
    String runName

    /**
     * You must have a subscription to set this to false
     */
    boolean metered = true

    /**
     * Override the endpoint
     * null == production per AWS SDK Client
     */
    String endpointOverride

    /**
     * Console url template
     * param1 = project arn
     * param2 = run arn
     */
    String consoleUrl = "https://console.aws.amazon.com/devicefarm/home?#/projects/%s/runs/%s"

    /**
    * User-Agent
     */
    String userAgent = "AWS Device Farm - Gradle %s"

    /**
     * Authentication credentials
     */
    Authentication authentication = new Authentication();

    /**
     * Device State configuration
     */
    DeviceState deviceState = new DeviceState()

    /**
     * The configured test to run, 'instrumentation' test is default
     * as it tests the bundled androidTest apk
     */
    ConfiguredTest test = new InstrumentationTest();

    DeviceFarmExtension(final Project project) {
        this.project = project;
    }

    boolean isValid() {
        projectName != null &&
        authentication.valid &&
        test != null && test.valid
    }

    String getRunName(final File testedApk) {
        runName ?: "${testedApk.name} (Gradle)"
    }

    void useMeteredDevices() {
        metered = true
    }

    void useUnmeteredDevices() {
        metered = false
    }

    void authentication(Closure closure) {
        project.configure(authentication, closure)

    }

    void devicestate(final Closure closure) {
        project.configure(deviceState, closure);
    }

    void fuzz(final Closure closure) {
        FuzzTest fuzzTest = new FuzzTest()
        project.configure fuzzTest, closure
        test = fuzzTest
    }

    void instrumentation(final Closure closure) {
        InstrumentationTest instrumentationTest = new InstrumentationTest()
        project.configure instrumentationTest, closure
        test = instrumentationTest
    }

    void calabash(final Closure closure) {
        CalabashTest calabashTest = new CalabashTest()
        project.configure calabashTest, closure
        test = calabashTest
    }

    void uiautomator(final Closure closure) {
        UiAutomatorTest uiAutomatorTest = new UiAutomatorTest()
        project.configure uiAutomatorTest, closure
        test = uiAutomatorTest
    }

    void appium(final Closure closure) {
        AppiumTest appiumTest = new AppiumTest()
        project.configure appiumTest, closure
        test = appiumTest
    }

    void appexplorer(final Closure closuser) {
        AppExplorerTest appExplorerTest = new AppExplorerTest()
        project.configure appExplorerTest, closuser
        test = appExplorerTest

    }
}
