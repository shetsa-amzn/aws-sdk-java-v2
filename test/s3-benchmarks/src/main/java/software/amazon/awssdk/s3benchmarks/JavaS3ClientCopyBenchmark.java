/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.s3benchmarks;

import java.util.List;

public class JavaS3ClientCopyBenchmark extends BaseJavaS3ClientBenchmark {

    public JavaS3ClientCopyBenchmark(TransferManagerBenchmarkConfig config) {
        super(config);
    }

    @Override
    protected void sendOneRequest(List<Double> latencies) throws Exception {

    }

    @Override
    protected long contentLength() throws Exception {
        return s3Client.headObject(b -> b.bucket(bucket).key(key)).contentLength();
    }
}