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

package software.amazon.awssdk.enhanced.dynamodb.functionaltests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primaryPartitionKey;

import java.util.Objects;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;

public class AsyncUpdateItemWithResponseTest extends LocalDynamoDbAsyncTestBase {
    private static class Record {
        private Integer id;
        private String stringAttr1;

        private Integer getId() {
            return id;
        }

        private Record setId(Integer id) {
            this.id = id;
            return this;
        }

        private String getStringAttr1() {
            return stringAttr1;
        }

        private Record setStringAttr1(String stringAttr1) {
            this.stringAttr1 = stringAttr1;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Record record = (Record) o;
            return Objects.equals(id, record.id) && Objects.equals(stringAttr1, record.stringAttr1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, stringAttr1);
        }
    }

    private static final TableSchema<Record> TABLE_SCHEMA =
        StaticTableSchema.builder(Record.class)
                         .newItemSupplier(Record::new)
                         .addAttribute(Integer.class, a -> a.name("id_1")
                                                            .getter(Record::getId)
                                                            .setter(Record::setId)
                                                            .tags(primaryPartitionKey()))
                         .addAttribute(String.class, a -> a.name("stringAttr1")
                                                           .getter(Record::getStringAttr1)
                                                           .setter(Record::setStringAttr1))
                         .build();

    private DynamoDbEnhancedAsyncClient enhancedClient;

    private DynamoDbAsyncTable<Record> mappedTable1;

    @Before
    public void createTable() {
        enhancedClient = DynamoDbEnhancedAsyncClient.builder()
                                               .dynamoDbClient(getDynamoDbAsyncClient())
                                               .build();

        mappedTable1 = enhancedClient.table(getConcreteTableName("table-name-1"), TABLE_SCHEMA);

        mappedTable1.createTable(r -> r.provisionedThroughput(getDefaultProvisionedThroughput())).join();
    }

    @After
    public void deleteTable() {
        getDynamoDbAsyncClient().deleteTable(DeleteTableRequest.builder()
                                                          .tableName(getConcreteTableName("table-name-1"))
                                                          .build())
                                .join();
    }

    @Test
    public void newValuesMapped() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)).join();

        assertThat(response.attributes()).isEqualTo(updated);
    }

    @Test
    public void returnConsumedCapacity_unset_consumedCapacityNull() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)).join();

        assertThat(response.consumedCapacity()).isNull();
    }

    @Test
    public void returnConsumedCapacity_set_consumedCapacityNotNull() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)
                                                                                                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL))
                                                                  .join();

        assertThat(response.consumedCapacity()).isNotNull();
    }

    @Test
    public void returnItemCollectionMetrics_unset_itemCollectionMetricsNull() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)).join();

        assertThat(response.itemCollectionMetrics()).isNull();
    }

    @Test
    public void returnValues_Null_attributesMapped() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)
                                                                                                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL))
                                                                  .join();
        AsyncUpdateItemWithResponseTest.Record returned = response.attributes();

        assertThat(returned).isEqualTo(updated);
    }

    @Test
    public void returnValues_allOld_attributesMapped() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)
                                                                                                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                                                                      .returnValues(ReturnValue.ALL_OLD))
                                                                  .join();

       AsyncUpdateItemWithResponseTest.Record returned = response.attributes();

        assertThat(returned).isEqualTo(original);
    }

    @Test
    public void returnValues_allNew_attributesMapped() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)
                                                                                                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                                                                                                .returnValues(ReturnValue.ALL_NEW))
                                                                  .join();
        AsyncUpdateItemWithResponseTest.Record returned = response.attributes();

        assertThat(returned).isEqualTo(updated);
    }

    @Test
    public void returnValues_None_attributesMapped() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)
                                                                                                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                                                                                                .returnValues(ReturnValue.NONE))
                                                                  .join();
        AsyncUpdateItemWithResponseTest.Record returned = response.attributes();

        assertNull(returned);
    }

    @Test
    public void returnValues_Updated_Old_attributesMapped() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)
                                                                                                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                                                                                                .returnValues(ReturnValue.UPDATED_OLD))
                                                                  .join();
        AsyncUpdateItemWithResponseTest.Record returned = response.attributes();
        assertThat(returned.getStringAttr1()).isEqualTo(original.stringAttr1);
    }

    @Test
    public void returnValues_Updated_New_attributesMapped() {
        Record original = new Record().setId(1).setStringAttr1("attr");
        mappedTable1.putItem(original).join();

        Record updated = new Record().setId(1).setStringAttr1("attr2");

        UpdateItemEnhancedResponse<Record> response = mappedTable1.updateItemWithResponse(r -> r.item(updated)
                                                                                                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                                                                                                .returnValues(ReturnValue.UPDATED_NEW))
                                                                  .join();
        AsyncUpdateItemWithResponseTest.Record returned = response.attributes();
        assertThat(returned.getStringAttr1()).isEqualTo(updated.getStringAttr1());
    }
}
