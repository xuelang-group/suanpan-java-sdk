package com.xuelang.mqstream.storage;

import com.xuelang.mqstream.api.response.Credentials;
import com.xuelang.mqstream.config.GlobalConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
                minioClient = MinioClient.builder().endpoint(GlobalConfig.minioEndpoint).credentials(credentials.getAccessKeyId(), credentials.getAccessKeySecret()).build();
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
            ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).build();
            Iterable<Result<Item>> results = minioClient.listObjects(args);
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
            ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(recursive).build();
            Iterable<Result<Item>> results = minioClient.listObjects(args);
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
            RemoveObjectArgs args = RemoveObjectArgs.builder().bucket(bucketName).object(key).build();
            minioClient.removeObject(args);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void putObject(String bucketName, String key, InputStream input) {
        try {
            PutObjectArgs put = PutObjectArgs.builder().bucket(bucketName).object(key).stream(input, input.available(), input.available()).build();
            minioClient.putObject(put);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public String generatePresignedUrl(String bucketName, String key, Date expiration) {
        long millMinus = expiration.getTime() - new Date().getTime();

        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName).object(key).expiry(Integer.parseInt(Long.toString(millMinus / 1000))).build();
            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            log.error("", e);
        }

        return null;
    }

    @Override
    public boolean doesObjectExist(String bucketName, String key) {
        try {
            minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(key).build());
            return true;
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    @Override
    public InputStream getObject(String bucketName, String key) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(key).build());
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
