package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaginationInfo {
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}
