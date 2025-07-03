package com.github.hpgrahsl.kafka.connect.transforms.kryptonite;

public class EncryptedDataWrapper {
    private Object data;

    public EncryptedDataWrapper() {
        // Default constructor
    }

    public EncryptedDataWrapper(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
