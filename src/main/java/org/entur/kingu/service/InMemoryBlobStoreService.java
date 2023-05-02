package org.entur.kingu.service;


import com.google.cloud.storage.Storage;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Profile("in-memory-blobstore")
public class InMemoryBlobStoreService implements BlobStoreService {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryBlobStoreService.class);

    private final Map<String, byte[]> blobs = new HashMap<>();
    @Override
    public void upload(String fileName, InputStream inputStream) {
        try {
        logger.debug("upload blob called in in-memory blob store {}",fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        blobs.put(fileName, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Storage getStorage() {
        return null;
    }

    @Override
    public InputStream download(String fileName) {
        return null;
    }

    @Override
    public String createBlobIdName(String blobPath, String fileName) {
        return null;
    }
}
