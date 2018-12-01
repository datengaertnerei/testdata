# Testdata Generator

Generator for german person and address test data.

```java
java com.datengaertnerei.testdata.generator.PersonGenerator -a <amount of test person rows>

```

You need to download an OSM file from [Geofabrik](https://download.geofabrik.de/europe.html) or another provider of OpenStreetMap dumps.

```java
java -Xmx2G com.datengaertnerei.testdata.generator.OsmPbfAddressExport -f europe-latest.osm.pbf 

```

