package com.certificate.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class CertificateStorage_sol_EncryptedCertificateStorage extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b506102e48061001c5f395ff3fe608060405234801561000f575f5ffd5b5060043610610029575f3560e01c80636e18da9c1461002d575b5f5ffd5b610047600480360381019061004291906101e7565b610049565b005b3373ffffffffffffffffffffffffffffffffffffffff167f6781cdfeb0818feca3bced73ef9831d9aeafaae661468caec13d851e4e98114d8260405161008f919061028e565b60405180910390a250565b5f604051905090565b5f5ffd5b5f5ffd5b5f5ffd5b5f5ffd5b5f601f19601f8301169050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b6100f9826100b3565b810181811067ffffffffffffffff82111715610118576101176100c3565b5b80604052505050565b5f61012a61009a565b905061013682826100f0565b919050565b5f67ffffffffffffffff821115610155576101546100c3565b5b61015e826100b3565b9050602081019050919050565b828183375f83830152505050565b5f61018b6101868461013b565b610121565b9050828152602081018484840111156101a7576101a66100af565b5b6101b284828561016b565b509392505050565b5f82601f8301126101ce576101cd6100ab565b5b81356101de848260208601610179565b91505092915050565b5f602082840312156101fc576101fb6100a3565b5b5f82013567ffffffffffffffff811115610219576102186100a7565b5b610225848285016101ba565b91505092915050565b5f81519050919050565b5f82825260208201905092915050565b8281835e5f83830152505050565b5f6102608261022e565b61026a8185610238565b935061027a818560208601610248565b610283816100b3565b840191505092915050565b5f6020820190508181035f8301526102a68184610256565b90509291505056fea2646970667358221220a6efb5aac0d27d9b1d00b6005ccb598a60a60889256dd90e496086d5274c302364736f6c634300081e0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_SAVECERTIFICATE = "saveCertificate";

    public static final Event CERTIFICATESAVED_EVENT = new Event("CertificateSaved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    @Deprecated
    protected CertificateStorage_sol_EncryptedCertificateStorage(String contractAddress,
            Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CertificateStorage_sol_EncryptedCertificateStorage(String contractAddress,
            Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected CertificateStorage_sol_EncryptedCertificateStorage(String contractAddress,
            Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
            BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected CertificateStorage_sol_EncryptedCertificateStorage(String contractAddress,
            Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

//    public static List<CertificateSavedEventResponse> getCertificateSavedEvents(
//            TransactionReceipt transactionReceipt) {
//        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CERTIFICATESAVED_EVENT, transactionReceipt);
//        ArrayList<CertificateSavedEventResponse> responses = new ArrayList<CertificateSavedEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            CertificateSavedEventResponse typedResponse = new CertificateSavedEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.sender = (String) eventValues.getIndexedValues().get(0).getValue();
//            typedResponse.encryptedHexData = (String) eventValues.getNonIndexedValues().get(0).getValue();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }

    public static CertificateSavedEventResponse getCertificateSavedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CERTIFICATESAVED_EVENT, log);
        CertificateSavedEventResponse typedResponse = new CertificateSavedEventResponse();
        typedResponse.log = log;
        typedResponse.sender = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.encryptedHexData = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<CertificateSavedEventResponse> certificateSavedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCertificateSavedEventFromLog(log));
    }

    public Flowable<CertificateSavedEventResponse> certificateSavedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CERTIFICATESAVED_EVENT));
        return certificateSavedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> saveCertificate(String encryptedHexData) {
        final Function function = new Function(
                FUNC_SAVECERTIFICATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(encryptedHexData)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static CertificateStorage_sol_EncryptedCertificateStorage load(String contractAddress,
            Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CertificateStorage_sol_EncryptedCertificateStorage(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static CertificateStorage_sol_EncryptedCertificateStorage load(String contractAddress,
            Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
            BigInteger gasLimit) {
        return new CertificateStorage_sol_EncryptedCertificateStorage(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static CertificateStorage_sol_EncryptedCertificateStorage load(String contractAddress,
            Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new CertificateStorage_sol_EncryptedCertificateStorage(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static CertificateStorage_sol_EncryptedCertificateStorage load(String contractAddress,
            Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        return new CertificateStorage_sol_EncryptedCertificateStorage(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<CertificateStorage_sol_EncryptedCertificateStorage> deploy(Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CertificateStorage_sol_EncryptedCertificateStorage.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<CertificateStorage_sol_EncryptedCertificateStorage> deploy(Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CertificateStorage_sol_EncryptedCertificateStorage.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<CertificateStorage_sol_EncryptedCertificateStorage> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CertificateStorage_sol_EncryptedCertificateStorage.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<CertificateStorage_sol_EncryptedCertificateStorage> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CertificateStorage_sol_EncryptedCertificateStorage.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

//    public static void linkLibraries(List<Contract.LinkReference> references) {
//        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
//    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class CertificateSavedEventResponse extends BaseEventResponse {
        public String sender;

        public String encryptedHexData;
    }
}
