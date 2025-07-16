package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.TransactionDTO;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EtherscanService {

    public String getTokenTransactions(String address) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String url = "https://api-sepolia.etherscan.io/api" +
                "?module=account" +
                "&action=tokentx" +
                "&address=" + address +
                "&startblock=0" +
                "&endblock=99999999" +
                "&sort=desc" +
                "&apikey=" + EnvUtil.get("ETHERSCAN_API_KEY");

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public List<TransactionDTO> getTokenTransactionsFromEtherscan(String address, String walletAddress) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String url = "https://api-sepolia.etherscan.io/api" +
                "?module=account" +
                "&action=tokentx" +
                "&address=" + address +
                "&startblock=0" +
                "&endblock=99999999" +
                "&sort=desc" +
                "&apikey=" + EnvUtil.get("ETHERSCAN_API_KEY");

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode results = root.path("result");

            List<TransactionDTO> transactions = new ArrayList<>();
            for (JsonNode tx : results) {
                TransactionDTO dto = new TransactionDTO();
                dto.setHash(tx.path("hash").asText());
                dto.setFrom(tx.path("from").asText());
                dto.setTo(tx.path("to").asText());
                dto.setValue(tx.path("value").asText());
                dto.setAsset(tx.path("tokenSymbol").asText()); // Etherscan trả về "tokenSymbol"
                dto.setBlockNum(tx.path("blockNumber").asText());
                dto.setBlockTimestamp(tx.path("timeStamp").path("blockTimestamp").asText());
//                dto.setBlockTimestamp(tx.path("timeStamp").asText()); // Unix timestamp
                dto.setDirection(walletAddress.equalsIgnoreCase(tx.path("from").asText()) ? "OUT" : "IN");

                transactions.add(dto);
            }

            return transactions;
        }
    }


    public List<TransactionDTO> getTokenTx(String address) throws Exception {
        String url = "https://api-sepolia.etherscan.io/api"
                + "?module=account&action=tokentx"
                + "&address=" + address
                + "&startblock=0&endblock=99999999"
                + "&page=1&offset=100&sort=desc"
                + "&apikey=" + EnvUtil.get("ETHERSCAN_API_KEY");

        RestTemplate restTemplate = new RestTemplate();
        JsonNode root = restTemplate.getForObject(url, JsonNode.class);

        if (root == null || !root.has("result")) {
            throw new RuntimeException("Không thể lấy dữ liệu giao dịch từ Etherscan");
        }

        JsonNode result = root.path("result");
        List<TransactionDTO> list = new ArrayList<>();

        for (JsonNode tx : result) {
            String gasPrice = tx.path("gasPrice").asText("0");
            String gasUsed = tx.path("gasUsed").asText("0");
            BigInteger feeWei = new BigInteger(gasPrice).multiply(new BigInteger(gasUsed));

            // Convert về đơn vị dễ đọc
            BigDecimal valueSTU = new BigDecimal(tx.path("value").asText("0")).divide(BigDecimal.TEN.pow(18), 6, RoundingMode.HALF_UP);
            BigDecimal transactionFeeETH = new BigDecimal(feeWei).divide(BigDecimal.TEN.pow(18), 6, RoundingMode.HALF_UP);

            TransactionDTO dto = new TransactionDTO();
            dto.setHash(tx.path("hash").asText());
            dto.setFrom(tx.path("from").asText().trim());
            dto.setTo(tx.path("to").asText().trim());
            dto.setValue(valueSTU.toPlainString()); // 5.000000 STU
            dto.setAsset(tx.path("tokenSymbol").asText());
            dto.setBlockNum(tx.path("blockNumber").asText());

            // Format timestamp ISO 8601
            try {
                long timestamp = Long.parseLong(tx.path("timeStamp").asText());
                Instant instant = Instant.ofEpochSecond(timestamp);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .withZone(ZoneOffset.UTC);
                dto.setBlockTimestamp(formatter.format(instant));
            } catch (Exception e) {
                dto.setBlockTimestamp("Invalid timestamp");
            }

            dto.setDirection(address.equalsIgnoreCase(dto.getFrom()) ? "OUT" : "IN");
            dto.setGasPrice(gasPrice);
            dto.setGasUsed(gasUsed);
            dto.setTransactionFee(transactionFeeETH.toPlainString()); // ví dụ: 0.000086 ETH

            list.add(dto);
        }

        return list;
    }

    public List<TransactionDTO> getTxList(String address) throws Exception {
        String url = "https://api-sepolia.etherscan.io/api"
                + "?module=account&action=txlist"
                + "&address=" + address
                + "&startblock=0&endblock=99999999"
                + "&page=1&offset=100&sort=desc"
                + "&apikey=" + EnvUtil.get("ETHERSCAN_API_KEY");

        RestTemplate restTemplate = new RestTemplate();
        JsonNode root = restTemplate.getForObject(url, JsonNode.class);

        if (root == null || !root.has("result")) {
            throw new RuntimeException("Không thể lấy dữ liệu txlist từ Etherscan");
        }

        JsonNode result = root.path("result");
        List<TransactionDTO> list = new ArrayList<>();

        for (JsonNode tx : result) {
            String gasPrice = tx.path("gasPrice").asText("0");
            String gasUsed = tx.path("gasUsed").asText("0");
            BigInteger feeWei = new BigInteger(gasPrice).multiply(new BigInteger(gasUsed));

            BigDecimal valueEth = new BigDecimal(tx.path("value").asText("0")).divide(BigDecimal.TEN.pow(18), 6, RoundingMode.HALF_UP);
            BigDecimal transactionFee = new BigDecimal(feeWei).divide(BigDecimal.TEN.pow(18), 6, RoundingMode.HALF_UP);

            TransactionDTO dto = new TransactionDTO();
            dto.setHash(tx.path("hash").asText());
            dto.setFrom(tx.path("from").asText().trim());
            dto.setTo(tx.path("to").asText().trim());
            dto.setValue(valueEth.toPlainString());
            dto.setAsset("ETH");
            dto.setBlockNum(tx.path("blockNumber").asText());

            // Format timestamp
            try {
                long timestamp = Long.parseLong(tx.path("timeStamp").asText());
                Instant instant = Instant.ofEpochSecond(timestamp);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .withZone(ZoneOffset.UTC);
                dto.setBlockTimestamp(formatter.format(instant));
            } catch (Exception e) {
                dto.setBlockTimestamp("Invalid timestamp");
            }

            dto.setDirection(address.equalsIgnoreCase(dto.getFrom()) ? "OUT" : "IN");
            dto.setGasPrice(gasPrice);
            dto.setGasUsed(gasUsed);
            dto.setTransactionFee(transactionFee.toPlainString());

            list.add(dto);
        }

        return list;
    }


    public List<TransactionDTO> getAllTransactions(String address) throws Exception {
        List<TransactionDTO> all = new ArrayList<>();
        all.addAll(getTxList(address));      // ETH
        all.addAll(getTokenTx(address));     // STUcoin

        // Sắp xếp theo thời gian giảm dần (mới nhất trước)
        all.sort((a, b) -> {
            try {
                Instant timeA = Instant.parse(a.getBlockTimestamp());
                Instant timeB = Instant.parse(b.getBlockTimestamp());
                return timeB.compareTo(timeA); // Giảm dần
            } catch (Exception e) {
                return 0;
            }
        });

        return all;
    }

//    public List<TransactionDTO> getAllTransactions(String address) throws Exception {
//        List<TransactionDTO> combined = new ArrayList<>();
//        combined.addAll(getTxList(address));    // ETH
//        combined.addAll(getTokenTx(address));   // STU
//
//        // Xóa trùng theo hash
//        Map<String, TransactionDTO> uniqueMap = new LinkedHashMap<>();
//        for (TransactionDTO tx : combined) {
//            uniqueMap.putIfAbsent(tx.getHash(), tx); // giữ lần đầu tiên gặp
//        }
//
//        // Sắp xếp theo thời gian (mới nhất trước)
//        List<TransactionDTO> sorted = new ArrayList<>(uniqueMap.values());
//        sorted.sort((a, b) -> {
//            try {
//                Instant timeA = Instant.parse(a.getBlockTimestamp());
//                Instant timeB = Instant.parse(b.getBlockTimestamp());
//                return timeB.compareTo(timeA);
//            } catch (Exception e) {
//                return 0;
//            }
//        });
//
//        return sorted;
//    }

}
