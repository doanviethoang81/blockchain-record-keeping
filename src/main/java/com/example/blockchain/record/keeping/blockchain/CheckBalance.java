//package com.example.blockchain.record.keeping.blockchain;
//
//import org.web3j.protocol.Web3j;
//import org.web3j.protocol.http.HttpService;
//import org.web3j.crypto.Credentials;
//import org.web3j.utils.Convert;
//import java.math.BigDecimal;
//import java.math.BigInteger;
//
//public class CheckBalance {
//    public static void main(String[] args) throws Exception {
//        String PRIVATE_KEY = "0x3D9433e04432406335CBEf89cB39784fC1Fd7d7B"; // Thay bằng private key ví của bạn
//        String ALCHEMY_URL = "https://eth-sepolia.g.alchemy.com/v2/RMhVi74n-f9YMV2rTlAps5gXGpCnMLjY";
//
//        Web3j web3j = Web3j.build(new HttpService(ALCHEMY_URL));
//        Credentials credentials = Credentials.create(PRIVATE_KEY);
//
//        BigInteger wei = web3j.ethGetBalance(credentials.getAddress(),
//                        org.web3j.protocol.core.DefaultBlockParameterName.LATEST)
//                .send().getBalance();
//
//        BigDecimal eth = Convert.fromWei(wei.toString(), Convert.Unit.ETHER);
//        System.out.println("Số dư: " + eth + " ETH");
//    }
//}
//
