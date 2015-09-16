/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amazonaws.devicefarm.extension

import com.amazonaws.auth.AWSCredentials

/**
 *
 * Provide AWS authentication credentials
 */
class Authentication implements AWSCredentials{

    /**
     * AWS access key
     */
    String accessKey

    /**
     * AWS secret key
     */
    String secretKey

    /**
     * AWS role ARN
     */
    String roleArn


    //These methods make the '=' optional when configuring the plugin
    void accessKey(String val) { accessKey = val }
    void secretKey(String val) { secretKey = val }
    void roleArn(String val) { roleArn = val }

    @Override
    String getAWSAccessKeyId() {
        accessKey
    }

    @Override
    String getAWSSecretKey() {
        secretKey
    }

    boolean isValid() {
        roleArn  ||
        (secretKey && accessKey)
    }


}
