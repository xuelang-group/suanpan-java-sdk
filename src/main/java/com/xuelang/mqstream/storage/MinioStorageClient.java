package com.xuelang.mqstream.storage;

import com.xuelang.mqstream.api.OSSApi;
import com.xuelang.mqstream.api.response.Credentials;
import com.xuelang.mqstream.config.GlobalConfig;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/9/30 16:50
 * @Description:minio
 */
@Slf4j
public class MinioStorageClient implements StorageClient {

    private MinioClient minioClient;

    private OSSApi ossApi = new OSSApi();

    public MinioStorageClient() {
        initClient();
    }

    /**
     * 初始化ossClient
     */
    public void initClient() {
        Credentials credentials = ossApi.getToken();
        try {
            if (null != credentials) {
                minioClient = new MinioClient(
                        GlobalConfig.minioEndpoint,
                        credentials.getAccessKeyId(),
                        credentials.getAccessKeySecret(),
                        GlobalConfig.minioSecure);
                log.info("minio client 创建成功");
            } else {
                log.error("获取ak失败");
                throw new RuntimeException("获取ak失败");
            }
        } catch (Exception e) {
            log.error("初始化minio client异常", e);
            throw new RuntimeException("初始化minio client异常");
        }
    }

    public MinioClient getMinioClient() {
        return this.minioClient;
    }

    @Override
    public List<ObjectSummary> listObjects(String bucketName, String prefix) {
        List<ObjectSummary> summaries = new ArrayList<>();

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(bucketName, prefix);
            Iterator<Result<Item>> iterator = results.iterator();
            while (iterator.hasNext()) {
                Result<Item> next = iterator.next();
                if (!next.get().isDir()) {
                    summaries.add(new ObjectSummary(bucketName, next.get().objectName()));
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return summaries;
    }

    @Override
    public List<ObjectSummary> listObjects(String bucketName, String prefix, boolean recursive) {
        List<ObjectSummary> summaries = new ArrayList<>();

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(bucketName, prefix, recursive);
            Iterator<Result<Item>> iterator = results.iterator();
            while (iterator.hasNext()) {
                Result<Item> next = iterator.next();
                if (!next.get().isDir()) {
                    summaries.add(new ObjectSummary(bucketName, next.get().objectName()));
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return summaries;
    }

    @Override
    public void deleteObject(String bucketName, String key) {
        try {
            minioClient.removeObject(bucketName, key);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void putObject(String bucketName, String key, InputStream input) {
        try {
            minioClient.putObject(bucketName, key, input, null, null, null, null);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public String generatePresignedUrl(String bucketName, String key, Date expiration) {
        long millMinus = expiration.getTime() - new Date().getTime();

        try {
            return minioClient.getPresignedObjectUrl(Method.GET, bucketName, key, Integer.parseInt(Long.toString(millMinus / 1000)), null);
        } catch (Exception e) {
            log.error("", e);
        }

        return null;
    }

    @Override
    public boolean doesObjectExist(String bucketName, String key) {
        try {
            minioClient.getObject(bucketName, key);
            return true;
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    @Override
    public InputStream getObject(String bucketName, String key) {
        try {
            return minioClient.getObject(bucketName, key);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    @Override
    public String getBucketName() {
        return GlobalConfig.minioBucketName;
    }
}
