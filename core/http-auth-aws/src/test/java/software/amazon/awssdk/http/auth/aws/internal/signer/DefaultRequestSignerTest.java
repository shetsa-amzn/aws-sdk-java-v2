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

package software.amazon.awssdk.http.auth.aws.internal.signer;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static software.amazon.awssdk.utils.BinaryUtils.toHex;

import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import software.amazon.awssdk.testutils.LogCaptor;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;

public class DefaultRequestSignerTest {

    private V4Properties v4Properties = V4Properties.builder()
                                            .credentials(AwsCredentialsIdentity.create("foo", "bar"))
                                            .credentialScope(new CredentialScope("baz", "qux", Instant.EPOCH))
                                            .signingClock(Clock.fixed(Instant.EPOCH, UTC))
                                            .doubleUrlEncode(true)
                                            .normalizePath(true)
                                            .build();

    @Test
    public void requestSigner_sign_shouldReturnSignedResult_butNotAddAnyAuthInfoToRequest() {
        SdkHttpRequest.Builder request = SdkHttpRequest
            .builder()
            .uri(URI.create("https://localhost"))
            .method(SdkHttpMethod.GET)
            .putHeader("Host", "localhost");
        String expectedContentHash = "quux";
        String expectedSigningKeyHex = "3d558b7a87b67996abc908071e0771a31b2a7977ab247144e60a6cba3356be1f";
        String expectedSignature = "6c1f4222e0888e6e68b20ded382bc80c7312465c69fb52cbd6d6ce2d073533bf";
        String expectedCanonicalRequestString = "GET\n/\n\n"
                                                + "host:localhost\n\n"
                                                + "host\nquux";
        String expectedHost = "localhost";

        try (LogCaptor logCaptor = LogCaptor.create(Level.DEBUG)) {
            DefaultV4RequestSigner requestSigner = new DefaultV4RequestSigner(v4Properties, "quux");
            V4RequestSigningResult requestSigningResult = requestSigner.sign(request);
            assertEquals(expectedContentHash, requestSigningResult.getContentHash());
            assertEquals(expectedSigningKeyHex, toHex(requestSigningResult.getSigningKey()));
            assertEquals(expectedSignature, requestSigningResult.getSignature());
            assertEquals(expectedCanonicalRequestString, requestSigningResult.getCanonicalRequest().getCanonicalRequestString());
            assertEquals(expectedHost, requestSigningResult.getSignedRequest().firstMatchingHeader("Host").orElse(""));
            assertThat(requestSigningResult.getSignedRequest().build()).usingRecursiveComparison().isEqualTo(request.build());
            List<LogEvent> logEvents = logCaptor.loggedEvents();
            assertThat(logEvents).hasSize(3);
            assertThat(logEvents.get(0).getMessage().getFormattedMessage()).contains("AWS4 Canonical Request");
            assertThat(logEvents.get(1).getMessage().getFormattedMessage()).contains("AWS4 Canonical Request Hash");
            assertThat(logEvents.get(2).getMessage().getFormattedMessage()).contains("AWS4 String to sign");
        }
    }
}
