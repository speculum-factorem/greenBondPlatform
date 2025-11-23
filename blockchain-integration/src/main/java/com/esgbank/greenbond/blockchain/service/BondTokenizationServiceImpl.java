package com.esgbank.greenbond.blockchain.service;

import com.esgbank.greenbond.blockchain.model.BlockchainTransactionResult;
import com.esgbank.greenbond.blockchain.model.BondInfo;
import com.esgbank.greenbond.blockchain.model.FundVerificationResult;
import com.esgbank.greenbond.blockchain.proto.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.MDC;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class BondTokenizationServiceImpl extends BondTokenizationServiceGrpc.BondTokenizationServiceImplBase {

    private final BlockchainService blockchainService;

    @Override
    public void tokenizeBond(TokenizeBondRequest request, StreamObserver<TokenizeBondResponse> responseObserver) {
        String requestId = MDC.get("requestId");
        log.info("Processing bond tokenization request: {}, requestId: {}", request.getBondId(), requestId);

        try {
            BlockchainTransactionResult result = blockchainService.tokenizeBond(
                    request.getBondId(),
                    request.getTotalSupply(),
                    request.getFaceValue(),
                    request.getCouponRate(),
                    request.getMaturityDate(),
                    request.getProjectWallet(),
                    request.getVerifierReportHash(),
                    request.getIssuerWallet()
            );

            TokenizeBondResponse response = TokenizeBondResponse.newBuilder()
                    .setTransactionHash(result.getTransactionHash())
                    .setBondAddress(result.getContractAddress())
                    .setStatus("SUCCESS")
                    .setMessage("Bond successfully tokenized")
                    .setBlockNumber(result.getBlockNumber())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("Bond tokenization completed successfully for bond: {}, txHash: {}",
                    request.getBondId(), result.getTransactionHash());

        } catch (Exception e) {
            log.error("Bond tokenization failed for bond: {}. Error: {}",
                    request.getBondId(), e.getMessage(), e);

            TokenizeBondResponse response = TokenizeBondResponse.newBuilder()
                    .setTransactionHash("")
                    .setBondAddress("")
                    .setStatus("FAILED")
                    .setMessage("Tokenization failed: " + e.getMessage())
                    .setBlockNumber(0)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getBondStatus(BondStatusRequest request, StreamObserver<BondStatusResponse> responseObserver) {
        log.debug("Fetching bond status for bond: {}", request.getBondId());

        try {
            BondInfo bondInfo = blockchainService.getBondStatus(
                    request.getBondId(),
                    request.getTransactionHash()
            );

            BondStatusResponse response = BondStatusResponse.newBuilder()
                    .setBondId(bondInfo.getBondId())
                    .setStatus(bondInfo.getStatus())
                    .setBondAddress(bondInfo.getBondAddress())
                    .setTransactionHash(bondInfo.getTransactionHash())
                    .setBlockNumber(bondInfo.getBlockNumber())
                    .setOwner(bondInfo.getOwner())
                    .setTotalSupply(bondInfo.getTotalSupply())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Failed to get bond status for bond: {}. Error: {}",
                    request.getBondId(), e.getMessage());

            BondStatusResponse response = BondStatusResponse.newBuilder()
                    .setBondId(request.getBondId())
                    .setStatus("ERROR")
                    .setBondAddress("")
                    .setTransactionHash("")
                    .setBlockNumber(0)
                    .setOwner("")
                    .setTotalSupply("")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void recordImpactData(ImpactDataRequest request, StreamObserver<ImpactDataResponse> responseObserver) {
        log.info("Recording impact data for bond: {}, metric: {}",
                request.getBondId(), request.getMetricType());

        try {
            BlockchainTransactionResult result = blockchainService.recordImpactData(
                    request.getBondId(),
                    request.getMetricType(),
                    request.getValue(),
                    request.getUnit(),
                    request.getTimestamp(),
                    request.getSource(),
                    request.getDataHash()
            );

            ImpactDataResponse response = ImpactDataResponse.newBuilder()
                    .setTransactionHash(result.getTransactionHash())
                    .setStatus("SUCCESS")
                    .setImpactId(generateImpactId(request.getBondId(), request.getMetricType()))
                    .setBlockNumber(result.getBlockNumber())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("Impact data recorded successfully for bond: {}, txHash: {}",
                    request.getBondId(), result.getTransactionHash());

        } catch (Exception e) {
            log.error("Failed to record impact data for bond: {}. Error: {}",
                    request.getBondId(), e.getMessage(), e);

            ImpactDataResponse response = ImpactDataResponse.newBuilder()
                    .setTransactionHash("")
                    .setStatus("FAILED")
                    .setImpactId("")
                    .setBlockNumber(0)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void verifyFundUsage(FundUsageRequest request, StreamObserver<FundUsageResponse> responseObserver) {
        log.info("Verifying fund usage for bond: {}, transaction: {}",
                request.getBondId(), request.getTransactionHash());

        try {
            FundVerificationResult result = blockchainService.verifyFundUsage(
                    request.getBondId(),
                    request.getTransactionHash(),
                    request.getAmount(),
                    request.getRecipient(),
                    request.getPurpose(),
                    request.getDocumentHash()
            );

            FundUsageResponse response = FundUsageResponse.newBuilder()
                    .setVerificationHash(result.getVerificationHash())
                    .setStatus("SUCCESS")
                    .setIsVerified(result.isVerified())
                    .setMessage(result.getMessage())
                    .setBlockNumber(result.getBlockNumber())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("Fund usage verification completed for bond: {}, verified: {}",
                    request.getBondId(), result.isVerified());

        } catch (Exception e) {
            log.error("Fund usage verification failed for bond: {}. Error: {}",
                    request.getBondId(), e.getMessage(), e);

            FundUsageResponse response = FundUsageResponse.newBuilder()
                    .setVerificationHash("")
                    .setStatus("FAILED")
                    .setIsVerified(false)
                    .setMessage("Verification failed: " + e.getMessage())
                    .setBlockNumber(0)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private String generateImpactId(String bondId, String metricType) {
        return bondId + "-" + metricType + "-" + System.currentTimeMillis();
    }
}