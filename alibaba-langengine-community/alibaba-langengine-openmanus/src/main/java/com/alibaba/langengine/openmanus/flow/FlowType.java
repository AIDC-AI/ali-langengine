package com.alibaba.langengine.openmanus.flow;

public enum FlowType {

    PLANNING("planning");

    private final String value;

    FlowType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
