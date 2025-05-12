package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedData<T> {
    private List<T> items;
    private PaginationMeta meta;
}
