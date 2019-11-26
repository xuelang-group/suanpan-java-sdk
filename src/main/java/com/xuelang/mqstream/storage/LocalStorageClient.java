package com.xuelang.mqstream.storage;

import com.xuelang.mqstream.config.GlobalConfig;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/26 16:01
 * @Description:
 */
public class LocalStorageClient implements StorageClient {

    private String formatPath(String bucketName, String key) {
        return GlobalConfig.storageLocalTempStore + File.separator + bucketName + File.separator + key;
    }

    private String formatKey(String bucketName, String key) {
        String strHead = GlobalConfig.storageLocalTempStore + File.separator + bucketName;
        strHead = strHead.replaceAll("\\\\", "/");
        key = key.replaceAll("\\\\", "/");
        if (key.indexOf(strHead) >= 0) {
            return key.substring(key.indexOf(strHead) + strHead.length() + 1);
        }
        return key;
    }

    @Override
    public List<ObjectSummary> listObjects(String bucketName, String prefix) {
        return listObjects(bucketName, prefix, true);
    }

    @Override
    public List<ObjectSummary> listObjects(String bucketName, String prefix, boolean recursive) {
        List<ObjectSummary> summaries = new ArrayList<>();

        String path = formatPath(bucketName, prefix);

        File dir = new File(path);
        if (!dir.exists()) {
            return summaries;
        }

        Collection<File> files = FileUtils.listFiles(dir, null, recursive);

        files.forEach(file -> {
            summaries.add(new ObjectSummary(bucketName, formatKey(bucketName, file.getAbsolutePath())));
        });

        return summaries;
    }

    @Override
    public void deleteObject(String bucketName, String key) {
        String path = formatPath(bucketName, key);

        File file = new File(path);

        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    @Override
    public void putObject(String bucketName, String key, InputStream input) {
        String path = formatPath(bucketName, key);

        try (BufferedInputStream in = new BufferedInputStream(input);
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path))) {

            int len;
            byte[] b = new byte[1024];
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException("写文件失败", e);
        }
    }

    @Override
    public String generatePresignedUrl(String bucketName, String key, Date expiration) {
        String path = formatPath(bucketName, key);
        return "file://" + path;
    }

    @Override
    public boolean doesObjectExist(String bucketName, String key) {
        String path = formatPath(bucketName, key);
        File file = new File(path);
        return file.exists();
    }

    @Override
    public InputStream getObject(String bucketName, String key) {
        try {
            String path = formatPath(bucketName, key);
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                return new FileInputStream(file);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException("读文件异常", e);
        }
    }

    @Override
    public String getBucketName() {
        return "suanpan";
    }
}
