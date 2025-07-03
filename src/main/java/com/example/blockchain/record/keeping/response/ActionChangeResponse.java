package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ActionChangeResponse {
    private Long id;
    private String fieldName;
    private String oldValue;
    private String newValue;
}
