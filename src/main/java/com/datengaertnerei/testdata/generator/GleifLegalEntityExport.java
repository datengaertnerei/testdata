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

import com.datengaertnerei.gleifloader.schema.AddressType;
import com.datengaertnerei.gleifloader.schema.EntityStatusEnum;
import com.datengaertnerei.gleifloader.schema.EntityType;
import com.datengaertnerei.gleifloader.schema.LEIData;
import com.datengaertnerei.gleifloader.schema.LEIRecordType;
import com.datengaertnerei.testdata.domain.LegalEntity;
import com.datengaertnerei.testdata.domain.PostalAddress;

public class GleifLegalEntityExport {

	private static Log log = LogFactory.getLog(GleifLegalEntityExport.class);

	private static final String OPT_FILE = "file";

	public static void main(String[] args) {
		CommandLine commandLine = generateCommandLine(generateOptions(), args);
		if (commandLine == null) {
			return;
		}
		String fileName = commandLine.getOptionValue(OPT_FILE);

		LEIData data;
		try {
			data = getLeiDataFromFile(fileName);
		} catch (JAXBException e) {
			log.error("Could not parse XML file.", e);
			return;
		}

		List<LEIRecordType> records = data.getLEIRecords().getLEIRecord();

		// initialize persistence layer and fill address list
		final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.openSession();

		for (LEIRecordType record : records) {
			EntityType entity = record.getEntity();

			if (entity.getEntityStatus().equals(EntityStatusEnum.ACTIVE) && entity.getLegalJurisdiction() != null
					&& entity.getLegalJurisdiction().equals("DE")) {
				LegalEntity le = new LegalEntity();
				le.setLegalEntityIdentifier(record.getLEI());
				le.setName(entity.getLegalName().getValue());
				le.setLegalForm(entity.getLegalForm().getEntityLegalFormCode());

				le.setLegalAddress(createAddress(entity.getLegalAddress()));
				le.setHeadquarterAddress(createAddress(entity.getHeadquartersAddress()));

				session.save(le);
			}

		}

		// shutdown persistence layer
		session.close();
		sessionFactory.close();
	}

	private static Options generateOptions() {
		final Option fileOption = Option.builder("f").required().hasArg().longOpt(OPT_FILE)
				.desc("File to be processed.").build();
		final Options options = new Options();
		options.addOption(fileOption);
		return options;
	}

	private static CommandLine generateCommandLine(final Options options, final String[] commandLineArguments) {
		final CommandLineParser cmdLineParser = new DefaultParser();
		CommandLine commandLine = null;
		try {
			commandLine = cmdLineParser.parse(options, commandLineArguments);
		} catch (ParseException parseException) {
			log.error("Unable to parse command-line arguments " + Arrays.toString(commandLineArguments),
					parseException);
		}
		return commandLine;
	}

	private static PostalAddress createAddress(AddressType a) {
		PostalAddress newAddress = new PostalAddress();
		newAddress.setStreetAddress(a.getFirstAddressLine());
		newAddress.setAddressLocality(a.getCity());
		newAddress.setAddressCountry(a.getCountry());
		newAddress.setPostalCode(a.getPostalCode());

		return newAddress;
	}

	private static LEIData getLeiDataFromFile(String fileName) throws JAXBException {
		File file = new File(fileName);
		JAXBContext jaxbContext = JAXBContext.newInstance(LEIData.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		JAXBElement<?> o = (JAXBElement<?>) jaxbUnmarshaller.unmarshal(file);
		return (LEIData) o.getValue();
	}

}
