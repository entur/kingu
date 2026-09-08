/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.entur.kingu.exporter.async;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.exporter.StreamingPublicationDelivery;
import org.entur.kingu.model.job.ExportJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers the bug where {@link NetexExporter#process} caught any {@link IOException} thrown while streaming
 * the export and replaced it with a brand new, generic "Unable to create file" exception - discarding the
 * real exception's type, message and stack trace. That made a real failure mid-export (e.g. a database
 * connectivity issue) indistinguishable in the logs from the local export file already existing.
 */
class NetexExporterTest {

    @TempDir
    Path tempDir;

    private StreamingPublicationDelivery streamingPublicationDelivery;
    private NetexExporter netexExporter;

    @BeforeEach
    void setUp() {
        streamingPublicationDelivery = mock(StreamingPublicationDelivery.class);
        netexExporter = new NetexExporter(streamingPublicationDelivery);
    }

    @Test
    void processPropagatesTheOriginalExceptionThrownWhileStreaming() throws Exception {
        File xmlFile = tempDir.resolve("tiamat-export-test.xml").toFile();

        ExportJob exportJob = new ExportJob();
        ExportParams exportParams = new ExportParams();
        exportParams.setName("test");
        exportJob.setExportParams(exportParams);
        exportJob.setLocalExportXmlFile(xmlFile.getAbsolutePath());
        exportJob.setLocalExportZipFile(tempDir.resolve("tiamat-export-test.zip").toFile().getAbsolutePath());

        IOException realCause = new IOException("Connection reset by peer while streaming publication delivery");
        doThrow(realCause).when(streamingPublicationDelivery).stream(any(ExportParams.class), any(OutputStream.class), eq(true));

        Exchange exchange = mockExchangeCarrying(exportJob);

        IOException thrown = assertThrows(IOException.class, () -> netexExporter.process(exchange));

        assertSame(realCause, thrown,
                "The original exception thrown while streaming should propagate unchanged, not be replaced with a generic 'Unable to create file' exception");
    }

    @Test
    void processThrowsWhenTheLocalExportXmlFileAlreadyExists() throws Exception {
        File xmlFile = tempDir.resolve("tiamat-export-existing.xml").toFile();
        assertTrue(xmlFile.createNewFile(), "test setup: file should not already exist");

        ExportJob exportJob = new ExportJob();
        ExportParams exportParams = new ExportParams();
        exportParams.setName("test");
        exportJob.setExportParams(exportParams);
        exportJob.setLocalExportXmlFile(xmlFile.getAbsolutePath());

        Exchange exchange = mockExchangeCarrying(exportJob);

        IOException thrown = assertThrows(IOException.class, () -> netexExporter.process(exchange));

        assertTrue(thrown.getMessage().contains(xmlFile.getAbsolutePath()),
                "Should report the path of the xml file that could not be created, got: " + thrown.getMessage());
        verifyNoInteractions(streamingPublicationDelivery);
    }

    private Exchange mockExchangeCarrying(ExportJob exportJob) {
        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
        when(exchange.getIn()).thenReturn(message);
        when(message.getHeader(Exchange.BREADCRUMB_ID)).thenReturn("test-breadcrumb");
        when(message.getBody(ExportJob.class)).thenReturn(exportJob);
        return exchange;
    }
}
