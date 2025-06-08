package com.certificate.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
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
import org.web3j.tuples.generated.Tuple7;
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
public class CertificateStorage_sol_CertificateStorage extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b50335f5f6101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550610f938061005b5f395ff3fe608060405234801561000f575f5ffd5b506004361061003f575f3560e01c80638007e6a3146100435780638da5cb5b1461005f578063ed0f2e751461007d575b5f5ffd5b61005d6004803603810190610058919061085c565b6100b3565b005b610067610289565b6040516100749190610a2c565b60405180910390f35b61009760048036038101906100929190610a45565b6102ad565b6040516100aa9796959493929190610aec565b60405180910390f35b5f5f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610141576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161013890610bfa565b60405180910390fd5b5f6040518060e001604052808981526020018881526020018781526020018681526020018581526020018481526020018381525090508060018a6040516101889190610c52565b90815260200160405180910390205f820151815f0190816101a99190610e6e565b5060208201518160010190816101bf9190610e6e565b5060408201518160020190816101d59190610e6e565b5060608201518160030190816101eb9190610e6e565b5060808201518160040190816102019190610e6e565b5060a08201518160050190816102179190610e6e565b5060c082015181600601908161022d9190610e6e565b509050508860405161023f9190610c52565b60405180910390207f3ca9b880c8645562f6f6dae2ec8c6da0e58ddcc82681ed677eb65aaaaf587742896040516102769190610f3d565b60405180910390a2505050505050505050565b5f5f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60608060608060608060605f6001896040516102c99190610c52565b90815260200160405180910390206040518060e00160405290815f820180546102f190610c95565b80601f016020809104026020016040519081016040528092919081815260200182805461031d90610c95565b80156103685780601f1061033f57610100808354040283529160200191610368565b820191905f5260205f20905b81548152906001019060200180831161034b57829003601f168201915b5050505050815260200160018201805461038190610c95565b80601f01602080910402602001604051908101604052809291908181526020018280546103ad90610c95565b80156103f85780601f106103cf576101008083540402835291602001916103f8565b820191905f5260205f20905b8154815290600101906020018083116103db57829003601f168201915b5050505050815260200160028201805461041190610c95565b80601f016020809104026020016040519081016040528092919081815260200182805461043d90610c95565b80156104885780601f1061045f57610100808354040283529160200191610488565b820191905f5260205f20905b81548152906001019060200180831161046b57829003601f168201915b505050505081526020016003820180546104a190610c95565b80601f01602080910402602001604051908101604052809291908181526020018280546104cd90610c95565b80156105185780601f106104ef57610100808354040283529160200191610518565b820191905f5260205f20905b8154815290600101906020018083116104fb57829003601f168201915b5050505050815260200160048201805461053190610c95565b80601f016020809104026020016040519081016040528092919081815260200182805461055d90610c95565b80156105a85780601f1061057f576101008083540402835291602001916105a8565b820191905f5260205f20905b81548152906001019060200180831161058b57829003601f168201915b505050505081526020016005820180546105c190610c95565b80601f01602080910402602001604051908101604052809291908181526020018280546105ed90610c95565b80156106385780601f1061060f57610100808354040283529160200191610638565b820191905f5260205f20905b81548152906001019060200180831161061b57829003601f168201915b5050505050815260200160068201805461065190610c95565b80601f016020809104026020016040519081016040528092919081815260200182805461067d90610c95565b80156106c85780601f1061069f576101008083540402835291602001916106c8565b820191905f5260205f20905b8154815290600101906020018083116106ab57829003601f168201915b5050505050815250509050805f015181602001518260400151836060015184608001518560a001518660c00151975097509750975097509750975050919395979092949650565b5f604051905090565b5f5ffd5b5f5ffd5b5f5ffd5b5f5ffd5b5f601f19601f8301169050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b61076e82610728565b810181811067ffffffffffffffff8211171561078d5761078c610738565b5b80604052505050565b5f61079f61070f565b90506107ab8282610765565b919050565b5f67ffffffffffffffff8211156107ca576107c9610738565b5b6107d382610728565b9050602081019050919050565b828183375f83830152505050565b5f6108006107fb846107b0565b610796565b90508281526020810184848401111561081c5761081b610724565b5b6108278482856107e0565b509392505050565b5f82601f83011261084357610842610720565b5b81356108538482602086016107ee565b91505092915050565b5f5f5f5f5f5f5f5f610100898b03121561087957610878610718565b5b5f89013567ffffffffffffffff8111156108965761089561071c565b5b6108a28b828c0161082f565b985050602089013567ffffffffffffffff8111156108c3576108c261071c565b5b6108cf8b828c0161082f565b975050604089013567ffffffffffffffff8111156108f0576108ef61071c565b5b6108fc8b828c0161082f565b965050606089013567ffffffffffffffff81111561091d5761091c61071c565b5b6109298b828c0161082f565b955050608089013567ffffffffffffffff81111561094a5761094961071c565b5b6109568b828c0161082f565b94505060a089013567ffffffffffffffff8111156109775761097661071c565b5b6109838b828c0161082f565b93505060c089013567ffffffffffffffff8111156109a4576109a361071c565b5b6109b08b828c0161082f565b92505060e089013567ffffffffffffffff8111156109d1576109d061071c565b5b6109dd8b828c0161082f565b9150509295985092959890939650565b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f610a16826109ed565b9050919050565b610a2681610a0c565b82525050565b5f602082019050610a3f5f830184610a1d565b92915050565b5f60208284031215610a5a57610a59610718565b5b5f82013567ffffffffffffffff811115610a7757610a7661071c565b5b610a838482850161082f565b91505092915050565b5f81519050919050565b5f82825260208201905092915050565b8281835e5f83830152505050565b5f610abe82610a8c565b610ac88185610a96565b9350610ad8818560208601610aa6565b610ae181610728565b840191505092915050565b5f60e0820190508181035f830152610b04818a610ab4565b90508181036020830152610b188189610ab4565b90508181036040830152610b2c8188610ab4565b90508181036060830152610b408187610ab4565b90508181036080830152610b548186610ab4565b905081810360a0830152610b688185610ab4565b905081810360c0830152610b7c8184610ab4565b905098975050505050505050565b7f4f6e6c79206f776e657220287363686f6f6c292063616e20706572666f726d205f8201527f7468697320616374696f6e000000000000000000000000000000000000000000602082015250565b5f610be4602b83610a96565b9150610bef82610b8a565b604082019050919050565b5f6020820190508181035f830152610c1181610bd8565b9050919050565b5f81905092915050565b5f610c2c82610a8c565b610c368185610c18565b9350610c46818560208601610aa6565b80840191505092915050565b5f610c5d8284610c22565b915081905092915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f6002820490506001821680610cac57607f821691505b602082108103610cbf57610cbe610c68565b5b50919050565b5f819050815f5260205f209050919050565b5f6020601f8301049050919050565b5f82821b905092915050565b5f60088302610d217fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82610ce6565b610d2b8683610ce6565b95508019841693508086168417925050509392505050565b5f819050919050565b5f819050919050565b5f610d6f610d6a610d6584610d43565b610d4c565b610d43565b9050919050565b5f819050919050565b610d8883610d55565b610d9c610d9482610d76565b848454610cf2565b825550505050565b5f5f905090565b610db3610da4565b610dbe818484610d7f565b505050565b5b81811015610de157610dd65f82610dab565b600181019050610dc4565b5050565b601f821115610e2657610df781610cc5565b610e0084610cd7565b81016020851015610e0f578190505b610e23610e1b85610cd7565b830182610dc3565b50505b505050565b5f82821c905092915050565b5f610e465f1984600802610e2b565b1980831691505092915050565b5f610e5e8383610e37565b9150826002028217905092915050565b610e7782610a8c565b67ffffffffffffffff811115610e9057610e8f610738565b5b610e9a8254610c95565b610ea5828285610de5565b5f60209050601f831160018114610ed6575f8415610ec4578287015190505b610ece8582610e53565b865550610f35565b601f198416610ee486610cc5565b5f5b82811015610f0b57848901518255600182019150602085019450602081019050610ee6565b86831015610f285784890151610f24601f891682610e37565b8355505b6001600288020188555050505b505050505050565b5f6020820190508181035f830152610f558184610ab4565b90509291505056fea26469706673582212202cb7eac9db04f18f76d261a7a9eb70adc355dd10e544d7b28c43774edee84a6a64736f6c634300081e0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_GETCERTIFICATE = "getCertificate";

    public static final String FUNC_ISSUECERTIFICATE = "issueCertificate";

    public static final String FUNC_OWNER = "owner";

    public static final Event CERTIFICATEISSUED_EVENT = new Event("CertificateIssued", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event CERTIFICATEREVOKED_EVENT = new Event("CertificateRevoked", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>(true) {}));
    ;

    @Deprecated
    protected CertificateStorage_sol_CertificateStorage(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CertificateStorage_sol_CertificateStorage(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected CertificateStorage_sol_CertificateStorage(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected CertificateStorage_sol_CertificateStorage(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

//    public static List<CertificateIssuedEventResponse> getCertificateIssuedEvents(
//            TransactionReceipt transactionReceipt) {
//        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CERTIFICATEISSUED_EVENT, transactionReceipt);
//        ArrayList<CertificateIssuedEventResponse> responses = new ArrayList<CertificateIssuedEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            CertificateIssuedEventResponse typedResponse = new CertificateIssuedEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.studentCode = (byte[]) eventValues.getIndexedValues().get(0).getValue();
//            typedResponse.studentName = (String) eventValues.getNonIndexedValues().get(0).getValue();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }

    public static CertificateIssuedEventResponse getCertificateIssuedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CERTIFICATEISSUED_EVENT, log);
        CertificateIssuedEventResponse typedResponse = new CertificateIssuedEventResponse();
        typedResponse.log = log;
        typedResponse.studentCode = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.studentName = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<CertificateIssuedEventResponse> certificateIssuedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCertificateIssuedEventFromLog(log));
    }

    public Flowable<CertificateIssuedEventResponse> certificateIssuedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CERTIFICATEISSUED_EVENT));
        return certificateIssuedEventFlowable(filter);
    }

