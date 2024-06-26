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

package software.amazon.awssdk.core.internal.util;

import java.io.IOException;
import java.io.InputStream;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.core.exception.Crc32MismatchException;
import software.amazon.awssdk.core.io.SdkFilterInputStream;
import software.amazon.awssdk.utils.Logger;

/**
 * Wraps the provided input stream with a {@link Crc32ChecksumCalculatingInputStream} and after the stream is closed
 * will validate the calculated checksum against the actual checksum.
 */
@SdkInternalApi
public class Crc32ChecksumValidatingInputStream extends SdkFilterInputStream {
    private static final Logger log = Logger.loggerFor(Crc32ChecksumValidatingInputStream.class);

    private final long expectedChecksum;

    private boolean shouldValidateChecksum = true;

    /**
     * @param in               Input stream to content.
     * @param expectedChecksum Expected CRC32 checksum returned by the service.
     */
    public Crc32ChecksumValidatingInputStream(InputStream in, long expectedChecksum) {
        super(new Crc32ChecksumCalculatingInputStream(in));
        this.expectedChecksum = expectedChecksum;
    }

    /**
     * Closes the underlying stream and validates the calculated checksum against the expected.
     *
     * @throws Crc32MismatchException If the calculated CRC32 checksum does not match the expected.
     */
    @Override
    public void close() throws IOException {
        try {
            validateChecksum();
        } finally {
            super.close();
        }
    }

    @Override
    public int read() throws IOException {
        try {
            return super.read();
        } catch (Throwable e) {
            shouldValidateChecksum = false;
            throw e;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return super.read(b, off, len);
        } catch (Throwable e) {
            shouldValidateChecksum = false;
            throw e;
        }
    }

    private void validateChecksum() throws Crc32MismatchException {
        if (!shouldValidateChecksum) {
            log.debug(() -> "Skipping CRC32 validation due to error encountered while reading from input stream");
            return;
        }
        long actualChecksum = ((Crc32ChecksumCalculatingInputStream) in).getCrc32Checksum();
        if (expectedChecksum != actualChecksum) {
            throw Crc32MismatchException.builder()
                                        .message(String.format("Expected %d as the Crc32 checksum but the actual " +
                                                 "calculated checksum was %d", expectedChecksum, actualChecksum))
                                        .build();
        }
    }
}
