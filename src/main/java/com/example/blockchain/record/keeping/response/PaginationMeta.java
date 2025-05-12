package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginationMeta {
    private long total;
    private int count;
    private int per_page;
    private int current_page;
    private int total_pages;
}
