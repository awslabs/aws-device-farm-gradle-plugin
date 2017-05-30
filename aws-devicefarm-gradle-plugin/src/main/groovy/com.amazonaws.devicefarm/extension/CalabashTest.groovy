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

class CalabashTest extends ConfiguredTest implements TestPackageProvider {

    {
        testType = TestType.CALABASH
        this.testPackageUploadType = UploadType.CALABASH_TEST_PACKAGE
    }


    String tags
    String profile

    void tags(String val) { tags = val }

    void profile(String val) { profile = val }

    @Override
    Map<String, String> getTestParameters() {
        [tags: tags, profile: profile]
    }

    @Override
    boolean isValid() { TestPackageProvider.super.isValid() }

}
