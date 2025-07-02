package com.example.blockchain.record.keeping.annotation;

import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    ActionType action();
    Entity entity();
}
