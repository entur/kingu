# Kingu

Kingu is a NeTEx (Network Timetable Exchange) exporter service for NSR (National Stop Register) / Tiamat. It exports stop place data and related entities to the NeTEx XML format.

## Overview

Kingu is a Spring Boot application that:
- Listens for export requests via Google Pub/Sub
- Queries the Tiamat database for stop places and related data
- Generates NeTEx-compliant XML exports
- Validates the generated NeTEx references
- Uploads the export files to Google Cloud Storage

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Pub/Sub       │────▶│     Kingu       │────▶│      GCS        │
│   (trigger)     │     │   (exporter)    │     │   (storage)     │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │    Tiamat DB    │
                        │   (PostgreSQL)  │
                        └─────────────────┘
```

### Key Components

| Component | Description |
|-----------|-------------|
| `NetexExportRoute` | Apache Camel routes for handling export workflow |
| `StreamingPublicationDelivery` | Core export logic - streams data to NeTEx XML |
| `NetexXmlReferenceValidator` | Validates internal references in generated NeTEx |
| `StopPlaceRepository` | Database access for stop places and related entities |

## Technology Stack

- **Java 21**
- **Spring Boot 3.x**
- **Apache Camel** - Integration framework for message routing
- **Hibernate/JPA** - ORM for database access
- **PostgreSQL with PostGIS** - Geospatial database
- **Google Cloud Pub/Sub** - Message queue for export triggers
- **Google Cloud Storage** - Export file storage
- **NeTEx Java Model** - NeTEx XML schema bindings

## Export Parameters

The export is triggered by a JSON message with the following structure:

| Parameter | Type               | Description                                      |
|-----------|--------------------|--------------------------------------------------|
| `name` | String             | Export name (e.g., "03_Oslo")                    |
| `tariffZoneExportMode` | ExportMode         | How to export tariff zones                       |
| `fareZoneExportMode` | ExportMode         | How to export fare zones                         |
| `groupOfStopPlacesExportMode` | ExportMode         | How to export stop place groups                  |
| `groupOfTariffZonesExportMode` | ExportMode         | How to export tariff zone groups                 |
| `topographicPlaceExportMode` | ExportMode         | How to export topographic places                 |
| `municipalityReferences` | List&lt;String&gt; | Filter by municipality IDs                       |
| `countyReferences` | List&lt;String&gt; | Filter by county IDs (e.g., "KVE:TopographicPlace:03") |
| `countryReferences` | List&lt;String&gt; | Filter by country IDs                            |
| `stopPlaceSearch` | StopPlaceSearch    | Advanced stop place search criteria              |
| `exportMultiSurface` | bolean             | default is false for backward compatibility      |

### Export Modes

| Mode | Description |
|------|-------------|
| `NONE` | Do not export this entity type |
| `RELEVANT` | Export only entities related to the selected stop places |
| `ALL` | Export all entities of this type |

### Stop Place Search Options

| Parameter | Type | Description |
|-----------|------|-------------|
| `query` | String | Text search query |
| `stopTypeEnumerations` | List | Filter by stop types |
| `netexIdList` | List&lt;String&gt; | Specific NeTEx IDs to export |
| `allVersions` | boolean | Include all versions (default: false) |
| `versionValidity` | VersionValidity | Version filter (CURRENT, ALL, etc.) |
| `pointInTime` | Instant | Export data valid at specific time |

## NeTEx Output Structure

The export generates a NeTEx `PublicationDelivery` containing:

- **SiteFrame** - Stop places, quays, tariff zones, topographic places
- **ServiceFrame** - Scheduled stop points, passenger stop assignments
- **FareFrame** - Fare zones, group of tariff zones
- **ResourceFrame** - Additional resources

## Configuration

Key configuration properties (via environment variables or application.properties):

```properties
# Database
spring.datasource.url=jdbc:postgresql://host:port/database
spring.datasource.username=username

# Pub/Sub
pubsub.kingu.outbound.topic.netex.export=topic-name
pubsub.kingu.inbound.subscription.netex.export=subscription-name

