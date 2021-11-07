package org.entur.kingu.config;

import org.entur.kingu.route.export.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
//TODO should be moved to cleint
@PropertySource(value = "${tiamat.exports.config.path}", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "tiamat")
@Configuration
public class TiamatExportConfig {
    List<ExportParams> exportJobs;

    public List<ExportParams> getExportJobs() {
        return exportJobs;
    }

    public void setExportJobs(List<ExportParams> exportJobs) {
        this.exportJobs = exportJobs;
    }
}
