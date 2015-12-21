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

import com.amazonaws.services.devicefarm.AWSDeviceFarmClient;
import com.amazonaws.services.devicefarm.model.*;
import com.google.common.collect.Lists;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Artifact uploader for AWS Device Farm
 */
public class DeviceFarmUploader {

    private final AWSDeviceFarmClient api;
    private final Logger logger;
    private final ExecutorService uploadExecutor;

    public DeviceFarmUploader(final AWSDeviceFarmClient api, final Logger logger) {
        this.api = api;
        this.logger = logger;
        this.uploadExecutor = Executors.newCachedThreadPool();
    }


    /**
     * Upload a single file, waits for upload to complete.
     * @param file the file
     * @param project the project
     * @param uploadType the upload type
     * @return upload object
     */
    public Upload upload(final File file, final Project project,
                         final UploadType uploadType) {

        if(!(file.exists() && file.canRead())) {
            throw new DeviceFarmException(String.format("File %s does not exist or is not readable", file));
        }

        final CreateUploadRequest appUploadRequest = new CreateUploadRequest()
                .withName(file.getName())
                .withProjectArn(project.getArn())
                .withContentType("application/octet-stream")
                .withType(uploadType.toString());
        final Upload upload = api.createUpload(appUploadRequest).getUpload();

        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final HttpPut httpPut = new HttpPut(upload.getUrl());
        httpPut.setHeader("Content-Type", upload.getContentType());

        final FileEntity entity = new FileEntity(file);
        httpPut.setEntity(entity);

        writeToLog(String.format("Uploading %s to S3", file.getName()));

        final HttpResponse response;
        try {
            response = httpClient.execute(httpPut);
        } catch (IOException e) {
            throw new DeviceFarmException(String.format("Error uploading artifact %s", file), e);
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new DeviceFarmException(String.format("Upload returned non-200 responses: %s", response.getStatusLine().getStatusCode()));
        }

        waitForUpload(file, upload);

        return upload;
    }

    public Collection<Upload> batchUpload(final List<File> artifacts, final Project project, final UploadType uploadType) {

        List<Future<Upload>> futures = Lists.newArrayList();

        // Upload each artifact and create a future for it.
        for (final File file : artifacts) {
            futures.add(uploadExecutor.submit(
                    new Callable<Upload>() {
                        @Override
                        public Upload call() throws Exception {
                            return upload(file, project, uploadType);
                        }
                    }
            ));
        }

        List<Upload> uploads = Lists.newArrayList();

        // Check future results and append the upload results to a list.
        for (Future<Upload> f : futures) {
            try {
                uploads.add(f.get());
            } catch (Exception e) {
                throw new DeviceFarmException(e);
            }
        }

        return uploads;
    }

    private void waitForUpload(final File file, final Upload upload) {

        while (true) {
            GetUploadRequest describeUploadRequest = new GetUploadRequest()
                    .withArn(upload.getArn());
            GetUploadResult describeUploadResult = api.getUpload(describeUploadRequest);
            String status = describeUploadResult.getUpload().getStatus();

            if ("SUCCEEDED".equalsIgnoreCase(status)) {
                break;
            }
            else if ("FAILED".equalsIgnoreCase(status)) {
                throw new DeviceFarmException(String.format("Upload %s failed!", upload.getName()));
            }
            else {
                try {
                    writeToLog(String.format("Waiting for upload %s to be ready (current status: %s)", file.getName(), status));
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private void writeToLog(final String msg) {
        logger.info(msg);
    }

}
