package com.example.blockchain.record.keeping.aspect;

public class AuditingContext {
    private static final ThreadLocal<String> descriptionHolder = new ThreadLocal<>();

    public static void setDescription(String description) {
        descriptionHolder.set(description);
    }

    public static String getDescription() {
        return descriptionHolder.get();
    }

    public static void clear() {
        descriptionHolder.remove();
    }
}
