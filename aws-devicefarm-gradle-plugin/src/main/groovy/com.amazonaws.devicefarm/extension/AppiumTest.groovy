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
import com.amazonaws.services.devicefarm.model.UploadType

/**
 * JUNIT by default.
 */
class AppiumTest extends ConfiguredTest implements TestPackageProvider, HasAppiumVersion {

    {
        testType = TestType.APPIUM_JAVA_JUNIT
        this.testPackageUploadType = UploadType.APPIUM_JAVA_JUNIT_TEST_PACKAGE
    }

    /**
     * Configure for testng
     */
    void useTestNG() {
        testType = TestType.APPIUM_JAVA_TESTNG
        this.testPackageUploadType = UploadType.APPIUM_JAVA_TESTNG_TEST_PACKAGE
    }

    /**
     * Configure for junit
     */
    void useJUnit() {
        testType = TestType.APPIUM_JAVA_JUNIT
        this.testPackageUploadType = UploadType.APPIUM_JAVA_JUNIT_TEST_PACKAGE
    }

    /**
     * Configure for python
     */
    void usePython() {
        testType = TestType.APPIUM_PYTHON
        this.testPackageUploadType = UploadType.APPIUM_PYTHON_TEST_PACKAGE
    }

    @Override
    boolean isValid() { TestPackageProvider.super.isValid() }

}