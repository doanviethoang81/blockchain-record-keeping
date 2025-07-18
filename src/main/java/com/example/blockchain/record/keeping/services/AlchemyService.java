package com.example.blockchain.record.keeping.services;

import com.STUcoin.contract.STUcoin_sol_STUcoin;
import com.example.blockchain.record.keeping.dtos.request.TransactionDTO;
import com.example.blockchain.record.keeping.dtos.request.WalletInfoDTO;
import com.example.blockchain.record.keeping.dtos.request.WalletSTUInfoDTO;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.response.PaginationMeta;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

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

    public PaginatedData<TransactionDTO> getAllTransactions(String walletAddress, String toContract, String type, int offset, int limit) {
        List<TransactionDTO> all = new ArrayList<>();

        try {
            HttpClient client = HttpClient.newHttpClient();
            List<JsonNode> transferNodes = new ArrayList<>();

            // Tăng desiredCount để đảm bảo lấy đủ dữ liệu
            int desiredCount = offset + limit + 100; // Buffer thêm 100 để tránh bỏ sót

            if ("all".equalsIgnoreCase(type)) {
                transferNodes.addAll(fetchAllTransfersWithPagination(client, walletAddress, "toAddress", desiredCount));
                transferNodes.addAll(fetchAllTransfersWithPagination(client, walletAddress, "fromAddress", desiredCount));
            } else if ("in".equalsIgnoreCase(type)) {
                transferNodes.addAll(fetchAllTransfersWithPagination(client, walletAddress, "toAddress", desiredCount));
            } else if ("out".equalsIgnoreCase(type)) {
                transferNodes.addAll(fetchAllTransfersWithPagination(client, walletAddress, "fromAddress", desiredCount));
                if (toContract != null && !toContract.isEmpty()) {
                    transferNodes.removeIf(tx -> !toContract.equalsIgnoreCase(tx.path("to").asText().trim()));
                }
            }

            Map<String, TransactionDTO> seenMap = new HashMap<>();

            for (JsonNode tx : transferNodes) {
                String hash = tx.path("hash").asText();
                if (seenMap.containsKey(hash)) continue;

                TransactionDTO dto = new TransactionDTO();
                dto.setHash(hash);
                dto.setFrom(tx.path("from").asText().trim());
                dto.setTo(tx.path("to").asText().trim());
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
                    String gasPriceHex = txDetail.path("gasPrice").asText("0x0");

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
                    String gasUsedHex = txReceipt.path("gasUsed").asText("0x0");

                    BigInteger gasPrice = new BigInteger(gasPriceHex.substring(2), 16);
                    BigInteger gasUsed = new BigInteger(gasUsedHex.substring(2), 16);

                    dto.setGasPrice(gasPrice.toString());
                    dto.setGasUsed(gasUsed.toString());
                    BigDecimal feeInWei = new BigDecimal(gasPrice).multiply(new BigDecimal(gasUsed));
                    BigDecimal feeInEth = feeInWei.divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);
                    dto.setTransactionFee(feeInEth.toPlainString());
                } catch (Exception e) {
                    System.err.println("Error fetching transaction details for hash: " + hash + " - " + e.getMessage());
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
      }
      """.formatted(addressType, walletAddress, "0x" + Integer.toHexString(maxPerCall)));

            if (pageKey != null) {
                paramsBuilder.append(String.format(", \"pageKey\": \"%s\"", pageKey));
            }

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
            BigDecimal gasPriceGwei = new BigDecimal(gasPrice).divide(BigDecimal.TEN.pow(9), 6, RoundingMode.HALF_UP);

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

    public WalletSTUInfoDTO getWalletInfoSTU(String address) {
        if (address == null) {
            WalletSTUInfoDTO dto = new WalletSTUInfoDTO();
            dto.setStuCoin("0");
            return dto;
        }

        try {
            Web3j web3j = Web3j.build(new HttpService(ALCHEMY_BASE_URL));

            // Lấy số dư ETH
            EthGetBalance balanceWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            BigDecimal eth = new BigDecimal(balanceWei.getBalance()).divide(BigDecimal.TEN.pow(18), 6, RoundingMode.HALF_UP);

            // Lấy số lần giao dịch (nonce)
            EthGetTransactionCount txCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = txCount.getTransactionCount();

            // Lấy gas price
            BigDecimal gasPriceGwei = new BigDecimal(web3j.ethGasPrice().send().getGasPrice())
                    .divide(BigDecimal.TEN.pow(9), 2, RoundingMode.HALF_UP);

            String SMART_CONTRACT_STUCOIN_ADDRESS = EnvUtil.get("SMART_CONTRACT_STUCOIN_ADDRESS");

            // Load smart contract STUcoi (dùng ví bất kỳ để call, không cần ký)
            STUcoin_sol_STUcoin stucoi = STUcoin_sol_STUcoin.load(
                    SMART_CONTRACT_STUCOIN_ADDRESS,  // ví dụ: "0xabc123..."
                    web3j,
                    new ReadonlyTransactionManager(web3j, address),
                    new DefaultGasProvider()
            );

            // Gọi balanceOf để lấy số dư STUcoin
            BigInteger tokenBalance = stucoi.balanceOf(address).send();
            BigDecimal stuBalance = new BigDecimal(tokenBalance).divide(BigDecimal.TEN.pow(18), 6, RoundingMode.HALF_UP);

            WalletSTUInfoDTO dto = new WalletSTUInfoDTO();
            dto.setStuCoin(stuBalance.toPlainString());
            return dto;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy thông tin ví");
        }
    }

    //chuyển coin từ sinh vien sang trường
    public String transferStucoin(String fromPrivateKey, String toAddress, BigInteger amount) {
        try {
            String ALCHEMY_URL = EnvUtil.get("ALCHEMY_URL");

            Web3j web3j = Web3j.build(new HttpService(ALCHEMY_URL));
            Credentials credentials = Credentials.create(fromPrivateKey);
            String toContract = EnvUtil.get("SMART_CONTRACT_STUCOIN_ADDRESS");

            STUcoin_sol_STUcoin stucoin = STUcoin_sol_STUcoin.load(
                    toContract,
                    web3j,
                    credentials,
                    new DefaultGasProvider()
            );

            TransactionReceipt receipt = stucoin.transfer(toAddress, amount).send();
            return receipt.getTransactionHash();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi chuyển STUcoin" + e.getMessage());
        }
    }


}
