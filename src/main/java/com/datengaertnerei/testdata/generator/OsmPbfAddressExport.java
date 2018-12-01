package com.datengaertnerei.testdata.generator;

import java.io.File;
import java.io.IOException;
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

public class OsmPbfAddressExport {

	private static final String COUNTRY = "DE";
	private static final String ADDR_POSTCODE = "addr:postcode";
	private static final String ADDR_HOUSENUMBER = "addr:housenumber";
	private static final String ADDR_STREET = "addr:street";
	private static final String ADDR_CITY = "addr:city";
	private static final String ADDR_COUNTRY = "addr:country";
	private static final String OPT_FILE = "file";

	public static void main(String[] args) throws IOException {
		CommandLine commandLine = generateCommandLine(generateOptions(), args);
		String fileName = commandLine.getOptionValue(OPT_FILE);
		
		Set<PostalAddress> unique = new ConcurrentSkipListSet<PostalAddress>();

		File osmFile = new File(fileName);
		PbfReader reader = new PbfReader(osmFile, 1);

		Sink sinkImplementation = new Sink() {

			public void process(EntityContainer entityContainer) {

				Entity entity = entityContainer.getEntity();
				if (entity instanceof Node) {
					Node node = (Node) entity;
					Collection<Tag> tags = node.getTags();
					String country = null;
					String city = null;
					String street = null;
					String housenumber = null;
					String postcode = null;
					int tagCount = 0;
					for (Tag tag : tags) {
						if (tag.getKey().equals(ADDR_COUNTRY)) {
							country = tag.getValue();
							tagCount++;
						}
						if (tag.getKey().equals(ADDR_CITY)) {
							city = tag.getValue();
							tagCount++;
						}
						if (tag.getKey().equals(ADDR_STREET)) {
							street = tag.getValue();
							tagCount++;
						}
						if (tag.getKey().equals(ADDR_HOUSENUMBER)) {
							housenumber = tag.getValue();
							tagCount++;
						}
						if (tag.getKey().equals(ADDR_POSTCODE)) {
							postcode = tag.getValue();
							tagCount++;
						}

						if (tagCount > 4 && country.equals(COUNTRY)) {

							PostalAddress pa = new PostalAddress(country, city, postcode, street, housenumber);
							unique.add(pa);
						}
					}
				}
			}

			@Override
			public void initialize(Map<String, Object> metaData) {

			}

			@Override
			public void complete() {

			}

			@Override
			public void close() {

			}
		};

		reader.setSink(sinkImplementation);
		reader.run();

		final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.openSession();
		
		for(PostalAddress address : unique) {
			session.save(address);
		}
		
		session.close();
		sessionFactory.close();
		
	}

	/**
	 * "Definition" stage of command-line parsing with Apache Commons CLI.
	 * @return Definition of command-line options.
	 */
	private static Options generateOptions()
	{
	   final Option fileOption = Option.builder("f")
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
			System.err.println("ERROR: Unable to parse command-line arguments " + Arrays.toString(commandLineArguments)
					+ " due to: " + parseException);
		}
		return commandLine;
	}	
}
