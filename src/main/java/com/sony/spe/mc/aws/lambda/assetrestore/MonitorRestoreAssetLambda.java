package com.sony.spe.mc.aws.lambda.assetrestore;

import java.util.Date;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Tier;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.InvalidMessageContentsException;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sony.spe.mc.aws.lambda.assetrestore.common.CommonConstants;
import com.sony.spe.mc.aws.lambda.assetrestore.proxy.response.AssetRestoreIntermediateResponse;
import com.sony.spe.mc.aws.lambda.assetrestore.proxy.response.AssetRestoreResponse;

public class MonitorRestoreAssetLambda implements RequestHandler<AssetRestoreIntermediateResponse, AssetRestoreIntermediateResponse> {

    private AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard().build();
    private AmazonSQS amazonSQS = AmazonSQSClientBuilder.standard().build();

    private LambdaLogger Logger = null;

    private String mcMainBucket = System.getenv(CommonConstants.ENV_MAIN_BUCKET);
    private String assetRestoreResponseSqs = System.getenv(CommonConstants.ASSET_RESTORE_RESPONSE_SQS);
    	
    public MonitorRestoreAssetLambda() {
	}
    
    // For test only.
    public MonitorRestoreAssetLambda(AmazonS3 s3, AmazonSQS sqs) {
		amazonS3 = s3;
		amazonSQS = sqs;
	}
    
    @Override
    public AssetRestoreIntermediateResponse handleRequest(AssetRestoreIntermediateResponse input, Context context) {
        Logger = context.getLogger();
        Logger.log("MonitorRestoreAssetLambda - Input: " + input);
        return this.monitorRestoreProgress(input, context);
    }

    /**
     * This method will monitor on-going restores and update status.
     *
     * @param input
     * @return
     */
    private AssetRestoreIntermediateResponse monitorRestoreProgress(AssetRestoreIntermediateResponse input, Context context) {

        Boolean isRestoreCompleted = Boolean.TRUE;
        Date restoreExpirationTime = null;
        String message = "";

        try {
            if(null != input.getFiles() && !input.getFiles().isEmpty()) {
                for(String file : input.getFiles()) {
                    GetObjectMetadataRequest getObjectMetadataRequest = new GetObjectMetadataRequest(mcMainBucket, file);
                    ObjectMetadata objectMetadata = amazonS3.getObjectMetadata(getObjectMetadataRequest);
                    Boolean restoreFlag = objectMetadata.getOngoingRestore();
                    if(restoreFlag) {
                        Logger.log(String.format("Restore not completed for file %s", file));
                        isRestoreCompleted = Boolean.FALSE;
                    } else {
                        Logger.log(String.format("Restore completed for file %s", file));
                        restoreExpirationTime = objectMetadata.getRestoreExpirationTime();
                    }
                }
                // Update status for process definition.
                if(isRestoreCompleted) {
                    input.setRestoreStatus(CommonConstants.RESTORED);
                } else {
                    input.setRestoreStatus(CommonConstants.RESTORE_IN_PROGRESS);
                }
            } else {
                Logger.log(String.format("No files received to monitor restore progress for item %s.", input.getItemId()));
                input.setRestoreStatus(CommonConstants.RESTORED);
            }

            // set delay interval.
            this.updateWaitStateTimeInterval(input);

        } catch (AmazonServiceException ase) {
            message = ase.getMessage();
            input.setRestoreStatus(CommonConstants.RESTORE_FAILED);
            Logger.log(String.format("MonitorRestoreAssetLambda - AmazonServiceException - Error occurred while monitoring restore. Error %s", message));
        } catch (Exception e) {
            message = e.getMessage();
            input.setRestoreStatus(CommonConstants.RESTORE_FAILED);
            Logger.log(String.format("MonitorRestoreAssetLambda - Exception - Error occurred while monitoring restore. Error %s", message));
        }

        // Send SQS notification.
        this.sendSqsNotification(input, message, restoreExpirationTime, context.getAwsRequestId());

        return input;
    }

    /**
     * Method to update time interval for wait state.
     *
     * @param input
     */
    private void updateWaitStateTimeInterval(AssetRestoreIntermediateResponse input) {
        // set delay based on restore tier.
        if (input.getTier().equalsIgnoreCase(Tier.Expedited.toString())) {
            Logger.log(String.format("Setting delay to 1 min."));
            input.setDelay(CommonConstants.ONE_MINUTE_IN_SECONDS);
        } else {
            if(null == input.getDelay()) {
                Logger.log(String.format("Setting delay to 3 Hours."));
                input.setDelay(CommonConstants.THREE_HOUR_IN_SECONDS);
            } else {
                Logger.log(String.format("Setting delay to 5 min."));
                input.setDelay(CommonConstants.FIVE_MINUTE_IN_SECONDS);
            }
        }
    }

    /**
     * Method will connect with SQS and send request payload as a new message.
     *
     * @param queue
     * @param payload
     */
    private void sendSQSMessage(String queue, String payload) {
        try {
            String myQueueUrl = amazonSQS.getQueueUrl(assetRestoreResponseSqs).getQueueUrl();
            Logger.log(String.format("My Queue Url is %s.", myQueueUrl));
            SendMessageRequest request = new SendMessageRequest(myQueueUrl, payload);
            request.setMessageGroupId(CommonConstants.ASSET_RESTORE_MESSAGE_GROUP);
            SendMessageResult result = amazonSQS.sendMessage(request);
            Logger.log(String.format("Message sent to SQS with sequence number %s and messageId %s.", result.getSequenceNumber(), result.getMessageId()));
        } catch (UnsupportedOperationException uoe) {
            Logger.log(String.format("sendSQSMessage - UnsupportedOperationException - Error occurred while sending message %s", uoe.getMessage()));
        } catch (InvalidMessageContentsException imce) {
            Logger.log(String.format("sendSQSMessage - InvalidMessageContentsException - Error occurred while sending message %s", imce.getMessage()));
        }catch (Exception e) {
            Logger.log(String.format("sendSQSMessage - Exception - Error occurred while sending message %s", e.getMessage()));
        }
    }
    /**
     * Send SQS notification for progress update.
     *
     * @param input
     * @param message
     * @param restoreExpDate
     * @param requestId
     */
    private void sendSqsNotification(AssetRestoreIntermediateResponse input, String message, Date restoreExpDate, String requestId) {
        AssetRestoreResponse response = new AssetRestoreResponse();
        response.setItemId(input.getItemId());
        response.setMessage(message);
        response.setStatus(input.getRestoreStatus());
        response.setRestoreExpirationTime(restoreExpDate);
        response.setProcessInstanceId(requestId);
        
        String payload = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            payload = mapper.writeValueAsString(response);
            Logger.log(String.format("Sending SQS notification %s to queue %s.", payload, assetRestoreResponseSqs));
            this.sendSQSMessage(assetRestoreResponseSqs, payload);
        } catch (JsonProcessingException e) {
            Logger.log(String.format("sendSqsNotification - JsonProcessingException - Error occurred while sending message %s", e.getMessage()));
        }
    }
}
