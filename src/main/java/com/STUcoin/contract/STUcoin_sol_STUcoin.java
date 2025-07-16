package com.STUcoin.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
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
public class STUcoin_sol_STUcoin extends Contract {
    public static final String BINARY = "608060405234801561000f575f5ffd5b50336040518060400160405280600781526020017f535455636f696e000000000000000000000000000000000000000000000000008152506040518060400160405280600381526020017f5354550000000000000000000000000000000000000000000000000000000000815250816003908161008c9190610702565b50806004908161009c9190610702565b5050505f73ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff160361010f575f6040517f1e4fbdf70000000000000000000000000000000000000000000000000000000081526004016101069190610810565b60405180910390fd5b61011e8161015c60201b60201c565b506101573361013161021f60201b60201c565b600a61013d9190610991565b6305f5e10061014c91906109db565b61022760201b60201c565b610aac565b5f60055f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690508160055f6101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b5f6012905090565b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603610297575f6040517fec442f0500000000000000000000000000000000000000000000000000000000815260040161028e9190610810565b60405180910390fd5b6102a85f83836102ac60201b60201c565b5050565b5f73ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff16036102fc578060025f8282546102f09190610a1c565b925050819055506103ca565b5f5f5f8573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f2054905081811015610385578381836040517fe450d38c00000000000000000000000000000000000000000000000000000000815260040161037c93929190610a5e565b60405180910390fd5b8181035f5f8673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f2081905550505b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603610411578060025f828254039250508190555061045b565b805f5f8473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f82825401925050819055505b8173ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040516104b89190610a93565b60405180910390a3505050565b5f81519050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f600282049050600182168061054057607f821691505b602082108103610553576105526104fc565b5b50919050565b5f819050815f5260205f209050919050565b5f6020601f8301049050919050565b5f82821b905092915050565b5f600883026105b57fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8261057a565b6105bf868361057a565b95508019841693508086168417925050509392505050565b5f819050919050565b5f819050919050565b5f6106036105fe6105f9846105d7565b6105e0565b6105d7565b9050919050565b5f819050919050565b61061c836105e9565b6106306106288261060a565b848454610586565b825550505050565b5f5f905090565b610647610638565b610652818484610613565b505050565b5b818110156106755761066a5f8261063f565b600181019050610658565b5050565b601f8211156106ba5761068b81610559565b6106948461056b565b810160208510156106a3578190505b6106b76106af8561056b565b830182610657565b50505b505050565b5f82821c905092915050565b5f6106da5f19846008026106bf565b1980831691505092915050565b5f6106f283836106cb565b9150826002028217905092915050565b61070b826104c5565b67ffffffffffffffff811115610724576107236104cf565b5b61072e8254610529565b610739828285610679565b5f60209050601f83116001811461076a575f8415610758578287015190505b61076285826106e7565b8655506107c9565b601f19841661077886610559565b5f5b8281101561079f5784890151825560018201915060208501945060208101905061077a565b868310156107bc57848901516107b8601f8916826106cb565b8355505b6001600288020188555050505b505050505050565b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f6107fa826107d1565b9050919050565b61080a816107f0565b82525050565b5f6020820190506108235f830184610801565b92915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f8160011c9050919050565b5f5f8291508390505b60018511156108ab5780860481111561088757610886610829565b5b60018516156108965780820291505b80810290506108a485610856565b945061086b565b94509492505050565b5f826108c3576001905061097e565b816108d0575f905061097e565b81600181146108e657600281146108f05761091f565b600191505061097e565b60ff84111561090257610901610829565b5b8360020a91508482111561091957610918610829565b5b5061097e565b5060208310610133831016604e8410600b84101617156109545782820a90508381111561094f5761094e610829565b5b61097e565b6109618484846001610862565b9250905081840481111561097857610977610829565b5b81810290505b9392505050565b5f60ff82169050919050565b5f61099b826105d7565b91506109a683610985565b92506109d37fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff84846108b4565b905092915050565b5f6109e5826105d7565b91506109f0836105d7565b92508282026109fe816105d7565b91508282048414831517610a1557610a14610829565b5b5092915050565b5f610a26826105d7565b9150610a31836105d7565b9250828201905080821115610a4957610a48610829565b5b92915050565b610a58816105d7565b82525050565b5f606082019050610a715f830186610801565b610a7e6020830185610a4f565b610a8b6040830184610a4f565b949350505050565b5f602082019050610aa65f830184610a4f565b92915050565b61127180610ab95f395ff3fe608060405234801561000f575f5ffd5b50600436106100fe575f3560e01c80638a3b019911610095578063a9059cbb11610064578063a9059cbb14610286578063b7fa4659146102b6578063dd62ed3e146102d2578063f2fde38b14610302576100fe565b80638a3b0199146102125780638da5cb5b1461022e57806395d89b411461024c5780639dc29fac1461026a576100fe565b8063313ce567116100d1578063313ce5671461019e57806340c10f19146101bc57806370a08231146101d8578063715018a614610208576100fe565b806306fdde0314610102578063095ea7b31461012057806318160ddd1461015057806323b872dd1461016e575b5f5ffd5b61010a61031e565b6040516101179190610eea565b60405180910390f35b61013a60048036038101906101359190610f9b565b6103ae565b6040516101479190610ff3565b60405180910390f35b6101586103d0565b604051610165919061101b565b60405180910390f35b61018860048036038101906101839190611034565b6103d9565b6040516101959190610ff3565b60405180910390f35b6101a6610407565b6040516101b3919061109f565b60405180910390f35b6101d660048036038101906101d19190610f9b565b61040f565b005b6101f260048036038101906101ed91906110b8565b610425565b6040516101ff919061101b565b60405180910390f35b61021061046a565b005b61022c60048036038101906102279190611034565b61047d565b005b610236610495565b60405161024391906110f2565b60405180910390f35b6102546104bd565b6040516102619190610eea565b60405180910390f35b610284600480360381019061027f9190610f9b565b61054d565b005b6102a0600480360381019061029b9190610f9b565b610563565b6040516102ad9190610ff3565b60405180910390f35b6102d060048036038101906102cb9190610f9b565b610585565b005b6102ec60048036038101906102e7919061110b565b6105a8565b6040516102f9919061101b565b60405180910390f35b61031c600480360381019061031791906110b8565b61062a565b005b60606003805461032d90611176565b80601f016020809104026020016040519081016040528092919081815260200182805461035990611176565b80156103a45780601f1061037b576101008083540402835291602001916103a4565b820191905f5260205f20905b81548152906001019060200180831161038757829003601f168201915b5050505050905090565b5f5f6103b86106ae565b90506103c58185856106b5565b600191505092915050565b5f600254905090565b5f5f6103e36106ae565b90506103f08582856106c7565b6103fb85858561075a565b60019150509392505050565b5f6012905090565b61041761084a565b61042182826108d1565b5050565b5f5f5f8373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f20549050919050565b61047261084a565b61047b5f610950565b565b61048561084a565b61049083838361075a565b505050565b5f60055f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b6060600480546104cc90611176565b80601f01602080910402602001604051908101604052809291908181526020018280546104f890611176565b80156105435780601f1061051a57610100808354040283529160200191610543565b820191905f5260205f20905b81548152906001019060200180831161052657829003601f168201915b5050505050905090565b61055561084a565b61055f8282610a13565b5050565b5f5f61056d6106ae565b905061057a81858561075a565b600191505092915050565b61058d61084a565b5f610596610495565b90506105a383828461075a565b505050565b5f60015f8473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f8373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f2054905092915050565b61063261084a565b5f73ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff16036106a2575f6040517f1e4fbdf700000000000000000000000000000000000000000000000000000000815260040161069991906110f2565b60405180910390fd5b6106ab81610950565b50565b5f33905090565b6106c28383836001610a92565b505050565b5f6106d284846105a8565b90507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8110156107545781811015610745578281836040517ffb8f41b200000000000000000000000000000000000000000000000000000000815260040161073c939291906111a6565b60405180910390fd5b61075384848484035f610a92565b5b50505050565b5f73ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff16036107ca575f6040517f96c6fd1e0000000000000000000000000000000000000000000000000000000081526004016107c191906110f2565b60405180910390fd5b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff160361083a575f6040517fec442f0500000000000000000000000000000000000000000000000000000000815260040161083191906110f2565b60405180910390fd5b610845838383610c61565b505050565b6108526106ae565b73ffffffffffffffffffffffffffffffffffffffff16610870610495565b73ffffffffffffffffffffffffffffffffffffffff16146108cf576108936106ae565b6040517f118cdaa70000000000000000000000000000000000000000000000000000000081526004016108c691906110f2565b60405180910390fd5b565b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603610941575f6040517fec442f0500000000000000000000000000000000000000000000000000000000815260040161093891906110f2565b60405180910390fd5b61094c5f8383610c61565b5050565b5f60055f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690508160055f6101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603610a83575f6040517f96c6fd1e000000000000000000000000000000000000000000000000000000008152600401610a7a91906110f2565b60405180910390fd5b610a8e825f83610c61565b5050565b5f73ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff1603610b02575f6040517fe602df05000000000000000000000000000000000000000000000000000000008152600401610af991906110f2565b60405180910390fd5b5f73ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603610b72575f6040517f94280d62000000000000000000000000000000000000000000000000000000008152600401610b6991906110f2565b60405180910390fd5b8160015f8673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f8573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f20819055508015610c5b578273ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b92584604051610c52919061101b565b60405180910390a35b50505050565b5f73ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603610cb1578060025f828254610ca59190611208565b92505081905550610d7f565b5f5f5f8573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f2054905081811015610d3a578381836040517fe450d38c000000000000000000000000000000000000000000000000000000008152600401610d31939291906111a6565b60405180910390fd5b8181035f5f8673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f2081905550505b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603610dc6578060025f8282540392505081905550610e10565b805f5f8473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f82825401925050819055505b8173ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef83604051610e6d919061101b565b60405180910390a3505050565b5f81519050919050565b5f82825260208201905092915050565b8281835e5f83830152505050565b5f601f19601f8301169050919050565b5f610ebc82610e7a565b610ec68185610e84565b9350610ed6818560208601610e94565b610edf81610ea2565b840191505092915050565b5f6020820190508181035f830152610f028184610eb2565b905092915050565b5f5ffd5b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f610f3782610f0e565b9050919050565b610f4781610f2d565b8114610f51575f5ffd5b50565b5f81359050610f6281610f3e565b92915050565b5f819050919050565b610f7a81610f68565b8114610f84575f5ffd5b50565b5f81359050610f9581610f71565b92915050565b5f5f60408385031215610fb157610fb0610f0a565b5b5f610fbe85828601610f54565b9250506020610fcf85828601610f87565b9150509250929050565b5f8115159050919050565b610fed81610fd9565b82525050565b5f6020820190506110065f830184610fe4565b92915050565b61101581610f68565b82525050565b5f60208201905061102e5f83018461100c565b92915050565b5f5f5f6060848603121561104b5761104a610f0a565b5b5f61105886828701610f54565b935050602061106986828701610f54565b925050604061107a86828701610f87565b9150509250925092565b5f60ff82169050919050565b61109981611084565b82525050565b5f6020820190506110b25f830184611090565b92915050565b5f602082840312156110cd576110cc610f0a565b5b5f6110da84828501610f54565b91505092915050565b6110ec81610f2d565b82525050565b5f6020820190506111055f8301846110e3565b92915050565b5f5f6040838503121561112157611120610f0a565b5b5f61112e85828601610f54565b925050602061113f85828601610f54565b9150509250929050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f600282049050600182168061118d57607f821691505b6020821081036111a05761119f611149565b5b50919050565b5f6060820190506111b95f8301866110e3565b6111c6602083018561100c565b6111d3604083018461100c565b949350505050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f61121282610f68565b915061121d83610f68565b9250828201905080821115611235576112346111db565b5b9291505056fea2646970667358221220915426cbdc1300c9d38b799d49ed1744b57de9c1584fc2ed48a8fd8ed7a72e9f64736f6c634300081e0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_ALLOWANCE = "allowance";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_BURN = "burn";

