package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.TransactionDTO;
import com.example.blockchain.record.keeping.dtos.request.WalletInfoDTO;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.services.AlchemyService;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalletController {
    private final AlchemyService alchemyService;

    @GetMapping("/pdt/transactions")
    public ResponseEntity<?> getWalletTransactions(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        String address = EnvUtil.get("METAMASK_ADDRESS");
        if (address == null || address.isBlank()) {
            return ApiResponseBuilder.badRequest("Địa chỉ ví không hợp lệ!");
        }

        if (!List.of("in", "out", "all").contains(type.toLowerCase())) {
            return ApiResponseBuilder.badRequest("Type không hợp lệ!");
        }

        int offset = (page - 1) * size;

        PaginatedData<TransactionDTO> data = alchemyService.getAllTransactions(address, type, offset, size);
        return ApiResponseBuilder.success("Lấy thành công lịch sử giao dịch", data);
    }

    @GetMapping("/pdt/wallet-info")
    public ResponseEntity<?> getWalletInfo() {
        String address = EnvUtil.get("METAMASK_ADDRESS");
        try {
            WalletInfoDTO info = alchemyService.getWalletInfo(address);
            return ApiResponseBuilder.success("Lấy thông tin ví thành công", info);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Không thể lấy thông tin ví!");
        }
    }
}
