package com.xuelang.mqstream.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.xuelang.mqstream.api.OSSApi;
import com.xuelang.mqstream.api.response.Credentials;
import com.xuelang.mqstream.config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/9/30 16:50
 * @Description:阿里云oss
 */
@Slf4j
public class OSSStorageClient implements StorageClient {

    private OSS ossClient;

    private String expiration;

    private OSSApi ossApi = new OSSApi();

    public OSSStorageClient() {
        initClient();
        refreshClientJob();
    }

    /**
     * 初始化ossClient
     */
    public void initClient() {
        Credentials credentials = ossApi.getToken();

        if (null != credentials) {
            ossClient = new OSSClientBuilder().build(
                    GlobalConfig.ossEndpoint,
                    credentials.getAccessKeyId(),
                    credentials.getAccessKeySecret(),
                    credentials.getSecurityToken()
            );
            expiration = credentials.getExpiration();
            log.info("oss client 创建成功");
        } else {
            log.error("获取临时ak失败");
            throw new RuntimeException("获取临时ak失败");
        }
    }

    /**
     * 定时刷新client
     */
    private void refreshClientJob() {
        long epochSecond = Instant.parse(expiration).minusSeconds(Instant.now().getEpochSecond()).getEpochSecond();

        //在client过期之前20s刷新
        long intervalSecond = epochSecond - 20;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this::initClient, intervalSecond, intervalSecond, TimeUnit.SECONDS);
    }

    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    public OSS getOssClient() {
        return this.ossClient;
    }

    @Override
    public List<ObjectSummary> listObjects(String bucketName, String prefix) {
        List<ObjectSummary> summaries = new ArrayList<>();

        ObjectListing objectListing = ossClient.listObjects(bucketName, prefix);

        for (OSSObjectSummary summary : objectListing.getObjectSummaries()) {
            summaries.add(new ObjectSummary(summary.getBucketName(), summary.getKey()));
        }

        return summaries;
    }

    @Override
    public List<ObjectSummary> listObjects(String bucketName, String prefix, boolean recursive) {
        List<ObjectSummary> summaries = new ArrayList<>();

        if (recursive) {
            summaries = listObjects(bucketName, prefix);
        } else {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setPrefix(prefix);
            listObjectsRequest.setDelimiter("/");
            listObjectsRequest.setBucketName(bucketName);

            ObjectListing objectListing = ossClient.listObjects(listObjectsRequest);

            for (OSSObjectSummary summary : objectListing.getObjectSummaries()) {
                if (summary.getKey().equals(prefix)) {
                    continue;
                }
                summaries.add(new ObjectSummary(summary.getBucketName(), summary.getKey()));
            }
        }
        return summaries;
    }

    @Override
    public void deleteObject(String bucketName, String key) {
        ossClient.deleteObject(bucketName, key);
    }

    @Override
    public void putObject(String bucketName, String key, InputStream input) {
        ossClient.putObject(bucketName, key, input);
    }

    @Override
    public String generatePresignedUrl(String bucketName, String key, Date expiration) {
        URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);
        return url.toString();
    }

    @Override
    public boolean doesObjectExist(String bucketName, String key) {
        return ossClient.doesObjectExist(bucketName, key);
    }

    @Override
    public InputStream getObject(String bucketName, String key) {
        OSSObject object = ossClient.getObject(bucketName, key);
        return object.getObjectContent();
    }

    @Override
    public String getBucketName() {
        return GlobalConfig.ossBucketName;
    }
}
