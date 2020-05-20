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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.auth.STSSessionCredentialsProvider;
import com.amazonaws.devicefarm.extension.Authentication;
import com.amazonaws.devicefarm.extension.DeviceFarmExtension;
import com.amazonaws.services.devicefarm.AWSDeviceFarm;
import com.amazonaws.services.devicefarm.AWSDeviceFarmClientBuilder;

import org.apache.commons.lang3.RandomStringUtils;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Returns an initialized AWS Device Farm Client
 */
public class DeviceFarmClientFactory {

    private final String pluginVersion;

    public DeviceFarmClientFactory(final Logger logger) {
        this.pluginVersion = readPluginVersion();
        logger.lifecycle("AWS Device Farm Plugin version " + pluginVersion);
    }

    public AWSDeviceFarm initializeApiClient(final DeviceFarmExtension extension) {
        final AWSDeviceFarmClientBuilder clientBuilder = AWSDeviceFarmClientBuilder.standard();
        final Authentication authentication = extension.getAuthentication();
        final AWSCredentialsProvider credentialsProvider = getAwsCredentialsProvider(authentication);
        final ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withUserAgentSuffix(String.format(extension.getUserAgent(), pluginVersion));
        return clientBuilder.withCredentials(credentialsProvider)
                            .withClientConfiguration(clientConfiguration)
                            .withRegion("us-west-2")
                            .build();
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

    private AWSCredentialsProvider getAwsCredentialsProvider(Authentication authentication) {
        AWSCredentialsProvider credentialsProvider;
        if (authentication != null && authentication.isValid()) {
            if (authentication.getRoleArn() != null) {
                credentialsProvider = new STSAssumeRoleSessionCredentialsProvider
                        .Builder(authentication.getRoleArn(), RandomStringUtils.randomAlphanumeric(8))
                        .build();
            } else {
                BasicAWSCredentials credentials = new BasicAWSCredentials(authentication.getAccessKey(), authentication.getSecretKey());
                credentialsProvider = new STSSessionCredentialsProvider(credentials);
            }
        } else {
            credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        }
        return credentialsProvider;
    }

    
}