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

package software.amazon.awssdk.http.auth.internal;

import static software.amazon.awssdk.http.auth.internal.util.CanonicalRequestV2.getCanonicalHeaders;
import static software.amazon.awssdk.http.auth.internal.util.CanonicalRequestV2.getSignedHeadersString;
import static software.amazon.awssdk.http.auth.internal.util.SignerConstant.AWS4_SIGNING_ALGORITHM;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.reactivestreams.Publisher;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.auth.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.SigV4RequestContext;
import software.amazon.awssdk.http.auth.internal.checksums.ContentChecksum;
import software.amazon.awssdk.http.auth.internal.checksums.SdkChecksum;
import software.amazon.awssdk.http.auth.internal.util.CanonicalRequestV2;
import software.amazon.awssdk.http.auth.internal.util.SignerConstant;
import software.amazon.awssdk.http.auth.spi.AsyncSignRequest;
import software.amazon.awssdk.http.auth.spi.SignRequest;
import software.amazon.awssdk.http.auth.spi.SyncSignRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.identity.spi.AwsSessionCredentialsIdentity;
import software.amazon.awssdk.utils.Pair;

/**
 * An implementation of {@link AwsV4HttpSigner} that adds auth to a request via query-params.
 */
@SdkInternalApi
public class DefaultAwsV4QueryHttpSigner implements AwsV4HttpSigner<AwsV4HttpProperties> {

    private final AwsV4HttpSigner<AwsV4HttpProperties> v4Signer;

    public DefaultAwsV4QueryHttpSigner(AwsV4HttpSigner<AwsV4HttpProperties> v4Signer) {
        this.v4Signer = v4Signer;
    }

    @Override
    public SdkChecksum createSdkChecksum(SignRequest<?, ?> signRequest, AwsV4HttpProperties properties) {
        return v4Signer.createSdkChecksum(signRequest, properties);
    }

    @Override
    public ContentChecksum createChecksum(SyncSignRequest<? extends AwsCredentialsIdentity> signRequest,
                                          AwsV4HttpProperties properties) {
        return v4Signer.createChecksum(signRequest, properties);
    }

    @Override
    public CompletableFuture<ContentChecksum> createChecksum(AsyncSignRequest<? extends AwsCredentialsIdentity> signRequest,
                                                             AwsV4HttpProperties properties) {
        return v4Signer.createChecksum(signRequest, properties);
    }

    @Override
    public String createContentHash(ContentStreamProvider payload, SdkChecksum sdkChecksum,
                                    AwsV4HttpProperties properties) {
        return v4Signer.createContentHash(payload, sdkChecksum, properties);
    }

    @Override
    public CompletableFuture<String> createContentHash(Publisher<ByteBuffer> payload, SdkChecksum sdkChecksum,
                                                       AwsV4HttpProperties properties) {
        return v4Signer.createContentHash(payload, sdkChecksum, properties);
    }

    @Override
    public void addPrerequisites(SdkHttpRequest.Builder requestBuilder,
                                 ContentChecksum contentChecksum, AwsV4HttpProperties properties) {
        v4Signer.addPrerequisites(requestBuilder, contentChecksum, properties);

        if (properties.getCredentials() instanceof AwsSessionCredentialsIdentity) {
            requestBuilder.putRawQueryParameter(SignerConstant.X_AMZ_SECURITY_TOKEN,
                ((AwsSessionCredentialsIdentity) properties.getCredentials()).sessionToken());
        }

        List<Pair<String, List<String>>> canonicalHeaders = getCanonicalHeaders(requestBuilder.build());
        requestBuilder.putRawQueryParameter(SignerConstant.X_AMZ_ALGORITHM, AWS4_SIGNING_ALGORITHM);
        requestBuilder.putRawQueryParameter(SignerConstant.X_AMZ_DATE, properties.getCredentialScope().getDatetime());
        requestBuilder.putRawQueryParameter(SignerConstant.X_AMZ_SIGNED_HEADERS, getSignedHeadersString(canonicalHeaders));
        requestBuilder.putRawQueryParameter(SignerConstant.X_AMZ_CREDENTIAL,
            properties.getCredentialScope().scope(properties.getCredentials()));
    }

    @Override
    public CanonicalRequestV2 createCanonicalRequest(SdkHttpRequest request, ContentChecksum contentChecksum,
                                                     AwsV4HttpProperties properties) {
        return v4Signer.createCanonicalRequest(request, contentChecksum, properties);
    }

    @Override
    public String createSignString(String canonicalRequestHash, AwsV4HttpProperties properties) {
        return v4Signer.createSignString(canonicalRequestHash, properties);
    }

    @Override
    public byte[] createSigningKey(AwsV4HttpProperties properties) {
        return v4Signer.createSigningKey(properties);
    }

    @Override
    public String createSignature(String stringToSign, byte[] signingKey, AwsV4HttpProperties properties) {
        return v4Signer.createSignature(stringToSign, signingKey, properties);
    }

    @Override
    public void addSignature(SdkHttpRequest.Builder requestBuilder,
                             CanonicalRequestV2 canonicalRequest,
                             String signature,
                             AwsV4HttpProperties properties) {
        requestBuilder.putRawQueryParameter(SignerConstant.X_AMZ_SIGNATURE, signature);
    }

    @Override
    public ContentStreamProvider processPayload(ContentStreamProvider payload,
                                                SigV4RequestContext v4RequestContext, AwsV4HttpProperties properties) {
        return v4Signer.processPayload(payload, v4RequestContext, properties);
    }

    @Override
    public Publisher<ByteBuffer> processPayload(Publisher<ByteBuffer> payload,
                                                CompletableFuture<SigV4RequestContext> futureV4RequestContext,
                                                AwsV4HttpProperties properties) {
        return v4Signer.processPayload(payload, futureV4RequestContext, properties);
    }

    @Override
    public AwsV4HttpProperties getProperties(SignRequest<?, ? extends AwsCredentialsIdentity> signRequest) {
        return AwsV4HttpProperties.create(signRequest);
    }
}
