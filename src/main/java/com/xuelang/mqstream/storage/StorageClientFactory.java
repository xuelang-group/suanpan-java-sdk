package com.xuelang.mqstream.storage;

import com.xuelang.mqstream.config.GlobalConfig;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/5 13:59
 * @Description:
 */
public class StorageClientFactory {

  private StorageClientFactory() { }

  private volatile static StorageClient storageClient;

  public static StorageClient getStorageClient() {
    if (null == storageClient) {
      synchronized (StorageClientFactory.class) {
        if (null == storageClient) {
          if ("minio".equals(GlobalConfig.storageType)) {
            storageClient = new MinioStorageClient();
          } else if ("local".equals(GlobalConfig.storageType)) {
            storageClient = new LocalStorageClient();
          } else {
            storageClient = new OSSStorageClient();
          }
        }
      }
    }
    return storageClient;
  }
}
