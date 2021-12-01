package com.xuelang.mqstream.config;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 13:50
 * @Description: 全局配置
 */
public final class GlobalConfig {

    //App
    public static String appType = EnvUtil.get("SP_APP_TYPE", null, true);

    public static String appParams = EnvUtil.get("SP_PARAM", "");

    public static String nodeInfo = EnvUtil.get("SP_NODE_INFO", "");

    public static String userId = EnvUtil.get("SP_USER_ID", null, true);

    public static String appId = EnvUtil.get("SP_APP_ID", null, true);

    public static String nodeId = EnvUtil.get("SP_NODE_ID", null, true);

    public static String nodeGroup = EnvUtil.get("SP_NODE_GROUP", "default");

    // Api
    public static String host = EnvUtil.get("SP_HOST", null);

    public  static String port =EnvUtil.get("SP_PORT",null);

    public static Boolean hostTls = Boolean.valueOf(EnvUtil.get("SP_HOST_TLS", "false"));

    public static String apiHost = EnvUtil.get("SP_API_HOST", null);

    public static Boolean apiHostTls = Boolean.valueOf(EnvUtil.get("SP_API_HOST_TLS", "false"));

    public static String affinity = EnvUtil.get("SP_AFFINITY");

    public static String accessKey = EnvUtil.get("SP_ACCESS_KEY", null);

    public static String accessSecret = EnvUtil.get("SP_ACCESS_SECRET", null);

    public static String userIdHeaderField = EnvUtil.get("SP_USER_ID_HEADER_FIELD", "x-sp-user-id");

    public static String userSignatureHeaderField = EnvUtil.get("SP_USER_SIGNATURE_HEADER_FIELD", "x-sp-signature");

    public static String userSignVersionHeaderField = EnvUtil.get("SP_USER_SIGN_VERSION_HEADER_FIELD", "x-sp-sign-version");

    // Screenshots
    public static String screenshotsType = EnvUtil.get("SP_SCREENSHOTS_TYPE", "index");

    public static String screenshotsPattern = EnvUtil.get("SP_SCREENSHOTS_PATTERN");

    public static String screenshotsStorageKey = EnvUtil.get("SP_SCREENSHOTS_STORAGE_KEY");

    public static String screenshotsThumbnailStorageKey = EnvUtil.get("SP_SCREENSHOTS_THUMBNAIL_STORAGE_KEY");

    // Storage
    public static String storageType = EnvUtil.get("SP_STORAGE_TYPE");

    public static String ossAccessId = EnvUtil.get("SP_STORAGE_OSS_ACCESS_ID");

    public static String ossAccessKey = EnvUtil.get("SP_STORAGE_OSS_ACCESS_KEY");

    public static String ossBucketName = EnvUtil.get("SP_STORAGE_OSS_BUCKET_NAME", "suanpan");

    public static String ossEndpoint = EnvUtil.get("SP_STORAGE_OSS_ENDPOINT", "http://oss-cn-beijing.aliyuncs.com");

    public static String ossDelimiter = EnvUtil.get("SP_STORAGE_OSS_DELIMITER", "/");

    public static String ossTempStore = EnvUtil.get("SP_STORAGE_OSS_TEMP_STORE", "/tmp");

    public static Integer ossDownloadNumThreads = Integer.valueOf(EnvUtil.get("SP_STORAGE_OSS_DOWNLOAD_NUM_THREADS", "1"));

    public static String ossDownloadStoreName = EnvUtil.get("SP_STORAGE_OSS_DOWNLOAD_STORE_NAME", ".py-oss-download");

    public static Integer ossUploadNumThreads = Integer.valueOf(EnvUtil.get("SP_STORAGE_OSS_UPLOAD_NUM_THREADS", "1"));

    public static String ossUploadStoreName = EnvUtil.get("SP_STORAGE_OSS_UPLOAD_STORE_NAME", ".py-oss-upload");

    public static String storageLocalTempStore = EnvUtil.get("SP_STORAGE_LOCAL_TEMP_STORE", "/tmp");

    public static String minioAccessKey = EnvUtil.get("SP_STORAGE_MINIO_ACCESS_KEY");

    public static String minioSecretKey = EnvUtil.get("SP_STORAGE_MINIO_SECRET_KEY");

    public static String minioBucketName = EnvUtil.get("SP_STORAGE_MINIO_BUCKET_NAME", "suanpan");

    public static String minioEndpoint = EnvUtil.get("SP_STORAGE_MINIO_ENDPOINT");

    public static Boolean minioSecure = Boolean.valueOf(EnvUtil.get("SP_STORAGE_MINIO_SECURE", "false"));

    public static String minioDelimiter = EnvUtil.get("SP_STORAGE_MINIO_DELIMITER", "/");

    public static String minioTempStore = EnvUtil.get("SP_STORAGE_MINIO_TEMP_STORE", "/tmp");

    //stream
    public static String streamUserId = EnvUtil.get("SP_STREAM_USER_ID");

    public static String streamAppId = EnvUtil.get("SP_STREAM_APP_ID");

    public static String streamNodeId = EnvUtil.get("SP_NODE_ID");

    public static String streamHost = EnvUtil.get("SP_STREAM_HOST");

    public static String streamSendQueue = EnvUtil.get("SP_STREAM_SEND_QUEUE");

    public static String streamRecvQueue = EnvUtil.get("SP_STREAM_RECV_QUEUE");

    public static String streamSendQueueMaxLength = EnvUtil.get("SP_STREAM_SEND_QUEUE_MAX_LENGTH");

    public static String streamSendQueueTrimImmediately = EnvUtil.get("SP_STREAM_SEND_QUEUE_TRIM_IMMEDIATELY");

    public static String mqType = EnvUtil.get("SP_MQ_TYPE");

    public static String mqRedisHost = EnvUtil.get("SP_MQ_REDIS_HOST");

    public static Integer mqRedisPort = Integer.valueOf(EnvUtil.get("SP_MQ_REDIS_PORT", "6379"));

    public static Long streamRecvQueueDelay = Long.valueOf(EnvUtil.get("SP_STREAM_RECV_QUEUE_DELAY", "1000"));

    public static String mstorageType = EnvUtil.get("SP_MSTORAGE_TYPE");

    public static String mstorageRedisDefaultExpire = EnvUtil.get("SP_MSTORAGE_REDIS_DEFAULT_EXPIRE");

    public static String mstorageRedisHost = EnvUtil.get("SP_MSTORAGE_REDIS_HOST");

    public static String spDockerRegistryHost = EnvUtil.get("SP_DOCKER_REGISTRY_HOST");

    public static String spServiceDockerRegistryUrl = EnvUtil.get("SP_SERVICE_DOCKER_REGISTRY_URL");

    public static final int defaultLogicPort = 8002;
    public static final int logicPort = Integer.valueOf(EnvUtil.get("APP_NODE_SDK_PORT_CONFIG", GlobalConfig.defaultLogicPort + "", false));

    public static final int startPort = Integer.valueOf(EnvUtil.get("SP_PORT_START", "50000", true));
    public static final int endPort = Integer.valueOf(EnvUtil.get("SP_PORT_END", "60000", true));


}