//    public static List<CertificateRevokedEventResponse> getCertificateRevokedEvents(
//            TransactionReceipt transactionReceipt) {
//        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CERTIFICATEREVOKED_EVENT, transactionReceipt);
//        ArrayList<CertificateRevokedEventResponse> responses = new ArrayList<CertificateRevokedEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            CertificateRevokedEventResponse typedResponse = new CertificateRevokedEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.studentCode = (byte[]) eventValues.getIndexedValues().get(0).getValue();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }

    public static CertificateRevokedEventResponse getCertificateRevokedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CERTIFICATEREVOKED_EVENT, log);
        CertificateRevokedEventResponse typedResponse = new CertificateRevokedEventResponse();
        typedResponse.log = log;
        typedResponse.studentCode = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<CertificateRevokedEventResponse> certificateRevokedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCertificateRevokedEventFromLog(log));
    }

    public Flowable<CertificateRevokedEventResponse> certificateRevokedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CERTIFICATEREVOKED_EVENT));
        return certificateRevokedEventFlowable(filter);
    }

    public RemoteFunctionCall<Tuple7<String, String, String, String, String, String, String>> getCertificate(
            String studentCode) {
        final Function function = new Function(FUNC_GETCERTIFICATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(studentCode)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteFunctionCall<Tuple7<String, String, String, String, String, String, String>>(function,
                new Callable<Tuple7<String, String, String, String, String, String, String>>() {
                    @Override
                    public Tuple7<String, String, String, String, String, String, String> call()
                            throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple7<String, String, String, String, String, String, String>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (String) results.get(4).getValue(), 
                                (String) results.get(5).getValue(), 
                                (String) results.get(6).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> issueCertificate(String studentCode,
            String studentName, String birthDate, String course, String university,
            String createdAt, String diplomaNumber, String certificateImageHash) {
        final Function function = new Function(
                FUNC_ISSUECERTIFICATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(studentCode), 
                new org.web3j.abi.datatypes.Utf8String(studentName), 
                new org.web3j.abi.datatypes.Utf8String(birthDate), 
                new org.web3j.abi.datatypes.Utf8String(course), 
                new org.web3j.abi.datatypes.Utf8String(university), 
                new org.web3j.abi.datatypes.Utf8String(createdAt), 
                new org.web3j.abi.datatypes.Utf8String(diplomaNumber), 
                new org.web3j.abi.datatypes.Utf8String(certificateImageHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    @Deprecated
    public static CertificateStorage_sol_CertificateStorage load(String contractAddress,
            Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CertificateStorage_sol_CertificateStorage(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static CertificateStorage_sol_CertificateStorage load(String contractAddress,
            Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
            BigInteger gasLimit) {
        return new CertificateStorage_sol_CertificateStorage(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static CertificateStorage_sol_CertificateStorage load(String contractAddress,
            Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new CertificateStorage_sol_CertificateStorage(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static CertificateStorage_sol_CertificateStorage load(String contractAddress,
            Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        return new CertificateStorage_sol_CertificateStorage(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<CertificateStorage_sol_CertificateStorage> deploy(Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CertificateStorage_sol_CertificateStorage.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<CertificateStorage_sol_CertificateStorage> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CertificateStorage_sol_CertificateStorage.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<CertificateStorage_sol_CertificateStorage> deploy(Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CertificateStorage_sol_CertificateStorage.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<CertificateStorage_sol_CertificateStorage> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CertificateStorage_sol_CertificateStorage.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class CertificateIssuedEventResponse extends BaseEventResponse {
        public byte[] studentCode;

        public String studentName;
    }

    public static class CertificateRevokedEventResponse extends BaseEventResponse {
        public byte[] studentCode;
    }
}
