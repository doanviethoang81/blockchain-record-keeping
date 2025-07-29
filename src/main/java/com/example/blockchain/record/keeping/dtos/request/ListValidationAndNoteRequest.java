package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListValidationAndNoteRequest {
    private List<Long> ids;
    private String note;
}
