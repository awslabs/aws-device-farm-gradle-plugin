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


import com.amazonaws.services.devicefarm.model.UploadType

/**
 * Models a test that requires an accompanying test package
 */
trait TestPackageProvider {

    /**
     * returns the upload type for the test apk
     */
    UploadType testPackageUploadType

    /**
     * returns the upload type for the test spec file
     */
    UploadType testSpecUploadType

    File testPackage = null

    void tests(File val) { testPackage = val }

    /**
     * Retrieve the test apk file
     * @param defaultTestPackage This is the one provided by Gradle, depending on the test framework it
     * may be something else (like in Calabash)
     * @return the test Apk
     */
    File resolveTestPackage(File defaultTestPackage) { testPackage ?: defaultTestPackage }


    boolean isValid() {
        testPackage != null && testPackage.canRead()
    }
}
