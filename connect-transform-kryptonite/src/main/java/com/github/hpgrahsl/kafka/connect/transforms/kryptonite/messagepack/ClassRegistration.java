package com.github.hpgrahsl.kafka.connect.transforms.kryptonite.messagepack;

public class ClassRegistration {

    private String className;
    private int id;
    public ClassRegistration(Class<?> clazz, int id) {
        this.className = clazz.getName();
        this.id = id;
    }

    public String getClassName() {
        return className;
    }
    public int getId() {
        return id;
    }
}
