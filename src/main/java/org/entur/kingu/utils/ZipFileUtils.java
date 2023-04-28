package org.entur.kingu.utils;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileUtils {
    private ZipFileUtils() {}
    public static final Logger LOGGER = LoggerFactory.getLogger(ZipFileUtils.class);

    public static void exportToLocalZipFile(File localZipFile, File localExportXmlFile) throws IOException {

        LOGGER.info("Adding {} to zip file: {}", localExportXmlFile, localZipFile);

        localZipFile.createNewFile();

        final FileOutputStream fileOutputStream = new FileOutputStream(localZipFile,false);


        try(ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry(localExportXmlFile.getName()));
            InputStream fileInputStream = new FileInputStream(localExportXmlFile);
            ByteStreams.copy(fileInputStream,zipOutputStream);
            zipOutputStream.closeEntry();
            LOGGER.info("Written to disk {}", localZipFile);
        } catch (IOException e) {
            LOGGER.error(String.format("Could not close zip output stream for file: %s", localZipFile), e);
        }
    }
}
