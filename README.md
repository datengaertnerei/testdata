[![DepShield Badge](https://depshield.sonatype.org/badges/datengaertnerei/testdata/depshield.svg)](https://depshield.github.io)

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

## License

MIT License

Copyright (c) 2018 Jens Dibbern

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.