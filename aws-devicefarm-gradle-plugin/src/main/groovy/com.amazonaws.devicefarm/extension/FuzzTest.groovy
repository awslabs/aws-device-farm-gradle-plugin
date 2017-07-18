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
 * Built in Fuzz Test
 */
class FuzzTest extends ConfiguredTest {

    {
        testType = TestType.BUILTIN_FUZZ
    }

    String eventCount = 6000
    String eventThrottle = 50
    String randomizerSeed

    // These methods make the '=' optional when configuring the plugin
    void eventCount(int val) { eventCount = val }

    void eventThrottle(int val) { eventThrottle = val }

    void randomizerSeed(int val) { randomizerSeed = val }


    @Override
    Map<String, String> getTestParameters() {

        [event_count: eventCount, throttle: eventThrottle, seed: randomizerSeed]
    }


}
