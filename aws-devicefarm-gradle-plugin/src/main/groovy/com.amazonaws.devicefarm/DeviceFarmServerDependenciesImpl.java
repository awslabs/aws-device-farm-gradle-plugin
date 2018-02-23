package com.amazonaws.devicefarm;

import com.amazonaws.devicefarm.extension.DeviceFarmExtension;
import com.amazonaws.services.devicefarm.AWSDeviceFarmClient;
import org.gradle.api.logging.Logger;

public class DeviceFarmServerDependenciesImpl implements DeviceFarmServerDependencies {

    private final DeviceFarmExtension extension;

    private final Logger logger;

    private final DeviceFarmClientFactory deviceFarmClientFactory;

    public DeviceFarmServerDependenciesImpl(DeviceFarmExtension extension, Logger logger) {
        this.extension = extension;
        this.logger = logger;
        this.deviceFarmClientFactory = new DeviceFarmClientFactory(logger);
    }

    @Override
    public AWSDeviceFarmClient createDeviceFarmClient() {
        return deviceFarmClientFactory.initializeApiClient(extension);
    }

    @Override
    public DeviceFarmUploader createDeviceFarmUploader(AWSDeviceFarmClient client) {
        return new DeviceFarmUploader(client, logger);
    }

    @Override
    public DeviceFarmUtils createDeviceFarmUtils(AWSDeviceFarmClient client) {
        return new DeviceFarmUtils(client, extension);
    }
}
