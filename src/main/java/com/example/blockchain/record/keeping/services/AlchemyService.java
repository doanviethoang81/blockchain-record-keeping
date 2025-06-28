package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.TransactionDTO;
import com.example.blockchain.record.keeping.dtos.request.WalletInfoDTO;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.response.PaginationMeta;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AlchemyService {
    private static final String ALCHEMY_BASE_URL = EnvUtil.get("ALCHEMY_URL");
//    private static final String ALCHEMY_BASE_URL = "https://eth-sepolia.g.alchemy.com/v2/" + ALCHEMY_API_KEY;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaginatedData<TransactionDTO> getAllTransactions(String walletAddress, String type, int offset, int limit) {
        List<TransactionDTO> all = new ArrayList<>();

        try {
            HttpClient client = HttpClient.newHttpClient();
            List<JsonNode> transferNodes = new ArrayList<>();

            // Duyệt theo type (in/out/all)
            if ("all".equalsIgnoreCase(type)) {
                transferNodes.addAll(fetchAllTransfersWithPagination(client, walletAddress, "toAddress", offset + limit));
                transferNodes.addAll(fetchAllTransfersWithPagination(client, walletAddress, "fromAddress", offset + limit));
            } else if ("in".equalsIgnoreCase(type)) {
                transferNodes.addAll(fetchAllTransfersWithPagination(client, walletAddress, "toAddress", offset + limit));
            } else if ("out".equalsIgnoreCase(type)) {
                transferNodes.addAll(fetchAllTransfersWithPagination(client, walletAddress, "fromAddress", offset + limit));
            }

            Map<String, TransactionDTO> seenMap = new HashMap<>();

            for (JsonNode tx : transferNodes) {
                String hash = tx.path("hash").asText();
                if (seenMap.containsKey(hash)) continue;

                TransactionDTO dto = new TransactionDTO();
                dto.setHash(hash);
                dto.setFrom(tx.path("from").asText());
                dto.setTo(tx.path("to").asText());
                dto.setValue(tx.path("value").asText());
                dto.setAsset(tx.path("asset").asText());
                dto.setBlockNum(tx.path("blockNum").asText());
                dto.setBlockTimestamp(tx.path("metadata").path("blockTimestamp").asText());
                dto.setDirection(walletAddress.equalsIgnoreCase(dto.getFrom()) ? "OUT" : "IN");

                try {
                    String txDetailReq = """
                {
                  "jsonrpc":"2.0",
                  "method":"eth_getTransactionByHash",
                  "params":["%s"],
                  "id":1
                }
                """.formatted(hash);

                    HttpRequest txRequest = HttpRequest.newBuilder()
                            .uri(URI.create(ALCHEMY_BASE_URL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(txDetailReq))
                            .build();

                    JsonNode txDetail = objectMapper.readTree(client.send(txRequest, HttpResponse.BodyHandlers.ofString()).body())
                            .path("result");
                    String gasPriceHex = txDetail.path("gasPrice").asText("0");

                    String txReceiptReq = """
                {
                  "jsonrpc":"2.0",
                  "method":"eth_getTransactionReceipt",
                  "params":["%s"],
                  "id":1
                }
                """.formatted(hash);

                    HttpRequest receiptRequest = HttpRequest.newBuilder()
                            .uri(URI.create(ALCHEMY_BASE_URL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(txReceiptReq))
                            .build();

                    JsonNode txReceipt = objectMapper.readTree(client.send(receiptRequest, HttpResponse.BodyHandlers.ofString()).body())
                            .path("result");
                    String gasUsedHex = txReceipt.path("gasUsed").asText("0");

                    BigInteger gasPrice = new BigInteger(gasPriceHex.substring(2), 16);
                    BigInteger gasUsed = new BigInteger(gasUsedHex.substring(2), 16);

                    dto.setGasPrice(gasPrice.toString());
                    dto.setGasUsed(gasUsed.toString());
                    dto.setTransactionFee(gasPrice.multiply(gasUsed).toString());
                } catch (Exception e) {
                    dto.setGasPrice("0");
                    dto.setGasUsed("0");
                    dto.setTransactionFee("0");
                }

                seenMap.put(hash, dto);
            }

            List<TransactionDTO> allSorted = new ArrayList<>(seenMap.values());
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            allSorted.sort((a, b) -> {
                Instant t1 = Instant.from(formatter.parse(b.getBlockTimestamp()));
                Instant t2 = Instant.from(formatter.parse(a.getBlockTimestamp()));
                return t1.compareTo(t2);
            });

            int totalItems = allSorted.size();
            int fromIndex = Math.min(offset, totalItems);
            int toIndex = Math.min(offset + limit, totalItems);
            List<TransactionDTO> pagedList = allSorted.subList(fromIndex, toIndex);

            int page = (offset / limit) + 1;
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            PaginationMeta meta = new PaginationMeta(totalItems, pagedList.size(), limit, page, totalPages);
            return new PaginatedData<>(pagedList, meta);

        } catch (Exception e) {
            e.printStackTrace();
            return new PaginatedData<>(List.of(), new PaginationMeta(0, 0, limit, 1, 1));
        }
    }

    private List<JsonNode> fetchAllTransfersWithPagination(HttpClient client, String walletAddress, String addressType, int desiredCount) throws Exception {
        List<JsonNode> results = new ArrayList<>();
        String pageKey = null;
        int maxPerCall = 100;

        do {
            StringBuilder paramsBuilder = new StringBuilder();
            paramsBuilder.append("""
          {
            "fromBlock": "0x0",
            "%s": "%s",
            "category": ["external", "erc20", "erc721", "internal"],
            "withMetadata": true,
            "excludeZeroValue": false,
            "maxCount": "%s"
        """.formatted(addressType, walletAddress, "0x" + Integer.toHexString(maxPerCall)));

            if (pageKey != null) {
                paramsBuilder.append(", " + pageKey+ ": \"%s\"".formatted(pageKey));
            }

            paramsBuilder.append("}");

            String requestBody = """
        {
          "jsonrpc": "2.0",
          "id": 1,
          "method": "alchemy_getAssetTransfers",
          "params": [%s]
        }
        """.formatted(paramsBuilder);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ALCHEMY_BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode transfers = root.path("result").path("transfers");
            JsonNode nextPageKey = root.path("result").path("pageKey");

            transfers.forEach(results::add);

            pageKey = nextPageKey.isMissingNode() ? null : nextPageKey.asText();
        } while (pageKey != null && results.size() < desiredCount);

        return results;
    }

    public WalletInfoDTO getWalletInfo(String address) {
        try {
            Web3j web3j = Web3j.build(new HttpService(ALCHEMY_BASE_URL));

            // Lấy số dư
            EthGetBalance balanceWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            BigInteger wei = balanceWei.getBalance();
            BigDecimal eth = new BigDecimal(wei).divide(BigDecimal.TEN.pow(18), 6, RoundingMode.HALF_UP);

            // Lấy nonce (số lần gửi giao dịch)
            EthGetTransactionCount txCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = txCount.getTransactionCount();

            // Lấy gas price hiện tại
            EthGasPrice gasPriceResponse = web3j.ethGasPrice().send();
            BigInteger gasPrice = gasPriceResponse.getGasPrice(); // đơn vị: wei

            // Tính gas price theo Gwei
            BigDecimal gasPriceGwei = new BigDecimal(gasPrice).divide(BigDecimal.TEN.pow(9), 2, RoundingMode.HALF_UP);

            // Tạo DTO kết quả
            WalletInfoDTO dto = new WalletInfoDTO();
            dto.setAddress(address);
            dto.setBalanceEth(eth.toPlainString());
            dto.setNonce(nonce.toString());
            dto.setGasPriceGwei(gasPriceGwei.toPlainString());
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy thông tin ví");
        }
    }

}
