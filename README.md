
[![License Badge](https://img.shields.io/github/license/datengaertnerei/testdata.svg)](https://mit-license.org/)

# Test data from public sources 

The generator has been moved to the new [test data service](https://github.com/datengaertnerei/test-data-service).

This repository will not be updated. The GLEIF export remains here without active maintenance.

# Export legal entities with addresses from gleif.org 

You need to download level 1 LEI Data from [GLEIF](https://www.gleif.org/en/lei-data/gleif-concatenated-file/download-the-concatenated-file#). 

```java
java com.datengaertnerei.testdata.generator.GleifLegalEntityExport -f <yyyymmdd>-gleif-concatenated-file-lei2.xml

```
