package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListValidationRequest {
    private List<Long> ids;
}
