package com.sony.spe.mc.aws.lambda.assetrestore.common;

/**
 * Created by tthakkar on 9/26/17.
 */
public interface CommonConstants {
	
	// Environment Variables.
	String ENV_MAIN_BUCKET = "mainBucket";
	String ASSET_RESTORE_RESPONSE_SQS = "assetResponseFifoSQS";
	
    // S3 Tags.
    String TAG_CAN_BE_ARCHIVED = "CanBeArchived";
    String TAG_OP_UNIT = "OpUnit";
    String TAG_MEDIA_TYPE = "MediaType";
    String TAG_TERRITORY = "Territory";
    String TAG_ITEM_ID = "ItemId";
    String TAG_COMPANY_SYSTEM_NAME = "CompanySystemName";

    // S3 Tag Values.
    String TAG_CAN_BE_ARCHIVED_VALUE_YES = "Yes";
    String TAG_CAN_BE_ARCHIVED_VALUE_NO = "No";

    // 	File Restore Constants.
 	String RESTORE_REQUIRED = "RESTORE_REQUIRED";
 	String RESTORE_RUNNING = "RESTORE_RUNNING";
 	String RESTORE_NOT_REQUIRED = "RESTORE_NOT_REQUIRED";

    // Restore status
    String RESTORE_IN_PROGRESS = "RESTORE_IN_PROGRESS";
    String RESTORED = "RESTORED";
    String RESTORE_FAILED = "RESTORE_FAILED";

    // Time Delay.
    Integer THREE_HOUR_IN_SECONDS = 10800;
    Integer ONE_MINUTE_IN_SECONDS = 60;
    Integer FIVE_MINUTE_IN_SECONDS = 300;

    // AWS Constants
    String ASSET_RESTORE_MESSAGE_GROUP = "AssetRestoreMessageGroup";
}
