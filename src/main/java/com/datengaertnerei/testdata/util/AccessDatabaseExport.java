/*MIT License

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
*/

package com.datengaertnerei.testdata.util;

import com.datengaertnerei.testdata.domain.Person;
import com.datengaertnerei.testdata.domain.PostalAddress;
import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

/**
 * Utility class for access database file export of person and address test data.
 *
 * @author Jens Dibbern
 */
public class AccessDatabaseExport {

  private static Log log = LogFactory.getLog(AccessDatabaseExport.class);

  /**
   * Starts up export to given file name.
   *
   * @param args passed to cmd line parser
   */
  public static void main(String[] args) {

    final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
    Session session = sessionFactory.openSession();
    List<Object[]> personRows = getPersonRows(session);
    List<Object[]> postalAddressRows = getPostalAddressRows(session);
    session.close();
    sessionFactory.close();

    try {
      Database db = DatabaseBuilder.create(Database.FileFormat.V2010, new File("testdata.mdb"));
      savePersons(personRows, db);
      savePostalAddresses(postalAddressRows, db);

      db.close();
    } catch (IOException | SQLException persistenceException) {
      log.error("Could not save to file.", persistenceException);
    }
  }

  /**
   * Loads previously persisted person records.
   * 
   * @param session Hibernate persistence session
   * @return persons objects as list of object arrays  
   */
  private static List<Object[]> getPersonRows(Session session) {
    CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
    CriteriaQuery<Person> criteriaQuery = criteriaBuilder.createQuery(Person.class);
    Root<Person> root = criteriaQuery.from(Person.class);
    criteriaQuery.select(root);
    Query<Person> query = session.createQuery(criteriaQuery);
    List<Person> result = query.list();
    List<Object[]> rows = new LinkedList<>();
    for (Person person : result) {
      Object[] row = new Object[8];
      row[0] = person.getId();
      row[1] = person.getGivenName();
      row[2] = person.getFamilyName();
      row[3] = person.getBirthName();
      row[4] = person.getGender();
      row[5] = Date.valueOf(person.getBirthDate());
      row[6] = person.getEmail();
      row[7] = person.getAddress().getId();
      rows.add(row);
    }
    return rows;
  }

  /**
   * Loads previously persisted postal address records.
   * 
   * @param session Hibernate persistence session
   * @return postal address objects as list of object arrays  
   */
  private static List<Object[]> getPostalAddressRows(Session session) {
    CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
    CriteriaQuery<PostalAddress> criteriaQuery = criteriaBuilder.createQuery(PostalAddress.class);
    Root<PostalAddress> root = criteriaQuery.from(PostalAddress.class);
    criteriaQuery.select(root);
    Query<PostalAddress> query = session.createQuery(criteriaQuery);
    List<PostalAddress> result = query.list();
    List<Object[]> rows = new LinkedList<>();
    for (PostalAddress address : result) {
      Object[] row = new Object[7];
      row[0] = address.getId();
      row[1] = address.getAddressCountry();
      row[2] = address.getAddressLocality();
      row[3] = address.getPostalCode();
      row[4] = address.getStreetAddress();
      row[5] = address.getHouseNumber();
      rows.add(row);
    }
    return rows;
  }

  /**
   * Saves person records to database file.
   * 
   * @param rows persons as list of object arrays
   * @param db jackcess database object
   * @throws IOException file operation failed, fix and try again
   * @throws SQLException database operation failed, fix and try again
   */
  private static void savePersons(List<Object[]> rows, Database db)
      throws IOException, SQLException {
    Table personTable =
        new TableBuilder("Person")
            .addColumn(new ColumnBuilder("Id").setSQLType(Types.INTEGER))
            .addColumn(new ColumnBuilder("GivenName").setSQLType(Types.VARCHAR))
            .addColumn(new ColumnBuilder("FamilyName").setSQLType(Types.VARCHAR))
            .addColumn(new ColumnBuilder("BirthName").setSQLType(Types.VARCHAR))
            .addColumn(new ColumnBuilder("Gender").setSQLType(Types.VARCHAR))
            .addColumn(new ColumnBuilder("BirthDate").setSQLType(Types.DATE))
            .addColumn(new ColumnBuilder("Email").setSQLType(Types.VARCHAR))
            .addColumn(new ColumnBuilder("FK_Address").setSQLType(Types.INTEGER))
            .toTable(db);

    personTable.addRows(rows);
  }

  /**
   * Saves postal address records to database file.
   * 
   * @param rows postal addresses as list of object arrays
   * @param db jackcess database object
   * @throws IOException file operation failed, fix and try again
   * @throws SQLException database operation failed, fix and try again
   */
  private static void savePostalAddresses(List<Object[]> rows, Database db)
      throws IOException, SQLException {
    Table addressTable =
        new TableBuilder("PostalAddress")
            .addColumn(new ColumnBuilder("Id").setSQLType(Types.INTEGER))
            .addColumn(new ColumnBuilder("AddressCountry").setSQLType(Types.VARCHAR))
            .addColumn(new ColumnBuilder("AddressLocality").setSQLType(Types.VARCHAR))
            .addColumn(new ColumnBuilder("PostalCode").setSQLType(Types.VARCHAR))
            .addColumn(new ColumnBuilder("StreetAddress").setSQLType(Types.VARCHAR))
            .addColumn(new ColumnBuilder("HouseNumber").setSQLType(Types.VARCHAR))
            .toTable(db);

    addressTable.addRows(rows);
  }
}
