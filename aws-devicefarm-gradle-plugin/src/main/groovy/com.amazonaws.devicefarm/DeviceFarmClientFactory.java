//
// Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.devicefarm.extension.DeviceFarmExtension;
import com.amazonaws.services.devicefarm.AWSDeviceFarmClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Returns an initialized AWS Device Farm Client
 */
public class DeviceFarmClientFactory {

    final String pluginVersion;

    public DeviceFarmClientFactory(final Logger logger) {
        this.pluginVersion = readPluginVersion();
        logger.lifecycle("AWS Device Farm Plugin version " + pluginVersion);
    }

    public AWSDeviceFarmClient initializeApiClient(final DeviceFarmExtension extension) {

        final String roleArn = extension.getAuthentication().getRoleArn();

        AWSCredentials credentials = extension.getAuthentication();

        if (roleArn != null) {
            final STSAssumeRoleSessionCredentialsProvider sts = new STSAssumeRoleSessionCredentialsProvider
                    .Builder(roleArn, RandomStringUtils.randomAlphanumeric(8))
                    .build();
            credentials = sts.getCredentials();
        }

        final ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withUserAgent(String.format(extension.getUserAgent(), pluginVersion));

        AWSDeviceFarmClient apiClient = new AWSDeviceFarmClient(credentials, clientConfiguration);
        apiClient.setServiceNameIntern("devicefarm");
        if (extension.getEndpointOverride() != null) {
            apiClient.setEndpoint(extension.getEndpointOverride());
        }

        return apiClient;

    }

    private static String readPluginVersion() {
        try {

            final Properties props = new Properties();
            props.load(DeviceFarmServer.class.getResourceAsStream("/META-INF/gradle-plugins/version.properties"));
            return props.getProperty("version");

        } catch (IOException e) {
            throw new DeviceFarmException("Unable to read version", e);
        }
    }


}
