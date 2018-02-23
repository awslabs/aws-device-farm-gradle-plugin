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
package com.amazonaws.devicefarm

import com.amazonaws.devicefarm.extension.DeviceFarmExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeviceFarmPlugin implements Plugin<Project> {

    public static final String PLUGIN_NAME = 'devicefarm'

    @Override
    void apply(Project project) {

        if (!project.plugins.hasPlugin('android') &&
                !project.plugins.hasPlugin('android-library')) {
            throw new GradleException('The android or android-library has not been applied yet')
        }

        DeviceFarmExtension extension = project.extensions.create(PLUGIN_NAME, DeviceFarmExtension, project)

        project.android.testServer(
                new DeviceFarmServer(extension, project.android.logger,
                        new DeviceFarmServerDependenciesImpl(extension, project.android.logger)))
    }
}