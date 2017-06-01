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
import com.amazonaws.services.devicefarm.AWSDeviceFarmClient;
import com.amazonaws.services.devicefarm.model.*;
import com.android.build.gradle.AppExtension;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Verifications;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.logging.Logger;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;


public class DeviceFarmPluginTest {

    @Injectable
    AWSDeviceFarm apiMock;

    @Injectable
    DeviceFarmUploader uploaderMock;

    @Injectable
    Logger loggerMock;

    Project gradleProject = ProjectBuilder.builder().build();

    @Test
    public void deviceFarmPluginAddsTestServerToProject() {

        gradleProject.getPluginManager().apply("com.android.application");
        gradleProject.getPluginManager().apply("devicefarm");

        assertTrue(((AppExtension) gradleProject.getExtensions().findByName("android")).getTestServers().get(0)
                instanceof DeviceFarmServer);

    }

    @Test
    public void instrumentationTest(@Injectable File testPackage, @Injectable File testedApp) throws IOException {

        final ListProjectsResult projectList = new ListProjectsResult();
        final com.amazonaws.services.devicefarm.model.Project myProject = new com.amazonaws.services.devicefarm.model.Project()
                .withName("MyProject")
                .withArn("1234");

        projectList.setProjects(Arrays.asList(myProject));

        final ListDevicePoolsResult devicePoolList = new ListDevicePoolsResult();
        devicePoolList.setDevicePools(Arrays.asList(new DevicePool().withName("Top Devices").withArn("1234")));

        DeviceFarmExtension extension = new DeviceFarmExtension(gradleProject);
        extension.setProjectName("MyProject");

        DeviceFarmServer server = new DeviceFarmServer(extension, loggerMock, apiMock, uploaderMock, new DeviceFarmUtils(apiMock, extension));

        final ScheduleRunResult runResult = new ScheduleRunResult();
        runResult.setRun(new Run());
        runResult.getRun().setArn("arn:1:2:3:4:5:runarn/projarn");

        new Expectations() {{

            apiMock.listProjects(new ListProjectsRequest());
            result = projectList;

            apiMock.listDevicePools(new ListDevicePoolsRequest().withArn("1234"));
            result = devicePoolList;

            uploaderMock.upload((File) any, myProject, UploadType.INSTRUMENTATION_TEST_PACKAGE);
            result = new Upload().withArn("arn:instrumentation/test/pkg");

            uploaderMock.upload((File) any, myProject, UploadType.ANDROID_APP);
            result = new Upload().withArn("arn:android/app");

            apiMock.scheduleRun((ScheduleRunRequest) any);
            result = runResult;

        }};


        server.uploadApks("debug", testPackage, testedApp);

        new Verifications() {{

            ScheduleRunRequest runRequest = null;
            apiMock.scheduleRun(runRequest = withCapture());
            assertNotNull(runRequest);
            assertEquals(runRequest.getTest().getType(), TestType.INSTRUMENTATION.toString());


        }};



    }

}
