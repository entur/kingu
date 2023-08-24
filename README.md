# kingu
NSR/tiamat netex exporter


# How to Trigger a manual export
example message body pubsub topic
```

{"name":"03_Oslo","tariffZoneExportMode":"RELEVANT","fareZoneExportMode":"RELEVANT","groupOfStopPlacesExportMode":"RELEVANT","groupOfTariffZonesExportMode":"RELEVANT","serviceFrameExportMode":"NONE","topographicPlaceExportMode":"RELEVANT","municipalityReferences":null,"countyReferences":["KVE:TopographicPlace:03"],"countryReferences":null,"stopPlaceSearch":{"query":null,"stopTypeEnumerations":null,"submode":null,"netexIdList":null,"allVersions":false,"versionValidity":"CURRENT","withoutLocationOnly":false,"withoutQuaysOnly":false,"withDuplicatedQuayImportedIds":false,"withNearbySimilarDuplicates":false,"hasParking":false,"version":null,"tags":null,"withTags":false,"pointInTime":null}}

```

pubsub topic attributes

```
attributes.EnturNetexExportStatus="Initiated"
attributes.EnturNetexExportStatus = "Completed"
```

gcloud command to publish message
```
gcloud pubsub topics publish local.kingu.topic.netex.export --message="{\"name\":\"03_Oslo\",\"tariffZoneExportMode\":\"RELEVANT\",\"fareZoneExportMode\":\"RELEVANT\",\"groupOfStopPlacesExportMode\":\"RELEVANT\",\"groupOfTariffZonesExportMode\":\"RELEVANT\",\"serviceFrameExportMode\":\"NONE\",\"topographicPlaceExportMode\":\"RELEVANT\",\"municipalityReferences\":null,\"countyReferences\":[\"KVE:TopographicPlace:03\"],\"countryReferences\":null,\"stopPlaceSearch\":{\"query\":null,\"stopTypeEnumerations\":null,\"submode\":null,\"netexIdList\":null,\"allVersions\":false,\"versionValidity\":\"CURRENT\",\"withoutLocationOnly\":false,\"withoutQuaysOnly\":false,\"withDuplicatedQuayImportedIds\":false,\"withNearbySimilarDuplicates\":false,\"hasParking\":false,\"version\":null,\"tags\":null,\"withTags\":false,\"pointInTime\":null}}" --attribute=EnturNetexExportStatus="Initiated" --project=ent-kingu-dev

```