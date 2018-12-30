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

package com.datengaertnerei.testdata.generator;

import com.datengaertnerei.gleifloader.schema.AddressType;
import com.datengaertnerei.gleifloader.schema.EntityStatusEnum;
import com.datengaertnerei.gleifloader.schema.EntityType;
import com.datengaertnerei.gleifloader.schema.LEIData;
import com.datengaertnerei.gleifloader.schema.LEIRecordType;
import com.datengaertnerei.testdata.domain.LegalEntity;
import com.datengaertnerei.testdata.domain.PostalAddress;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * batch exporter class for GLEIF concatenated files It is using JAXB and LEI CDF 2.1 schema to read
 * the given file and map records to legal entity an postal address objects. Persistence is done
 * with Hibernate.
 *
 * <p>Use -f or --file argument for input data.
 *
 * @author Jens Dibbern
 */
public final class GleifLegalEntityExport {

  private static final String FILTER_COUNTRY = "DE";

  private static Log log = LogFactory.getLog(GleifLegalEntityExport.class);

  private static final String OPT_FILE = "file";

  /**
   * starts GLEIF parser and generates test data.
   *
   * @param args passed to cmd line parser
   */
  public static void main(String[] args) {
    CommandLine commandLine = generateCommandLine(generateOptions(), args);
    if (commandLine == null) {
      return;
    }
    String fileName = commandLine.getOptionValue(OPT_FILE);

    LEIData data;
    try {
      data = getLeiDataFromFile(fileName);
    } catch (JAXBException parseException) {
      log.error("Could not parse XML file.", parseException);
      return;
    }

    List<LEIRecordType> records = data.getLEIRecords().getLEIRecord();

    // initialize persistence layer and fill address list
    final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
    try (Session session = sessionFactory.openSession()) {

      for (LEIRecordType record : records) {
        EntityType entity = record.getEntity();

        if (entity.getEntityStatus().equals(EntityStatusEnum.ACTIVE)
            && entity.getLegalJurisdiction() != null
            && entity.getLegalJurisdiction().equals(FILTER_COUNTRY)) {
          // map LEI data to new legal entity object and save
          LegalEntity le = new LegalEntity();
          le.setLegalEntityIdentifier(record.getLEI());
          le.setName(entity.getLegalName().getValue());
          le.setLegalForm(entity.getLegalForm().getEntityLegalFormCode());

          le.setLegalAddress(createAddress(entity.getLegalAddress()));
          le.setHeadquarterAddress(createAddress(entity.getHeadquartersAddress()));

          session.save(le);
        }
      }
    }
    // shutdown persistence layer
    sessionFactory.close();
  }

  /**
   * Creates commons cli options.
   *
   * @return options as object
   */
  private static Options generateOptions() {
    final Option fileOption =
        Option.builder("f")
            .required()
            .hasArg()
            .longOpt(OPT_FILE)
            .desc("File to be processed.")
            .build();
    final Options options = new Options();
    options.addOption(fileOption);
    return options;
  }

  /**
   * Parses arguments and create commons cli CommandLine object.
   *
   * @param options previously created options object
   * @param commandLineArguments main arguments array
   * @return CommandLine object
   */
  private static CommandLine generateCommandLine(
      final Options options, final String[] commandLineArguments) {
    final CommandLineParser cmdLineParser = new DefaultParser();
    CommandLine commandLine = null;
    try {
      commandLine = cmdLineParser.parse(options, commandLineArguments);
    } catch (ParseException parseException) {
      log.error(
          "Unable to parse command-line arguments " + Arrays.toString(commandLineArguments),
          parseException);
    }
    return commandLine;
  }

  /**
   * Maps GLEIF AddressType to PostalAddress.
   *
   * @param address GLEIF AddressType object from JAXB parser
   * @return new PostalAddress object
   */
  private static PostalAddress createAddress(AddressType address) {
    PostalAddress newAddress = new PostalAddress();
    newAddress.setStreetAddress(address.getFirstAddressLine());
    newAddress.setAddressLocality(address.getCity());
    newAddress.setAddressCountry(address.getCountry());
    newAddress.setPostalCode(address.getPostalCode());

    return newAddress;
  }

  /**
   * Parses GLEIF data with JAXB.
   *
   * @param fileName the file name to parse
   * @return root element of GLEIF data
   * @throws JAXBException thrown during parsing
   */
  private static LEIData getLeiDataFromFile(String fileName) throws JAXBException {
    File file = new File(fileName);
    JAXBContext jaxbContext = JAXBContext.newInstance(LEIData.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    JAXBElement<?> jaxbObject = (JAXBElement<?>) jaxbUnmarshaller.unmarshal(file);
    return (LEIData) jaxbObject.getValue();
  }
}
