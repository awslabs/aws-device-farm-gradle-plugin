package com.amazonaws.devicefarm;

import com.amazonaws.services.devicefarm.AWSDeviceFarmClient;

public interface DeviceFarmServerDependencies {

    AWSDeviceFarmClient createDeviceFarmClient();

    DeviceFarmUploader createDeviceFarmUploader(AWSDeviceFarmClient client);

    DeviceFarmUtils createDeviceFarmUtils(AWSDeviceFarmClient client);
}
