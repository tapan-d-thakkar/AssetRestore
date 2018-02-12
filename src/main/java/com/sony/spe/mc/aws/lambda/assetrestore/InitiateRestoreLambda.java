package com.sony.spe.mc.aws.lambda.assetrestore;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GlacierJobParameters;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.Tier;
import com.sony.spe.mc.aws.lambda.assetrestore.common.CommonConstants;
import com.sony.spe.mc.aws.lambda.assetrestore.proxy.request.AssetRestoreRequest;
import com.sony.spe.mc.aws.lambda.assetrestore.proxy.response.AssetRestoreIntermediateResponse;

public class InitiateRestoreLambda implements RequestHandler<AssetRestoreRequest, AssetRestoreIntermediateResponse> {

    private AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard().build();
    private String mcMainBucket = System.getenv(CommonConstants.ENV_MAIN_BUCKET);
    LambdaLogger Logger = null;
    
    public InitiateRestoreLambda() {
	}
    
    // For test only.
    public InitiateRestoreLambda(AmazonS3 amazonS3) {
    		amazonS3Client = amazonS3;
    }
    
    /**
     * @param input
     * @param context
     * @return
     */
    @Override
    public AssetRestoreIntermediateResponse handleRequest(AssetRestoreRequest input, Context context) {
        Logger = context.getLogger();
        Logger.log("InitiateRestoreLambda - Input: " + input);

        // initiate restore.
        return this.initiateRestore(input);
    }

    private AssetRestoreIntermediateResponse initiateRestore(AssetRestoreRequest input) {
        AssetRestoreIntermediateResponse response = new AssetRestoreIntermediateResponse();
        response.setItemId(input.getItemId());
        response.setTier(input.getTier());
        List<String> actualFilesRestored = new ArrayList<String>();

        try {
            // Loop through all files and initiate restore.
            input.getFiles().forEach(file -> {
                String isReady = this.isReadyToRestore(mcMainBucket, file);
                switch (isReady) {
                    case CommonConstants.RESTORE_REQUIRED:
                        RestoreObjectRequest restoreObjectRequest = new RestoreObjectRequest(mcMainBucket, file);
                        restoreObjectRequest.setExpirationInDays(input.getExpirationInDays().intValue());
                        restoreObjectRequest.setGlacierJobParameters(this.getGlacierJobParameters(input.getTier()));
                        amazonS3Client.restoreObject(restoreObjectRequest);
                        Logger.log(String.format("Initiated restore for %s", file));
                        actualFilesRestored.add(file);
                        break;
                    case CommonConstants.RESTORE_RUNNING:
                        actualFilesRestored.add(file);
                        break;
                    case CommonConstants.RESTORE_NOT_REQUIRED:
                        Logger.log(String.format("Asset %s/%s is not ready to restore.", mcMainBucket, file));
                        break;
                    default:
                        Logger.log(String.format("Invalid status found for Asset %s/%s, ignoring this file.", mcMainBucket, file));
                }
            });

            response.getFiles().addAll(actualFilesRestored);
            response.setRestoreStatus(CommonConstants.RESTORE_IN_PROGRESS);
        } catch(AmazonServiceException ase) {
            response.setRestoreStatus(CommonConstants.RESTORE_FAILED);
            Logger.log(String.format("AmazonServiceException - Error occurred while initiating restore. Error: %s", ase.getMessage()));
        } catch(Exception e) {
            response.setRestoreStatus(CommonConstants.RESTORE_FAILED);
            Logger.log(String.format("Exception - Error occurred while initiating restore. Error: %s", e.getMessage()));
        }

        return response;
    }

    /**
     * @param bucketName
     * @param file
     * @return
     */
    private String isReadyToRestore(String bucketName, String file) {

        String isObjectReadyToRestore = CommonConstants.RESTORE_NOT_REQUIRED;
        try {
            ObjectMetadata objectMetadata = amazonS3Client.getObjectMetadata(bucketName, file);
            if (null != objectMetadata && null != objectMetadata.getStorageClass() && objectMetadata.getStorageClass().equalsIgnoreCase(StorageClass.Glacier.toString())) {
                // Object exists in s3, and storage is glacier
                if (null != objectMetadata.getOngoingRestore() && objectMetadata.getOngoingRestore()) {
                    // restore is already running.
                    Logger.log(String.format("Restore is already running for file %s/%s.", bucketName, file));
                    return CommonConstants.RESTORE_RUNNING;
                } else {
                    // restore is required.
                    return CommonConstants.RESTORE_REQUIRED;
                }
            } else {
                // object storage is standard, so restore not required.
                Logger.log(String.format("Storage class is %s for %s/%s, dude you playing with FIRE !!", objectMetadata.getStorageClass(), bucketName, file));
            }
        } catch (AmazonServiceException ase) {
            isObjectReadyToRestore = CommonConstants.RESTORE_NOT_REQUIRED;
            Logger.log(String.format("InitiateAssetRestore - AmazonServiceException - Error occurred while get object %s metadata from bucket %s. Error: %s", file, bucketName, ase.getMessage()));
        } catch (SdkClientException sce) {
            isObjectReadyToRestore = CommonConstants.RESTORE_NOT_REQUIRED;
            Logger.log(String.format("InitiateAssetRestore - SdkClientException - Error occurred while get object %s metadata from bucket %s. Error: %s", file, bucketName, sce.getMessage()));
        } catch (Exception e) {
            isObjectReadyToRestore = CommonConstants.RESTORE_NOT_REQUIRED;
            Logger.log(String.format("InitiateAssetRestore - Exception - Error occurred while get object %s metadata from bucket %s. Error: %s", file, bucketName, e.getMessage()));
        }

        return isObjectReadyToRestore;
    }

    /**
     * method to get Glacier restore parameters.
     *
     * @param tier
     * @return
     */
    private GlacierJobParameters getGlacierJobParameters(String tier) {
        GlacierJobParameters parameters = new GlacierJobParameters();

        if(null != tier && !tier.isEmpty()) {
            parameters.setTier(Tier.fromValue(tier));
        } else {
            parameters.setTier(Tier.Standard);
        }
        return parameters;
    }
}
