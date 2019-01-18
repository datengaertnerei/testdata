[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3d6a88390ce6414d810825d566ebe4a7)](https://app.codacy.com/app/datengaertnerei/testdata?utm_source=github.com&utm_medium=referral&utm_content=datengaertnerei/testdata&utm_campaign=Badge_Grade_Dashboard)
[![Dependencies](https://img.shields.io/librariesio/github/datengaertnerei/testdata.svg)](https://libraries.io/github/datengaertnerei/testdata)
[![DepShield Badge](https://depshield.sonatype.org/badges/datengaertnerei/testdata/depshield.svg)](https://depshield.github.io)
[![License Badge](https://img.shields.io/github/license/datengaertnerei/testdata.svg)](https://mit-license.org/)

# Testdata Generator

Generator for german person and address test data. It uses [OpenStreetMap](https://www.openstreetmap.org/) data to provide valid addresses as well as lists of names and personal attributes.

Persistence layer is [Hibernate](http://hibernate.org/orm/) and default setup stores data in local [HSQLDB](http://hsqldb.org/). You can use any relational database supported by Hibernate.

Reasons for using test data can be
* [General Data Protection Regulation](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32016R0679) compliance
* need for a larger data set
* need for specific data matching test requirements

## 1. Step: Export valid address data from OpenStreetMap

You need to download an OSM file from [Geofabrik](https://download.geofabrik.de/europe.html) or another provider of OpenStreetMap dumps.

```java
java -Xmx2G com.datengaertnerei.testdata.generator.OsmPbfAddressExport -f europe-latest.osm.pbf 

```

## 2. Step: Generate random persons and attach addresses

```java
java com.datengaertnerei.testdata.generator.PersonGenerator -a <amount of test person rows>

```

## 3. Step: Export legal entities with addresses from gleif.org to your database

You need to download level 1 LEI Data from [GLEIF](https://www.gleif.org/en/lei-data/gleif-concatenated-file/download-the-concatenated-file#). 

```java
java com.datengaertnerei.testdata.generator.GleifLegalEntityExport -f 20181207-gleif-concatenated-file-lei2.xml

```

## Export of test data to MS Access file for Microsoft product environments

```java
java com.datengaertnerei.testdata.util.AccessDatabaseExport 

```

Example data is derived from OpenStreetMap data and available under [Open Database License](https://opendatacommons.org/licenses/odbl/).
