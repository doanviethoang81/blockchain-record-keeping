package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ApiDataResponse<T> {
    private List<T> items;
    private PaginationMeta meta;
}
