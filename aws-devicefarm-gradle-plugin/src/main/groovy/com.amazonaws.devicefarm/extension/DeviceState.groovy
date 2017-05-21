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
package com.amazonaws.devicefarm.extension

import com.amazonaws.services.devicefarm.model.Location
import com.amazonaws.services.devicefarm.model.Radios
import org.gradle.api.file.FileCollection

/**
 * DeviceState parameters
 */
class DeviceState {

    File extraDataZipFile
    List<File> auxiliaryApps = Collections.emptyList()
    def wifiOn = true;
    def bluetoothOn = true;
    def gpsOn = true;
    def nfcOn = true;

    def latitude = 47.6204;
    def longitude = -122.3491;

    def locale = Locale.US

    //These methods make the '=' optional when configuring the plugin
    void extraDataZipFile(File val) { extraDataZipFile = val }

    void auxiliaryApps(FileCollection val) { auxiliaryApps = val as List }

    void wifi(String onOff) { wifiOn = RadioOnOff.valueOf(onOff).bool }

    void bluetooth(String onOff) { bluetoothOn = RadioOnOff.valueOf(onOff).bool }

    void gps(String onOff) { gpsOn = RadioOnOff.valueOf(onOff).bool }

    void nfc(String onOff) { nfcOn = RadioOnOff.valueOf(onOff).bool }

    void latitude(double d) { latitude = d }

    void longitude(double d) { longitude = d }

    Location getLocation() {

        def location = new Location();

        location.latitude = latitude;
        location.longitude = longitude;

        location;
    }

    Radios getRadios() {

        def radios = new Radios();

        radios.bluetooth = bluetoothOn;
        radios.gps = gpsOn;
        radios.nfc = nfcOn;
        radios.wifi = wifiOn;

        radios;

    }
}
