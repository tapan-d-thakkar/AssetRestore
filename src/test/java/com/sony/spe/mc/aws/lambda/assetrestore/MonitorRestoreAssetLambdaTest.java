package com.sony.spe.mc.aws.lambda.assetrestore;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.sony.spe.mc.aws.lambda.assetrestore.common.CommonConstants;
import com.sony.spe.mc.aws.lambda.assetrestore.proxy.response.AssetRestoreIntermediateResponse;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

/**
 * A JUnit test case to validate asset restore monitor lambda function.
 * @author Tapan Thakkar
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MonitorRestoreAssetLambdaTest {
	
    private static AssetRestoreIntermediateResponse input;
    
    @Mock
    private AmazonS3 amazonS3;
    @Mock
    private S3Object s3Object;
    @Mock
    private AmazonSQS amazonSQS;
    @Captor
    private ArgumentCaptor<SendMessageRequest> sendMessageRequest;
    @Captor
    private ArgumentCaptor<GetObjectMetadataRequest> getObjectMetadataRequest;
    
    @Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    
    @Before
    public void setUp() throws IOException {
    		input = TestUtils.parse("/monitorRestore.json", AssetRestoreIntermediateResponse.class);
    		
    		environmentVariables.set(CommonConstants.ENV_MAIN_BUCKET, "step-test-bucket");
    		environmentVariables.set(CommonConstants.ASSET_RESTORE_RESPONSE_SQS, "asset-restore-response-dev.fifo");
    		
    		// Mockito.when(System.getenv(CommonConstants.ENV_MAIN_BUCKET)).thenReturn("step-test-bucket");
    		// Mockito.when(System.getenv(CommonConstants.ASSET_RESTORE_RESPONSE_SQS)).thenReturn("asset-restore-response-dev.fifo");
    		
    		ObjectMetadata metadata = new ObjectMetadata();
        metadata.setOngoingRestore(Boolean.FALSE);
        metadata.setRestoreExpirationTime(new Date());
        Mockito.when(amazonS3.getObjectMetadata(getObjectMetadataRequest.capture())).thenReturn(metadata);
        
        SendMessageResult result = new SendMessageResult();
        result.setMessageId(UUID.randomUUID().toString());
        result.setSequenceNumber(UUID.randomUUID().toString());
        Mockito.when(amazonSQS.sendMessage(sendMessageRequest.capture())).thenReturn(result);
        
        GetQueueUrlResult getQueueUrlResult = new GetQueueUrlResult();
        getQueueUrlResult.setQueueUrl("sampleQueue");
        Mockito.when(amazonSQS.getQueueUrl(Mockito.anyString())).thenReturn(getQueueUrlResult);
        
    }
    
    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("MonitorRestoreAssetLambda");
        
        // ctx.getClientContext().getEnvironment().put(CommonConstants.ENV_MAIN_BUCKET, "step-test-bucket");
        // ctx.getClientContext().getEnvironment().put(CommonConstants.ASSET_RESTORE_RESPONSE_SQS, "asset-restore-response-dev.fifo");
        return ctx;
    }

    @Test
    public void testMonitorRestoreAssetLambda() {
        MonitorRestoreAssetLambda handler = new MonitorRestoreAssetLambda(amazonS3, amazonSQS);
        Context ctx = createContext();

        AssetRestoreIntermediateResponse output = handler.handleRequest(input, ctx);

        // TODO: validate output here if needed.
        Assert.assertNotNull(output);
        Assert.assertEquals(input.getItemId(), output.getItemId());
        Assert.assertEquals(input.getFiles(), output.getFiles());
        Assert.assertEquals(input.getTier(), output.getTier());
    }
}
