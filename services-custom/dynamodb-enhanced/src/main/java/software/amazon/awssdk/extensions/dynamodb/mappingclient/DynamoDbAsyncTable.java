/*
 * Copyright 2010-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.awssdk.extensions.dynamodb.mappingclient;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.model.GetItemEnhancedRequest;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.model.PutItemEnhancedRequest;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.model.QueryEnhancedRequest;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.model.ScanEnhancedRequest;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.model.UpdateItemEnhancedRequest;

/**
 * Asynchronous interface for running commands against an object that is linked to a specific DynamoDb table resource
 * and therefore knows how to map records from that table into a modelled object.
 *
 * @param <T> The type of the modelled object.
 */
@SdkPublicApi
public interface DynamoDbAsyncTable<T> extends MappedTableResource<T> {
    /**
     * Returns a mapped index that can be used to execute commands against a secondary index belonging to the table
     * being mapped by this object. Note that only a subset of the commands that work against a table will work
     * against a secondary index.
     *
     * @param indexName The name of the secondary index to build the command interface for.
     * @return An {@link DynamoDbAsyncIndex} object that can be used to execute database commands against.
     */
    DynamoDbAsyncIndex<T> index(String indexName);

    default CompletableFuture<Void> createTable(CreateTableEnhancedRequest request) {
        throw new UnsupportedOperationException();
    }

    default CompletableFuture<T> deleteItem(DeleteItemEnhancedRequest<T> request) {
        throw new UnsupportedOperationException();
    }

    default CompletableFuture<T> getItem(GetItemEnhancedRequest request) {
        throw new UnsupportedOperationException();
    }

    default SdkPublisher<Page<T>> query(QueryEnhancedRequest request) {
        throw new UnsupportedOperationException();
    }

    default CompletableFuture<Void> putItem(PutItemEnhancedRequest<T> request) {
        throw new UnsupportedOperationException();
    }

    default SdkPublisher<Page<T>> scan(ScanEnhancedRequest request) {
        throw new UnsupportedOperationException();
    }

    default CompletableFuture<T> updateItem(UpdateItemEnhancedRequest<T> request) {
        throw new UnsupportedOperationException();
    }

}
