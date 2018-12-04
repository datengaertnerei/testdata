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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

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
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

import com.datengaertnerei.testdata.domain.PostalAddress;

/**
 * @author Jens
 *
 */
public class OsmPbfAddressExport {

	private static Log log = LogFactory.getLog(OsmPbfAddressExport.class);

	private static final String COUNTRY = "DE";
	private static final String ADDR_POSTCODE = "addr:postcode";
	private static final String ADDR_HOUSENUMBER = "addr:housenumber";
	private static final String ADDR_STREET = "addr:street";
	private static final String ADDR_CITY = "addr:city";
	private static final String ADDR_COUNTRY = "addr:country";
	private static final String OPT_FILE = "file";

	public static void main(String[] args) {
		CommandLine commandLine = generateCommandLine(generateOptions(), args);
		if (commandLine == null) {
			return;
		}
		String fileName = commandLine.getOptionValue(OPT_FILE);

		Set<PostalAddress> uniqueAdresses = new ConcurrentSkipListSet<>();

		File osmFile = new File(fileName);
		PbfReader reader = new PbfReader(osmFile, 1);

		Sink sinkImplementation = new Sink() {

			public void process(EntityContainer entityContainer) {

				Entity entity = entityContainer.getEntity();
				if (entity instanceof Node) {
					processNode(uniqueAdresses, entity);
				}
			}

			private void processNode(Set<PostalAddress> unique, Entity entity) {
				Node node = (Node) entity;
				Collection<Tag> tags = node.getTags();
				String country = null;
				String city = null;
				String street = null;
				String housenumber = null;
				String postcode = null;
				int tagCount = 0;
				for (Tag tag : tags) {
					switch (tag.getKey()) {
					case ADDR_COUNTRY:
						country = tag.getValue();
						tagCount++;
						break;
					case ADDR_CITY:
						city = tag.getValue();
						tagCount++;
						break;
					case ADDR_STREET:
						street = tag.getValue();
						tagCount++;
						break;
					case ADDR_HOUSENUMBER:
						housenumber = tag.getValue();
						tagCount++;
						break;
					case ADDR_POSTCODE:
						postcode = tag.getValue();
						tagCount++;
						break;

					default: // just skip to the next tag
					}

					if (tagCount > 4 && country != null && country.equals(COUNTRY)) {

						PostalAddress pa = new PostalAddress(country, city, postcode, street, housenumber);
						unique.add(pa);
					}
				}
			}

			@Override
			public void initialize(Map<String, Object> metaData) {
				// just to comply with the interface
			}

			@Override
			public void complete() {
				// just to comply with the interface
			}

			@Override
			public void close() {
				// just to comply with the interface
			}
		};

		reader.setSink(sinkImplementation);
		reader.run();

		saveUniqueAddresses(uniqueAdresses);

	}

	private static void saveUniqueAddresses(Set<PostalAddress> uniqueAdresses) {
		final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.openSession();

		for (PostalAddress address : uniqueAdresses) {
			session.save(address);
		}

		session.close();
		sessionFactory.close();
	}

	/**
	 * "Definition" stage of command-line parsing with Apache Commons CLI.
	 * 
	 * @return Definition of command-line options.
	 */
	private static Options generateOptions() {
		final Option fileOption = Option.builder("f").required().hasArg().longOpt(OPT_FILE)
				.desc("File to be processed.").build();
		final Options options = new Options();
		options.addOption(fileOption);
		return options;
	}

	/**
	 * "Parsing" stage of command-line processing demonstrated with Apache Commons
	 * CLI.
	 *
	 * @param options              Options from "definition" stage.
	 * @param commandLineArguments Command-line arguments provided to application.
	 * @return Instance of CommandLine as parsed from the provided Options and
	 *         command line arguments; may be {@code null} if there is an exception
	 *         encountered while attempting to parse the command line options.
	 */
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
}
