# Testdata Generator

Generator for german person and address test data. It uses [OpenStreetMap](https://www.openstreetmap.org/) data to provide valid addresses as well as lists of names and personal attributes.

Persistence layer is [Hibernate](http://hibernate.org/orm/) and default setup stores data in local [HSQLDB](http://hsqldb.org/). You can use any relational database supported by Hibernate.

Reasons for using test data can be

- [General Data Protection Regulation](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32016R0679) compliance

- need for a larger data set

- need for specific data matching test requirements

