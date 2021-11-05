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
 *
 */

package org.entur.kingu.route.export;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TiamatExportTask {

    public static final String SEPARATOR = ",";

    public String name;

    public String queryString = "";

    public String url ="";


    private TiamatExportTask() {
    }

    public TiamatExportTask(String name, String queryString) {
        this.name = name;
        this.queryString = queryString;
    }


    public TiamatExportTask(String config) {
        if (config == null || config.split(SEPARATOR).length < 1 || config.split(SEPARATOR).length > 3) {
            throw new IllegalArgumentException("Invalid config string, should contain 'name' and optionally 'queryString' separated by '" + SEPARATOR + "' : " + config);
        }

        String[] configArray = config.split(SEPARATOR);
        name = configArray[0].trim();
        if (configArray.length > 1) {
            queryString = configArray[1].trim();
        }

    }

    public String getName() {
        return name;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, this);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TiamatExportTask fromString(String string) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(string, TiamatExportTask.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
