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

package software.amazon.awssdk.http.auth.aws.internal.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.http.auth.aws.checksum.SdkChecksum;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Base class for AWS Java SDK specific {@link DigestInputStream}.
 */
@SdkInternalApi
public final class SdkDigestInputStream extends DigestInputStream implements Releasable {
    private static final int SKIP_BUF_SIZE = 2 * 1024;

    private final SdkChecksum sdkChecksum;

    public SdkDigestInputStream(InputStream stream, MessageDigest digest, SdkChecksum sdkChecksum) {
        super(stream, digest);
        this.sdkChecksum = sdkChecksum;
    }

    public SdkDigestInputStream(InputStream stream, MessageDigest digest) {
        this(stream, digest, null);
    }

    // https://github.com/aws/aws-sdk-java/issues/232

    /**
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream, while taking the skipped bytes into account for digest
     * calculation. The <code>skip</code> method may, for a variety of reasons,
     * end up skipping over some smaller number of bytes, possibly
     * <code>0</code>. This may result from any of a number of conditions;
     * reaching end of file before <code>n</code> bytes have been skipped is
     * only one possibility. The actual number of bytes skipped is returned. If
     * <code>n</code> is negative, no bytes are skipped.
     *
     * <p>
     * The <code>skip</code> method of this class creates a byte array and then
     * repeatedly reads into it until <code>n</code> bytes have been read or the
     * end of the stream has been reached. Subclasses are encouraged to provide
     * a more efficient implementation of this method. For instance, the
     * implementation may depend on the ability to seek.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if the stream does not support seek, or if some other I/O
     *                     error occurs.
     */
    @Override
    public long skip(final long n) throws IOException {
        if (n <= 0) {
            return n;
        }
        byte[] b = new byte[(int) Math.min(SKIP_BUF_SIZE, n)];
        long m = n; // remaining number of bytes to read
        while (m > 0) {
            int len = read(b, 0, (int) Math.min(m, b.length));
            if (len == -1) {
                return n - m;
            }
            m -= len;
        }
        assert (m == 0);
        return n;
    }

    @Override
    public void release() {
        // Don't call IOUtils.release(in, null) or else could lead to infinite loop
        IoUtils.closeQuietly(this, null);
        if (in instanceof Releasable) {
            // This allows any underlying stream that has the close operation
            // disabled to be truly released
            Releasable r = (Releasable) in;
            r.release();
        }
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public int read() throws IOException {
        int ch = in.read();
        if (ch != -1) {
            digest.update((byte) ch);
            if (sdkChecksum != null) {
                sdkChecksum.update((byte) ch);
            }

        }
        return ch;
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result != -1) {
            digest.update(b, off, result);
            if (sdkChecksum != null) {
                sdkChecksum.update(b, off, result);
            }
        }
        return result;
    }
}