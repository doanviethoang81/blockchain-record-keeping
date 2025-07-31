package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.TransactionDTO;
import com.example.blockchain.record.keeping.dtos.request.WalletInfoDTO;
import com.example.blockchain.record.keeping.dtos.request.WalletSTUInfoDTO;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.Wallet;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.response.PaginationMeta;
import com.example.blockchain.record.keeping.services.AlchemyService;
import com.example.blockchain.record.keeping.services.EtherscanService;
import com.example.blockchain.record.keeping.services.StudentService;
import com.example.blockchain.record.keeping.services.WalletService;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalletController {
    private final AlchemyService alchemyService;
    private final StudentService studentService;
    private final WalletService walletService;
    private final EtherscanService etherscanService;

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            String walletAddress = EnvUtil.get("METAMASK_ADDRESS");

            List<TransactionDTO> allTxs = etherscanService.getAllTransactions(walletAddress);
            int totalItems = allTxs.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            if (page < 1) page = 1;

            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalItems);

            if (fromIndex >= totalItems) {
                PaginatedData<TransactionDTO> data = new PaginatedData<>(Collections.emptyList(),
                        new PaginationMeta(totalItems, 0, size, page, totalPages));
                return ApiResponseBuilder.success("Không có giao dịch nào", data);
            }

            List<TransactionDTO> pageList = allTxs.subList(fromIndex, toIndex);

            PaginationMeta meta = new PaginationMeta(totalItems, pageList.size(), size, page, totalPages);
            PaginatedData<TransactionDTO> data = new PaginatedData<>(pageList, meta);

            return ApiResponseBuilder.success("Lấy lịch sử giao dịch thành công", data);

        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //thông tin ví trường
    @PreAuthorize("hasAuthority('READ')")
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

    //số STU coin
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/wallet-info-stu")
    public ResponseEntity<?> getWalletSTUInfo() {
        String address = EnvUtil.get("METAMASK_ADDRESS");
        try {
            WalletSTUInfoDTO info = alchemyService.getWalletInfoSTUOfUniversity(address);
            return ApiResponseBuilder.success("Lấy thông tin ví thành công", info);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Không thể lấy thông tin ví!");
        }
    }

    //lịch sử giao dịch của sinh viên
    @GetMapping("/student/transactions")
    public ResponseEntity<?> getTransactionStudent(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            String username= authentication.getName();
            Student student = studentService.findByEmail(username);
            Wallet wallet = walletService.findByStudent(student);
            String walletAddress = wallet.getWalletAddress();
//            String walletAddress = EnvUtil.get("METAMASK_ADDRESS");

            List<TransactionDTO> allTxs = etherscanService.getAllTransactions(walletAddress);
            int totalItems = allTxs.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            if (page < 1) page = 1;

            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalItems);

            if (fromIndex >= totalItems) {
                PaginatedData<TransactionDTO> data = new PaginatedData<>(Collections.emptyList(),
                        new PaginationMeta(totalItems, 0, size, page, totalPages));
                return ApiResponseBuilder.success("Không có giao dịch nào", data);
            }

            List<TransactionDTO> pageList = allTxs.subList(fromIndex, toIndex);

            PaginationMeta meta = new PaginationMeta(totalItems, pageList.size(), size, page, totalPages);
            PaginatedData<TransactionDTO> data = new PaginatedData<>(pageList, meta);

            return ApiResponseBuilder.success("Lấy lịch sử giao dịch thành công", data);

        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
