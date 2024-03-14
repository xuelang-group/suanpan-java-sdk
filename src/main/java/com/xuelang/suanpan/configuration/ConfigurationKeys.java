package com.xuelang.suanpan.configuration;

public class ConfigurationKeys {
    //App
    public static final String appTypeKey = "SP_APP_TYPE";

    public static final String appParamsKey = "SP_PARAM";

    public static final String nodeInfoKey = "SP_NODE_INFO";

    public static final String userIdKey = "SP_USER_ID";

    public static final String appIdKey = "SP_APP_ID";

    public static final String nodeIdKey = "SP_NODE_ID";

    public static final String nodeGroupKey = "SP_NODE_GROUP";

    // Api
    public static final String hostKey = "SP_HOST";

    public static final String portKey = "SP_PORT";

    public static final String hostTlsKey = "SP_HOST_TLS";

    public static final String apiHostKey = "SP_API_HOST";

    public static final String apiHostTlsKey = "SP_API_HOST_TLS";

    public static final String affinityKey = "SP_AFFINITY";

    public static final String accessKey = "SP_ACCESS_KEY";

    public static final String accessSecretKey = "SP_ACCESS_SECRET";



    // Storage
    public static final String storageTypeKey = "SP_STORAGE_TYPE";

    public static final String ossAccessIdKey = "SP_STORAGE_OSS_ACCESS_ID";

    public static final String ossAccessKeyKey = "SP_STORAGE_OSS_ACCESS_KEY";

    public static final String ossBucketNameKey = "SP_STORAGE_OSS_BUCKET_NAME";

    public static final String ossEndpointKey = "SP_STORAGE_OSS_ENDPOINT";

    public static final String ossDelimiterKey = "SP_STORAGE_OSS_DELIMITER";

    public static final String ossTempStoreKey = "SP_STORAGE_OSS_TEMP_STORE";

    public static final String ossDownloadNumThreadsKey = "SP_STORAGE_OSS_DOWNLOAD_NUM_THREADS";

    public static final String ossDownloadStoreNameKey = "SP_STORAGE_OSS_DOWNLOAD_STORE_NAME";

    public static final String ossUploadNumThreadsKey = "SP_STORAGE_OSS_UPLOAD_NUM_THREADS";

    public static final String ossUploadStoreNameKey = "SP_STORAGE_OSS_UPLOAD_STORE_NAME";

    public static final String storageLocalTempStoreKey = "SP_STORAGE_LOCAL_TEMP_STORE";

    public static final String minioAccessKey = "SP_STORAGE_MINIO_ACCESS_KEY";

    public static final String minioSecretKey = "SP_STORAGE_MINIO_SECRET_KEY";

    public static final String minioBucketNameKey = "SP_STORAGE_MINIO_BUCKET_NAME";

    public static final String minioEndpointKey = "SP_STORAGE_MINIO_ENDPOINT";

    public static final String minioSecureKey = "SP_STORAGE_MINIO_SECURE";

    public static final String minioDelimiterKey = "SP_STORAGE_MINIO_DELIMITER";

    public static final String minioTempStoreKey = "SP_STORAGE_MINIO_TEMP_STORE";

    //stream
    public static final String streamUserIdKey = "SP_STREAM_USER_ID";

    public static final String streamAppIdKey = "SP_STREAM_APP_ID";

    public static final String streamNodeIdKey = "SP_NODE_ID";

    public static final String streamSendQueueKey = "SP_STREAM_SEND_QUEUE";

    public static final String streamRecvQueueKey = "SP_STREAM_RECV_QUEUE";
    public static final String streamSendQueueMaxLengthKey = "SP_STREAM_SEND_QUEUE_MAX_LENGTH";

    public static final String streamSendQueueTrimImmediatelyKey = "SP_STREAM_SEND_QUEUE_TRIM_IMMEDIATELY";

    public static final String enableP2pSendKey = "SP_ENABLE_P2P_SEND";

    public static final String mqTypeKey = "SP_MQ_TYPE";
    public static final String mqRedisHostKey = "SP_MQ_REDIS_HOST";
    public static final String mqRedisPortKey = "SP_MQ_REDIS_PORT";

    public static final String streamRecvQueueDelayKey = "SP_STREAM_RECV_QUEUE_DELAY";

    public static final String mstorageTypeKey = "SP_MSTORAGE_TYPE";

    public static final String mstorageRedisDefaultExpireKey = "SP_MSTORAGE_REDIS_DEFAULT_EXPIRE";

    public static final String mstorageRedisHostKey = "SP_MSTORAGE_REDIS_HOST";

    public static final String spDockerRegistryHostKey = "SP_DOCKER_REGISTRY_HOST";

    public static final String spServiceDockerRegistryUrlKey = "SP_SERVICE_DOCKER_REGISTRY_URL";
}