    public static final String FUNC_COLLECTTOKENFROMSTUDENT = "collectTokenFromStudent";

    public static final String FUNC_DECIMALS = "decimals";

    public static final String FUNC_MINT = "mint";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFER = "transfer";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final String FUNC_TRANSFERONBEHALF = "transferOnBehalf";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event APPROVAL_EVENT = new Event("Approval",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event TRANSFER_EVENT = new Event("Transfer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected STUcoin_sol_STUcoin(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected STUcoin_sol_STUcoin(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected STUcoin_sol_STUcoin(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected STUcoin_sol_STUcoin(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static ApprovalEventResponse getApprovalEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(APPROVAL_EVENT, log);
        ApprovalEventResponse typedResponse = new ApprovalEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getApprovalEventFromLog(log));
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public static TransferEventResponse getTransferEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> allowance(String owner, String spender) {
        final Function function = new Function(FUNC_ALLOWANCE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner), 
                new org.web3j.abi.datatypes.Address(160, spender)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> approve(String spender, BigInteger value) {
        final Function function = new Function(
                FUNC_APPROVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, spender), 
                new org.web3j.abi.datatypes.generated.Uint256(value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String account) {
        final Function function = new Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, account)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> burn(String from, BigInteger amount) {
        final Function function = new Function(
                FUNC_BURN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> collectTokenFromStudent(String student,
            BigInteger amount) {
        final Function function = new Function(
                FUNC_COLLECTTOKENFROMSTUDENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, student), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> decimals() {
        final Function function = new Function(FUNC_DECIMALS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> mint(String to, BigInteger amount) {
        final Function function = new Function(
                FUNC_MINT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> name() {
        final Function function = new Function(FUNC_NAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalSupply() {
        final Function function = new Function(FUNC_TOTALSUPPLY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transfer(String to, BigInteger value) {
        final Function function = new Function(
                FUNC_TRANSFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferFrom(String from, String to,
            BigInteger value) {
        final Function function = new Function(
                FUNC_TRANSFERFROM, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOnBehalf(String from, String to,
            BigInteger amount) {
        final Function function = new Function(
                FUNC_TRANSFERONBEHALF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static STUcoin_sol_STUcoin load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new STUcoin_sol_STUcoin(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static STUcoin_sol_STUcoin load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new STUcoin_sol_STUcoin(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static STUcoin_sol_STUcoin load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new STUcoin_sol_STUcoin(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static STUcoin_sol_STUcoin load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new STUcoin_sol_STUcoin(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<STUcoin_sol_STUcoin> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(STUcoin_sol_STUcoin.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<STUcoin_sol_STUcoin> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(STUcoin_sol_STUcoin.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<STUcoin_sol_STUcoin> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(STUcoin_sol_STUcoin.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<STUcoin_sol_STUcoin> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(STUcoin_sol_STUcoin.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class ApprovalEventResponse extends BaseEventResponse {
        public String owner;

        public String spender;

        public BigInteger value;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger value;
    }
}
