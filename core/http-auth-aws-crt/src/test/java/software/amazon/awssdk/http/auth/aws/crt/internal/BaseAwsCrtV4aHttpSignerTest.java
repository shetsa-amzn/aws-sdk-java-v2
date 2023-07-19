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

package software.amazon.awssdk.http.auth.aws.crt.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static software.amazon.awssdk.http.auth.aws.crt.AwsCrtV4aHttpSigner.REGION_NAME;
import static software.amazon.awssdk.http.auth.aws.crt.AwsCrtV4aHttpSigner.SERVICE_SIGNING_NAME;
import static software.amazon.awssdk.http.auth.aws.crt.TestUtils.AnonymousCredentialsIdentity;
import static software.amazon.awssdk.http.auth.aws.crt.TestUtils.generateBasicAsyncRequest;
import static software.amazon.awssdk.http.auth.aws.crt.TestUtils.generateBasicRequest;
import static software.amazon.awssdk.http.auth.aws.crt.TestUtils.getSigningConfig;
import static software.amazon.awssdk.http.auth.aws.crt.TestUtils.verifyEcdsaSignature;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.http.auth.aws.crt.AwsCrtV4aHttpSigner;
import software.amazon.awssdk.http.auth.aws.crt.internal.signer.AwsCrtV4aHttpProperties;
import software.amazon.awssdk.http.auth.aws.crt.internal.signer.BaseAwsCrtV4aHttpSigner;
import software.amazon.awssdk.http.auth.spi.AsyncSignRequest;
import software.amazon.awssdk.http.auth.spi.SyncSignRequest;
import software.amazon.awssdk.http.auth.spi.SyncSignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;

/**
 * Functional tests for the Sigv4a signer. These tests call the CRT native signer code.
 */
public class BaseAwsCrtV4aHttpSignerTest {

    private static final AwsCrtV4aHttpSigner signer = BaseAwsCrtV4aHttpSigner.create();

    @Test
    public void sign_withBasicRequest_shouldSign() {
        AwsCredentialsIdentity credentials =
            AwsCredentialsIdentity.create("AKIDEXAMPLE", "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY");
        String expectedCanonicalRequest = "POST\n" +
            "/\n" +
            "\n" +
            "host:demo.us-east-1.amazonaws.com\n" +
            "x-amz-archive-description:test test\n" +
            "x-amz-date:20200803T174823Z\n" +
            "x-amz-region-set:aws-global\n" +
            "\n" +
            "host;x-amz-archive-description;x-amz-date;x-amz-region-set\n" +
            "a15c8292b1d12abbbbe4148605f7872fbdf645618fee5ab0e8072a7b34f155e2";
        SyncSignRequest<AwsCredentialsIdentity> request = generateBasicRequest(credentials,
            (httpRequest) -> {
                httpRequest.port(443);
            },
            (signRequest) -> {
            }
        );
        AwsSigningConfig expectedConfig = getSigningConfig((BaseAwsCrtV4aHttpSigner<AwsCrtV4aHttpProperties>) signer, request);

        SyncSignedRequest signedRequest = signer.sign(request);

        String authHeader = signedRequest.request().firstMatchingHeader("Authorization").get();
        String signatureKey = "Signature=";
        String signatureValue = authHeader.substring(authHeader.indexOf(signatureKey) + signatureKey.length());

        assertThat(signedRequest.request().firstMatchingHeader("Host")).hasValue("demo.us-east-1.amazonaws.com");
        assertThat(signedRequest.request().firstMatchingHeader("X-Amz-Date")).hasValue("20200803T174823Z");
        assertThat(signedRequest.request().firstMatchingHeader("X-Amz-Region-Set")).hasValue("aws-global");
        verifyEcdsaSignature(request.request(), request.payload().get(), expectedCanonicalRequest, expectedConfig,
            signatureValue);
    }

    @Test
    public void sign_withAnonymousCredentials_shouldNotSign() {
        SyncSignRequest<? extends AwsCredentialsIdentity> request = generateBasicRequest(
            new AnonymousCredentialsIdentity(),
            (httpRequest -> {
            }),
            (signRequest -> {
            })
        );

        SyncSignedRequest signedRequest = signer.sign(request);

        assertNull(signedRequest.request().headers().get("Authorization"));
    }

    @Test
    public void sign_withoutRegionNameProperty_throws() {
        SyncSignRequest<? extends AwsCredentialsIdentity> request = generateBasicRequest(
            AwsCredentialsIdentity.create("access", "secret"),
            (httpRequest -> {
            }),
            (signRequest -> signRequest.putProperty(REGION_NAME, null))
        );

        NullPointerException exception = assertThrows(NullPointerException.class, () -> signer.sign(request));

        assertThat(exception.getMessage()).contains("must not be null");
    }

    @Test
    public void sign_withoutServiceSigningNameProperty_throws() {
        SyncSignRequest<? extends AwsCredentialsIdentity> request = generateBasicRequest(
            AwsCredentialsIdentity.create("access", "secret"),
            (httpRequest -> {
            }),
            (signRequest -> signRequest.putProperty(SERVICE_SIGNING_NAME, null))
        );

        NullPointerException exception = assertThrows(NullPointerException.class, () -> signer.sign(request));

        assertThat(exception.getMessage()).contains("must not be null");
    }

    @Test
    public void signAsync_throwsUnsupportedOperationException() {
        AsyncSignRequest<? extends AwsCredentialsIdentity> request = generateBasicAsyncRequest(
            AwsCredentialsIdentity.create("access", "secret"),
            (httpRequest -> {
            }),
            (signRequest -> {
            })
        );

        assertThrows(UnsupportedOperationException.class, () -> signer.signAsync(request));
    }
}