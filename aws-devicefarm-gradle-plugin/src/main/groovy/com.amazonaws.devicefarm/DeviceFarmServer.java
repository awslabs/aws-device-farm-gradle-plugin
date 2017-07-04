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

import com.amazonaws.devicefarm.extension.DeviceFarmExtension;
import com.amazonaws.devicefarm.extension.TestPackageProvider;
import com.amazonaws.services.devicefarm.AWSDeviceFarm;
import com.amazonaws.services.devicefarm.AWSDeviceFarmClient;
import com.amazonaws.services.devicefarm.model.BillingMethod;
import com.amazonaws.services.devicefarm.model.DevicePool;
import com.amazonaws.services.devicefarm.model.Project;
import com.amazonaws.services.devicefarm.model.ScheduleRunConfiguration;
import com.amazonaws.services.devicefarm.model.ScheduleRunRequest;
import com.amazonaws.services.devicefarm.model.ScheduleRunResult;
import com.amazonaws.services.devicefarm.model.ScheduleRunTest;
import com.amazonaws.services.devicefarm.model.Upload;
import com.amazonaws.services.devicefarm.model.UploadType;
import com.amazonaws.services.devicefarm.model.ExecutionConfiguration;
import com.amazonaws.services.devicefarm.model.TestType;
import com.android.builder.testing.api.TestServer;
import com.google.common.collect.Lists;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Sends a test run request to AWS Device Farm.
 * The url to check for results will be printed to console.
 */
public class DeviceFarmServer extends TestServer {

    private final DeviceFarmExtension extension;
    private final Logger logger;
    private final AWSDeviceFarm api;
    private final DeviceFarmUploader uploader;
    private final DeviceFarmUtils utils;

    public DeviceFarmServer(final DeviceFarmExtension extension,
                            final Logger logger, final AWSDeviceFarmClient deviceFarmClient) throws IOException {

        this(extension, logger, deviceFarmClient,
                new DeviceFarmUploader(deviceFarmClient, logger),
                new DeviceFarmUtils(deviceFarmClient, extension));
    }

    public DeviceFarmServer(final DeviceFarmExtension extension,
                            final Logger logger, final AWSDeviceFarm deviceFarmClient,
                            final DeviceFarmUploader uploader,
                            final DeviceFarmUtils utils) throws IOException {

        this.extension = extension;
        this.logger = logger;
        this.api = deviceFarmClient;
        this.uploader = uploader;
        this.utils = utils;
    }


    /**
     * Name of the gradle plugin.
     *
     * @return devicefarm
     */
    @Override
    public String getName() {
        return DeviceFarmPlugin.PLUGIN_NAME;
    }

