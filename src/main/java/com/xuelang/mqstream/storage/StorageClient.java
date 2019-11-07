package com.xuelang.mqstream.storage;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 10:29
 * @Description:
 */
public interface StorageClient {

    List<ObjectSummary> listObjects(String bucketName, String prefix);

    List<ObjectSummary> listObjects(String bucketName, String prefix, boolean recursive);

    void deleteObject(String bucketName, String key);

    void putObject(String bucketName, String key, InputStream input);

    String generatePresignedUrl(String bucketName, String key, Date expiration);

    boolean doesObjectExist(String bucketName, String key);

    InputStream getObject(String bucketName, String key);

    String getBucketName();
}
