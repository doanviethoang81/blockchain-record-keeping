package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.TransactionDTO;
import com.example.blockchain.record.keeping.dtos.request.WalletInfoDTO;
import com.example.blockchain.record.keeping.dtos.request.WalletSTUInfoDTO;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.Wallet;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.services.AlchemyService;
import com.example.blockchain.record.keeping.services.StudentService;
import com.example.blockchain.record.keeping.services.WalletService;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalletController {
    private final AlchemyService alchemyService;
    private final StudentService studentService;
    private final WalletService walletService;


    @GetMapping("/pdt/transactions")
    public ResponseEntity<?> getWalletTransactions(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        String toContract = EnvUtil.get("SMART_CONTRACT_CERTIFICATE_ADDRESS");
        String address = EnvUtil.get("METAMASK_ADDRESS");
        if (address == null || address.isBlank()) {
            return ApiResponseBuilder.badRequest("Địa chỉ ví không hợp lệ!");
        }

        if (!List.of("in", "out", "all").contains(type.toLowerCase())) {
            return ApiResponseBuilder.badRequest("Type không hợp lệ!");
        }

        int offset = (page - 1) * size;

        PaginatedData<TransactionDTO> data = alchemyService.getAllTransactions(address,toContract, type, offset, size);
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

    @GetMapping("/pdt/wallet-info-stu")
    public ResponseEntity<?> getWalletSTUInfo() {
        String address = EnvUtil.get("METAMASK_ADDRESS");
        try {
            WalletSTUInfoDTO info = alchemyService.getWalletInfoSTU(address);
            return ApiResponseBuilder.success("Lấy thông tin ví thành công", info);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Không thể lấy thông tin ví!");
        }
    }

    @GetMapping("/student/wallet-coin")
    public ResponseEntity<?> getWalletSTUStudent() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Student student = studentService.findByEmail(username);

            Wallet wallet =walletService.findByStudent(student);

            WalletSTUInfoDTO info = alchemyService.getWalletInfoSTU(wallet.getWalletAddress());
            return ApiResponseBuilder.success("Lấy thông tin ví thành công", info);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Không thể lấy thông tin ví!");
        }
    }


}
