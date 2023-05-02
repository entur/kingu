package org.entur.kingu.service;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.model.job.ExportJob;
import org.entur.kingu.model.job.JobStatus;
import org.entur.kingu.time.ExportTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class NetexJobBuilder {
    public static final Logger logger = LoggerFactory.getLogger(NetexJobBuilder.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSS");


    @Autowired
    ExportTimeZone exportTimeZone;

    public ExportJob createExportJob(Exchange exchange, @Header(Exchange.FILE_PARENT) String localExportPath) {
        final String body = exchange.getIn().getBody(String.class);
        final ExportParams exportParams = ExportParams.fromString(body);
        logger.info("Task name: {}, and queryPram {}", exportParams.getName(), exportParams.getStopPlaceSearch().getVersionValidity());

        ExportJob exportJob = new ExportJob(JobStatus.PROCESSING);

        final Instant startTime = Instant.now();

        exportJob.setId(Long.valueOf(startTime.atZone(exportTimeZone.getDefaultTimeZoneId()).format(DATE_TIME_FORMATTER)));
        exportJob.setStarted(startTime);
        exportJob.setExportParams(exportParams);
        exportJob.setSubFolder(generateSubFolderName(startTime));
        String fileNameWithoutExtension = createFileNameWithoutExtension(startTime,exportParams.getName());
        exportJob.setFileName(fileNameWithoutExtension + ".zip");
        exportJob.setJobUrl("export" + "/" + exportJob.getId());

        exportJob.setLocalExportXmlFile(generateLocalFile(localExportPath, fileNameWithoutExtension,"xml"));
        exportJob.setLocalExportZipFile(generateLocalFile(localExportPath, fileNameWithoutExtension,"zip"));

        logger.info("Started export job: {}", exportJob);

        return exportJob;
    }

    private String createFileNameWithoutExtension(Instant started, String name) {
        final String fileName;
        var fileNameSuffix = started.atZone(exportTimeZone.getDefaultTimeZoneId()).format(DATE_TIME_FORMATTER);
        if (name != null && !name.isEmpty()) {
            fileName = "tiamat-export-"+name+ "-" + fileNameSuffix;
        } else {
            fileName = "tiamat-export-" + fileNameSuffix;
        }
        return fileName;
    }
    private File generateLocalFile(String localExportPath,String fileNameWithoutExtension, String extension) {
        return new File(localExportPath + File.separator + fileNameWithoutExtension + "." + extension);
    }

    private String generateSubFolderName(Instant startTime) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
        return localDateTime.getYear() + "-" + String.format("%02d", localDateTime.getMonthValue());
    }


}
