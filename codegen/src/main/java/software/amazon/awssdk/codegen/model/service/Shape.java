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

package software.amazon.awssdk.codegen.model.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Shape {
    private String type;

    private Map<String, Member> members = Collections.emptyMap();

    private String documentation;

    private List<String> required;

    private List<String> enumValues;

    private String payload;

    private boolean flattened;

    private boolean synthetic;

    private boolean exception;

    private boolean streaming;

    private boolean requiresLength;

    private boolean wrapper;

    private Member listMember;

    private Member mapKeyType;

    private Member mapValueType;

    private ErrorTrait error;

    private long min;

    private long max;

    private String pattern;

    private boolean fault;

    private boolean deprecated;
    
    private String deprecatedMessage;

    private boolean eventstream;

    private boolean event;

    private String timestampFormat;

    private boolean sensitive;

    private XmlNamespace xmlNamespace;

    private boolean document;

    private boolean union;

    private RetryableTrait retryable;

    public boolean isFault() {
        return fault;
    }

    public void setFault(boolean fault) {
        this.fault = fault;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Member> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Member> members) {
        this.members = members;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    /**
     * The actual JSON value of "enumValues".
     */
    public void setEnum(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isFlattened() {
        return flattened;
    }

    public void setFlattened(boolean flattened) {
        this.flattened = flattened;
    }

    /**
     * Returns flag that indicates whether this shape is a custom SDK shape. If true, this shape will be excluded from the static
     * SdkFields, preventing it from being marshalled.
     */
    public boolean isSynthetic() {
        return synthetic;
    }

    /**
     * Sets flag that indicates whether this shape is a custom SDK shape. If true, this shape will be excluded from the static
     * SdkFields, preventing it from being marshalled.
     */
    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    public boolean isException() {
        return exception;
    }

    public void setException(boolean exception) {
        this.exception = exception;
    }

    public Member getMapKeyType() {
        return mapKeyType;
    }

    public void setMapKeyType(Member mapKeyType) {
        this.mapKeyType = mapKeyType;
    }

    /**
     * The actual JSON name of "mapKeyType".
     */
    public void setKey(Member key) {
        this.mapKeyType = key;
    }

    public Member getMapValueType() {
        return mapValueType;
    }

    public void setMapValueType(Member mapValueType) {
        this.mapValueType = mapValueType;
    }

    /**
     * The actual JSON name of "mapValueType".
     */
    public void setValue(Member value) {
        this.mapValueType = value;
    }

    public Member getListMember() {
        return listMember;
    }

    public void setListMember(Member listMember) {
        this.listMember = listMember;
    }

    /**
     * The actual JSON name of "listMember".
     */
    public void setMember(Member listMember) {
        this.listMember = listMember;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    public boolean isRequiresLength() {
        return requiresLength;
    }

    public void setRequiresLength(boolean requiresLength) {
        this.requiresLength = requiresLength;
    }

    public boolean isWrapper() {
        return wrapper;
    }

    public void setWrapper(boolean wrapper) {
        this.wrapper = wrapper;
    }

    public ErrorTrait getError() {
        return error;
    }

    public void setError(ErrorTrait error) {
        this.error = error;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getDeprecatedMessage() {
        return deprecatedMessage;
    }

    public void setDeprecatedMessage(String deprecatedMessage) {
        this.deprecatedMessage = deprecatedMessage;
    }

    public boolean isEventstream() {
        return eventstream;
    }

    public void setEventstream(boolean eventstream) {
        this.eventstream = eventstream;
    }

    public boolean isEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public XmlNamespace getXmlNamespace() {
        return xmlNamespace;
    }

    public void setXmlNamespace(XmlNamespace xmlNamespace) {
        this.xmlNamespace = xmlNamespace;
    }

    public boolean isDocument() {
        return document;
    }

    public void setDocument(boolean document) {
        this.document = document;
    }

    public boolean isUnion() {
        return union;
    }

    public void setUnion(boolean union) {
        this.union = union;
    }

    public void setRetryable(RetryableTrait retryable) {
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable != null;
    }

    public boolean isThrottling() {
        return retryable != null && retryable.isThrottling();
    }
}
