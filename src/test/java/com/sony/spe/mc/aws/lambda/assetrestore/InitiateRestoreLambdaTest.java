package com.sony.spe.mc.aws.lambda.assetrestore;

import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.sony.spe.mc.aws.lambda.assetrestore.common.CommonConstants;
import com.sony.spe.mc.aws.lambda.assetrestore.proxy.request.AssetRestoreRequest;
import com.sony.spe.mc.aws.lambda.assetrestore.proxy.response.AssetRestoreIntermediateResponse;

/**
 * A JUnit test case to validate asset restore lambda.
 * @author Tapan Thakkar
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class InitiateRestoreLambdaTest {
	
	@Mock
    private AmazonS3 amazonS3;
	@Mock
	private ObjectMetadata metadata;
	@Captor
	private ArgumentCaptor<RestoreObjectRequest> restoreObjectRequest;
	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    
    private static AssetRestoreRequest input;

    @Before
    public void createInput() throws IOException {
        input = TestUtils.parse("/initiateRestore.json", AssetRestoreRequest.class);
        environmentVariables.set(CommonConstants.ENV_MAIN_BUCKET, "step-test-bucket");
        
        Mockito.when(amazonS3.getObjectMetadata(Mockito.anyString(), Mockito.anyString())).thenReturn(metadata);
        Mockito.when(metadata.getStorageClass()).thenReturn(StorageClass.Glacier.toString());
    }

    private Context createContext() {
        TestContext ctx = new TestContext();
        
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testInitiateRestoreLambda() {
        InitiateRestoreLambda handler = new InitiateRestoreLambda(amazonS3);
        Context ctx = createContext();

        AssetRestoreIntermediateResponse output = handler.handleRequest(input, ctx);

        // Validate output here.
        Assert.assertEquals(output.getItemId(), input.getItemId());
        Assert.assertEquals(output.getRestoreStatus(), CommonConstants.RESTORE_IN_PROGRESS);
        Assert.assertEquals(output.getFiles().size(), input.getFiles().size());
    }
}
