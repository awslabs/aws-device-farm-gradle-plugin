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
package com.amazonaws.devicefarm.extension

import com.amazonaws.services.devicefarm.model.TestType

/**
 * Any configured test types must implement
 */
abstract class ConfiguredTest {

    TestType testType

    static final String APPIUM_VERSION_1_6_5 = "1.6.5"
    static final String APPIUM_VERSION_1_6_3 = "1.6.3"
    static final String APPIUM_VERSION_1_4_16 = "1.4.16"

    /**
     * Provide API ready test parameters
     * @return test parameter map
     */
    Map<String, String> getTestParameters() { [:] }

    /**
     * @return true if configuration is valid
     */
    boolean valid = true

    /**
     * If this test has a filter return it.
     * @return the filter, or null if not specified
     */
    String getFilter() {

        if (this instanceof HasFilter) {
            return ((HasFilter) this).getFilter();
        }

        return null;
    }

    /**
     * If this test has a appium Version return it.
     * @return the filter, or null if not specified
     */
    String getAppiumVersion() {

        if (this instanceof HasAppiumVersion) {
            return ((HasAppiumVersion) this).getAppiumVersion();
        }
        
        return APPIUM_VERSION_1_6_5;
    }


}