    /**
     * Upload and test the newly built apk.
     *
     * @param variantName variant of the latest build. Ex: 'debug'
     * @param testPackage File object to the newly built APK which contains tests
     * @param testedApk   File object to the newly built application APK
     */
    @Override
    public void uploadApks(final String variantName, final File testPackage, final File testedApk) {

        final Project project = utils.findProjectByName(extension.getProjectName());
        logger.lifecycle(String.format("Using Project \"%s\", \"%s\"", project.getName(), project.getArn()));

        final DevicePool devicePool = utils.findDevicePoolByName(project, extension.getDevicePool());
        logger.lifecycle(String.format("Using Device Pool \"%s\", \"%s\"", devicePool.getName(), devicePool.getArn()));

        final String appArn = uploader.upload(testedApk, project, UploadType.ANDROID_APP).getArn();
        logger.lifecycle(String.format("Will test app in  \"%s\", \"%s\"", testedApk.getName(), appArn));

        final Collection<Upload> auxApps = uploadAuxApps(project);

        final String extraDataArn = uploadExtraDataZip(project);

        final ScheduleRunTest runTest = new ScheduleRunTest()
                .withParameters(extension.getTest().getTestParameters())
                .withType(extension.getTest().getTestType())
                .withFilter(extension.getTest().getFilter())
                .withTestPackageArn(uploadTestPackageIfNeeded(project, testPackage));

        if (!extension.getExecutionConfiguration().getVideoRecording()) {
            runTest.addParametersEntry("video_recording", "false");
        }

        if (!extension.getExecutionConfiguration().getPerformanceMonitoring()) {
            runTest.addParametersEntry("app_performance_monitoring", "false");
        }

        if (extension.getTest().getTestType().equals(TestType.APPIUM_JAVA_JUNIT) || extension.getTest().getTestType().equals(TestType.APPIUM_JAVA_TESTNG) || extension.getTest().getTestType().equals(TestType.APPIUM_PYTHON)) {
            runTest.addParametersEntry("appium_version", extension.getTest().getAppiumVersion());
            logger.lifecycle(String.format("The Appium version used for the test: %s", extension.getTest().getAppiumVersion()));
        }

        final ScheduleRunConfiguration configuration = new ScheduleRunConfiguration()
                .withAuxiliaryApps(getAuxAppArns(auxApps))
                .withExtraDataPackageArn(extraDataArn)
                .withLocale(extension.getDeviceState().getLocale().toString())
                .withLocation(extension.getDeviceState().getLocation())
                .withBillingMethod(extension.isMetered() ? BillingMethod.METERED : BillingMethod.UNMETERED)
                .withRadios(extension.getDeviceState().getRadios());

        final ExecutionConfiguration exeConfiguration = new ExecutionConfiguration()
                .withJobTimeoutMinutes(extension.getExecutionConfiguration().getMaxExecutionTime());

        final ScheduleRunRequest request = new ScheduleRunRequest()
                .withAppArn(appArn)
                .withConfiguration(configuration)
                .withDevicePoolArn(devicePool.getArn())
                .withProjectArn(project.getArn())
                .withTest(runTest)
                .withExecutionConfiguration(exeConfiguration)
                .withName(String.format("%s (Gradle)", testedApk.getName()));

        final ScheduleRunResult response = api.scheduleRun(request);

        logger.lifecycle(String.format("View the %s run in the AWS Device Farm Console: %s",
                runTest.getType(), utils.getRunUrlFromArn(response.getRun().getArn())));
    }

    /**
     * If the tests requires it upload the test package and return the arn.
     *
     * @param project     the Device Farm project
     * @param testPackage the test package
     * @return test package arn, or null if test does not require a test package
     */
    private String uploadTestPackageIfNeeded(final Project project, final File testPackage) {

        String testArtifactsArn = null;
        if (extension.getTest() instanceof TestPackageProvider) {

            final TestPackageProvider testPackageProvider = (TestPackageProvider) extension.getTest();

            final File testArtifacts = testPackageProvider.resolveTestPackage(testPackage);

            testArtifactsArn = uploader.upload(testArtifacts,
                    project, testPackageProvider.getTestPackageUploadType()).getArn();

            logger.lifecycle(String.format("Will run tests in %s, %s",
                    testArtifacts.getName(), testArtifactsArn));

        }

        return testArtifactsArn;
    }

    private Collection<Upload> uploadAuxApps(final Project project) {

        final Collection<Upload> auxApps = uploader.batchUpload(extension.getDeviceState().getAuxiliaryApps(),
                project, UploadType.ANDROID_APP);

        if (auxApps == null || auxApps.size() == 0) {
            return null;
        }

        for (Upload auxApp : auxApps) {
            logger.lifecycle(String.format("Will install additional app %s, %s",
                    auxApp.getName(), auxApp.getArn()));
        }

        return auxApps;
    }

    private String uploadExtraDataZip(final Project project) {

        final File extraDataZip = extension.getDeviceState().getExtraDataZipFile();

        String extraDataArn = null;

        if (extraDataZip != null) {

            extraDataArn = uploader.upload(
                    extraDataZip,
                    project, UploadType.EXTERNAL_DATA).getArn();

            logger.lifecycle(String.format("Will copy data from zip %s, %s",
                    extraDataZip, extraDataArn));
        }

        return extraDataArn;
    }

    private List<String> getAuxAppArns(Collection<Upload> auxUploads) {
        List<String> auxAppArns = Lists.newArrayList();

        if (auxUploads == null || auxUploads.size() == 0) {
            return auxAppArns;
        }

        for (Upload auxApp : auxUploads) {
            auxAppArns.add(auxApp.getArn());
        }

        return auxAppArns;
    }

    /**
     * Verify the Device Farm extension is properly configured.
     *
     * @return true if configuration is valid, false otherwise.
     */
    @Override
    public boolean isConfigured() {

        final boolean configured = extension.isValid();

        logger.lifecycle(String.format("AWS Device Farm configuration is %s", configured ? "VALID" : "NOT VALID"));

        return configured;

    }


}
