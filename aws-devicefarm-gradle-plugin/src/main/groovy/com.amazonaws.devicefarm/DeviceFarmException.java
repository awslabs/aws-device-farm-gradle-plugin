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
package com.amazonaws.devicefarm;

/**
 * Denotes an error in submitting test run to AWS Device Farm
 */
public class DeviceFarmException extends RuntimeException {

    public DeviceFarmException(String msg) {
        super(msg);
    }

    public DeviceFarmException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DeviceFarmException(Throwable cause) {
        super(cause);
    }
}
