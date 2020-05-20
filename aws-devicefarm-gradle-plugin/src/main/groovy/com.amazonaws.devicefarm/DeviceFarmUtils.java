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
import com.amazonaws.services.devicefarm.AWSDeviceFarm;
import com.amazonaws.services.devicefarm.model.DevicePool;
import com.amazonaws.services.devicefarm.model.ListDevicePoolsRequest;
import com.amazonaws.services.devicefarm.model.ListDevicePoolsResult;
import com.amazonaws.services.devicefarm.model.ListProjectsRequest;
import com.amazonaws.services.devicefarm.model.ListProjectsResult;
import com.amazonaws.services.devicefarm.model.ListUploadsRequest;
import com.amazonaws.services.devicefarm.model.ListUploadsResult;
import com.amazonaws.services.devicefarm.model.Project;
import com.amazonaws.services.devicefarm.model.Upload;
import com.amazonaws.services.devicefarm.model.UploadStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains collection of helper functions for common AWS Device Farm actions.
 */
public class DeviceFarmUtils {

    private final DeviceFarmExtension extension;
    private final AWSDeviceFarm api;

    public DeviceFarmUtils(final AWSDeviceFarm api, final DeviceFarmExtension extension) {
        this.extension = extension;
        this.api = api;
    }

    /**
     * Get all Device Farm projects.
     *
     * @return A List of the Device Farm projects.
     */
    public List<Project> getProjects() {

        List<Project> projects = new ArrayList<Project>();
        ListProjectsResult result = api.listProjects(new ListProjectsRequest());
        projects.addAll(result.getProjects());
        while (result.getNextToken() != null) {
            ListProjectsRequest request = new ListProjectsRequest();
            request.setNextToken(result.getNextToken());
            result = api.listProjects(request);
            projects.addAll(result.getProjects());
        }
        return projects;
    }

    /**
     * Get Device Farm uploads for a given Device Farm project.
     *
     * @param project
     *            Device Farm Project.
     * @return A List of the Device Farm uploads.
     */
    public List<Upload> getUploads(Project project) {

        List<Upload> uploads = new ArrayList<Upload>();
        ListUploadsResult result = api.listUploads(new ListUploadsRequest().withArn(project.getArn()));
        uploads.addAll(result.getUploads());
        while (result.getNextToken() != null) {
            ListUploadsRequest request = new ListUploadsRequest();
            request.setNextToken(result.getNextToken());
            result = api.listUploads(request);
            uploads.addAll(result.getUploads());
        }
        return uploads;
    }

    /**
     * Get Device Farm TestSpecs  for a given Device Farm project.
     *
     * @param project Device Farm Project.
     * @return A List of the Device Farm TestSpecs.
     */
    public List<Upload> getTestSpecs(Project project)  {
        List<Upload> allUploads = getUploads(project);
        List<Upload> testSpecUploads = new ArrayList<Upload>();
        for (Upload upload : allUploads) {
            if (upload.getType().contains("TEST_SPEC")
                    && UploadStatus.SUCCEEDED.toString().equals(upload.getStatus())) {
                testSpecUploads.add(upload);

            }
        }
        return testSpecUploads;
    }

    /**
     * Get Device Farm testSpec by name.
     *
     * @param testSpecName String name of the Device Farm testSpec.
     * @param project Device Farm project
     * @return The Device Farm project.
     */
    public Upload findTestSpecByName(final String testSpecName, Project project) {

        if (StringUtils.isBlank(testSpecName)) {
            return null;
        }
        for (Upload upload : getTestSpecs(project)) {
            if (upload.getName().equals(testSpecName)) {
                return upload;
            }
        }

        throw new DeviceFarmException(String.format("testSpec '%s' not found.", testSpecName));
    }

    /**
     * Get Device Farm project by name.
     *
     * @param projectName String name of the Device Farm project.
     * @return The Device Farm project.
     */
    public Project findProjectByName(final String projectName) {

        for (Project p : getProjects()) {
            if (p.getName().equals(projectName)) {
                return p;
            }
        }

        throw new DeviceFarmException(String.format("Project '%s' not found.", projectName));
    }

    /**
     * Get Device Farm device pools for a given Device Farm project.
     *
     * @param project Device Farm Project.
     * @return A List of the Device Farm device pools.
     */
    public List<DevicePool> getDevicePools(final Project project) {

        final ListDevicePoolsResult poolsResult = api.listDevicePools(new ListDevicePoolsRequest().withArn(project.getArn()));

        return poolsResult.getDevicePools();
    }

    /**
     * Get Device Farm device pool by Device Farm project and device pool name.
     *
     * @param project        The Device Farm project.
     * @param devicePoolName String name of the device pool.
     * @return The Device Farm device pool.
     */
    public DevicePool findDevicePoolByName(final Project project, final String devicePoolName) {

        final List<DevicePool> pools = getDevicePools(project);

        for (DevicePool dp : pools) {
            if (dp.getName().equals(devicePoolName)) {
                return dp;
            }
        }

        throw new DeviceFarmException(String.format("DevicePool '%s' not found.", devicePoolName));
    }


    /**
     * Get the Device Farm run URL from the Device Farm run ARN.
     *
     * @param arn The Device Farm run ARN.
     * @return The Device Farm run URL.
     */
    public String getRunUrlFromArn(String arn) {
        String projectId = getProjectIdFromArn(arn);
        String runId = getRunIdFromArn(arn);
        return String.format(extension.getConsoleUrl(), projectId, runId);

    }

    /**
     * Extract the devices arn from the given device pool and return the array of arns.
     * @param devicePool in which the tests going to get triggered.
     * @return array of device arns.
     */
    public String[] getDevicesInDevicePool(DevicePool devicePool) {
        return devicePool.getRules().get(0).getValue()
                         .replace("[\"","")
                         .replace("]\"", "")
                         .replace("\"", "")
                         .split(",");
    }

    /**
     * Get the Device Farm run ID from the Device Farm run ARN.
     *
     * @param arn The Device Farm run ARN.
     * @return The Device Farm run ID.
     */
    public static String getRunIdFromArn(String arn) {
        String[] projectRunId = splitRunArn(arn);
        return projectRunId[1];
    }

    /**
     * Get the Device Farm project ID from the Device Farm run ARN.
     *
     * @param arn The Device Farm run ARN.
     * @return The Device Farm project ID.
     */
    public static String getProjectIdFromArn(String arn) {
        String[] projectRunId = splitRunArn(arn);
        return projectRunId[0];
    }

    /**
     * Split the run ARN into Device Farm run and project IDs.
     *
     * @param arn The Device Farm run ARN.
     * @return An array containing the run and project IDs.
     */
    public static String[] splitRunArn(String arn) {
        // The stuff we care about is in the 7th slot (index = 6)
        return arn.split(":")[6].split("/");
    }
}
