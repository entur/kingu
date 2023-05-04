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

package org.entur.kingu.netex.validation;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides simple validation for references in netex file.
 */
@Component
public class NetexXmlReferenceValidator {

    private static final Logger logger = LoggerFactory.getLogger(NetexXmlReferenceValidator.class);
    public static final String REF_ELEMENT_NAME_POSTFIX = "Ref";
    public static final String COUNTRY_REF = "CountryRef";
    public static final String REF_ATTRIBUTE = "ref";
    public static final String VERSION_ATTRIBUTE = "version";
    public static final String ID_VERSION_SEPARATOR = "-";
    public static final String ID_ATTRIBUTE = "id";

    private final boolean throwOnValidationError;

    public NetexXmlReferenceValidator(@Value("${netexXmlReferenceValidator.throwOnValidationError:false}") boolean throwOnValidationError) {
        this.throwOnValidationError = throwOnValidationError;
    }

    public void validateNetexReferences(File file) throws org.entur.kingu.netex.validation.NetexReferenceValidatorException {
        try {
            validateNetexReferences(new FileInputStream(file), file.getName());
        } catch (FileNotFoundException e) {
            logger.warn("Error reading file {} - {}  ", file.getName(), e.getStackTrace());
            throw new org.entur.kingu.netex.validation.NetexReferenceValidatorException("Error reading file path " + file.getName(), e);
        }
    }

    public void validateNetexReferences(InputStream inputStream, String xmlNameForLogging) throws org.entur.kingu.netex.validation.NetexReferenceValidatorException {

        long start = System.currentTimeMillis();

        try {

            final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            // This disables DTDs entirely for that factory
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            // This causes XMLStreamException to be thrown if external DTDs are accessed.
            xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // disable external entities
            xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);

            Set<String> references = new HashSet<>();

            // List, because of intentions to detect and report duplicate identifiers.
            List<String> identifiers = new ArrayList<>();

            traverseXml(xmlStreamReader, references, identifiers);

            Set<String> distinctIdentifiers = Sets.newHashSet(identifiers);
            Set<String> invalidReferences = Sets.difference(references, distinctIdentifiers);

            if (invalidReferences.isEmpty()) {
                logger.info("{} is valid. {} distinct identifiers. {} references", xmlNameForLogging, distinctIdentifiers.size(), references.size());
            } else {
                String message = xmlNameForLogging + " is NOT valid. Invalid references detected: " + invalidReferences.size();
                logger.warn("{}: {}", message, invalidReferences);
                if (throwOnValidationError) {
                    throw new org.entur.kingu.netex.validation.NetexReferenceValidatorException(message);
                }
            }

        } catch (XMLStreamException e) {
            logger.warn("Error streaming {} - {}  ", xmlNameForLogging, e.getStackTrace());
            throw new org.entur.kingu.netex.validation.NetexReferenceValidatorException("Error streaming " + xmlNameForLogging, e);
        } finally {
            logger.info("Spent {} ms validating {}", System.currentTimeMillis() - start, xmlNameForLogging);
        }
    }

    private void traverseXml(XMLStreamReader xmlStreamReader, Set<String> references, List<String> identifiers) throws XMLStreamException {
        while (xmlStreamReader.hasNext()) {

            int eventCode = xmlStreamReader.next();

            if ((XMLStreamConstants.START_ELEMENT == eventCode)) {
                String localName = xmlStreamReader.getLocalName();
                if (localName.contains(REF_ELEMENT_NAME_POSTFIX) && !localName.contains(COUNTRY_REF)) {
                    processReference(xmlStreamReader, references);
                } else {
                    processId(xmlStreamReader, identifiers);
                }
            }
        }
    }

    private void processReference(XMLStreamReader xmlStreamReader, Set<String> references) {
        String value = getAttributeValue(REF_ATTRIBUTE, xmlStreamReader);
        if (value != null) {
            String version = getAttributeValue(VERSION_ATTRIBUTE, xmlStreamReader);
            if (version != null) {
                references.add(value + ID_VERSION_SEPARATOR + version);
            }
        }
    }

    private void processId(XMLStreamReader xmlStreamReader, List<String> identifiers) {
        String id = getAttributeValue(ID_ATTRIBUTE, xmlStreamReader);

        if (id != null) {
            String version = getAttributeValue(VERSION_ATTRIBUTE, xmlStreamReader);
            identifiers.add(id);
            if (version != null) {
                // It should be possible for a reference to not have version.
                // So both should be added
                identifiers.add(id + ID_VERSION_SEPARATOR + version);
            }
        }
    }

    private String getAttributeValue(String attribute, XMLStreamReader xmlStreamReader) {
        return xmlStreamReader.getAttributeValue(null, attribute);
    }
}