# GCS Storage
blobstore.gcs.bucket.name=bucket-name
blobstore.gcs.blob.path=export

# Export settings
async.export.path=/tmp
netexXmlReferenceValidator.throwOnValidationError=false
```

## Running Locally

### Prerequisites

- Java 21
- Docker (for PostgreSQL/PostGIS and Pub/Sub emulator)
- Maven

### Build

```bash
./mvnw clean install
```

### Run Tests

```bash
./mvnw test
```

### Run Application

```bash
./mvnw spring-boot:run
```

## How to Trigger a Manual Export

### Example Message Body (Pub/Sub)

```json
{
  "name": "03_Oslo",
  "tariffZoneExportMode": "RELEVANT",
  "fareZoneExportMode": "RELEVANT",
  "groupOfStopPlacesExportMode": "RELEVANT",
  "groupOfTariffZonesExportMode": "RELEVANT",
  "topographicPlaceExportMode": "RELEVANT",
  "municipalityReferences": null,
  "countyReferences": ["KVE:TopographicPlace:03"],
  "countryReferences": null,
  "exportMultiSurface": false,
  "stopPlaceSearch": {
    "query": null,
    "stopTypeEnumerations": null,
    "submode": null,
    "netexIdList": null,
    "allVersions": false,
    "versionValidity": "CURRENT",
    "withoutLocationOnly": false,
    "withoutQuaysOnly": false,
    "withDuplicatedQuayImportedIds": false,
    "withNearbySimilarDuplicates": false,
    "hasParking": false,
    "version": null,
    "tags": null,
    "withTags": false,
    "pointInTime": null
  }
}
```

### Pub/Sub Topic Attributes

```
attributes.EnturNetexExportStatus="Initiated"   # When starting export
attributes.EnturNetexExportStatus="Completed"   # When export is done
```

### gcloud Command to Publish Message

```bash
gcloud pubsub topics publish local.kingu.topic.netex.export \
  --message='{"name":"03_Oslo","tariffZoneExportMode":"RELEVANT","fareZoneExportMode":"RELEVANT","groupOfStopPlacesExportMode":"RELEVANT","groupOfTariffZonesExportMode":"RELEVANT","topographicPlaceExportMode":"RELEVANT","municipalityReferences":null,"countyReferences":["KVE:TopographicPlace:39"],"countryReferences":null,"exportMultiSurface":true,"stopPlaceSearch":{"query":null,"stopTypeEnumerations":null,"submode":null,"netexIdList":null,"allVersions":false,"versionValidity":"CURRENT","withoutLocationOnly":false,"withoutQuaysOnly":false,"withDuplicatedQuayImportedIds":false,"withNearbySimilarDuplicates":false,"hasParking":false,"version":null,"tags":null,"withTags":false,"pointInTime":null}}' \
  --attribute=EnturNetexExportStatus="Initiated" \
  --project=ent-kingu-dev
```

## Export Workflow

1. **Receive Message** - Pub/Sub message triggers the export
2. **Parse Parameters** - JSON message is parsed into `ExportParams`
3. **Create Export Job** - Job is created with status `PROCESSING`
4. **Query Database** - Stop places and related entities are queried
5. **Stream to NeTEx** - Data is streamed to NeTEx XML format
6. **Validate References** - Internal NeTEx references are validated
7. **Create ZIP** - XML is compressed to ZIP file
8. **Upload to GCS** - ZIP file is uploaded to Cloud Storage
9. **Complete Job** - Job status is set to `FINISHED`
10. **Cleanup** - Local temporary files are deleted

## Testing

The project includes:

- **Unit tests** - For individual components
- **Integration tests** - Full export flow with Testcontainers
- **Route tests** - Apache Camel route testing

Run specific test class:
```bash
./mvnw test -Dtest=StreamingPublicationDeliveryIntegrationTest
```

## Related Projects

- **Tiamat** - The stop place register that Kingu exports from
- **NeTEx** - Network Timetable Exchange standard