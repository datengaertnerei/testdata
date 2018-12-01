package com.datengaertnerei.testdata.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.datengaertnerei.testdata.domain.Person;

/**
 * @author Jens
 *
 */
public class PersonGenerator {

	private static final String RES_EYECOLORS = "eyecolors.txt";
	private static final String RES_MALE = "male.txt";
	private static final String RES_FEMALE = "female.txt";
	private static final String RES_SURNAMES = "surnames.txt";
	private static final String OPT_AMOUNT = "amount";
	private static final String FEMALE = "female";
	private static final String MALE = "male";
	private static final Object EMAIL_TEST = "@email.test";
	private Random random;
	private List<String> surnames;
	private List<String> femaleNames;
	private List<String> maleNames;
	private List<String> eyecolors;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLine commandLine = generateCommandLine(generateOptions(), args);
		int amount = 0;
		try {
			amount = Integer.parseInt(commandLine.getOptionValue(OPT_AMOUNT));
		} catch (NumberFormatException e) {
			System.err.println("ERROR: Unable to parse amount option: " + e);
			return;
		}

		final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		PersonGenerator p;
		try {
			p = new PersonGenerator();
		} catch (IOException e) {
			System.err.println("ERROR: Unable to initialize person generator: " + e);
			return;
		}
		Session session = sessionFactory.openSession();
		for (int i = 0; i < amount; i++) {
			session.save(p.createRandomPerson());
		}
		session.close();
		sessionFactory.close();
	}

	public PersonGenerator() throws IOException {
		surnames = loadValues(RES_SURNAMES);
		femaleNames = loadValues(RES_FEMALE);
		maleNames = loadValues(RES_MALE);
		eyecolors = loadValues(RES_EYECOLORS);

		random = new Random(System.currentTimeMillis());
	}

	public Person createRandomPerson() {
		String gender = random.nextBoolean() ? MALE : FEMALE;
		String firstname;
		if (MALE.equals(gender)) {
			firstname = maleNames.get(random.nextInt(maleNames.size())).trim();
		} else {
			firstname = femaleNames.get(random.nextInt(femaleNames.size())).trim();
		}
		String surname = surnames.get(random.nextInt(surnames.size())).trim();
		String eyecolor = eyecolors.get(random.nextInt(eyecolors.size())).trim();

		LocalDate dateOfBirth = createRandomDateOfBirth();
		StringBuffer email = new StringBuffer(firstname).append(surname).append(dateOfBirth.getYear())
				.append(EMAIL_TEST);
		String emailNorm = Normalizer.normalize(email.toString(), Form.NFKC).replaceAll("[^\\p{ASCII}]", "");

		int height = createRandomHeight(gender);

		return new Person(firstname, surname, gender, dateOfBirth, (int) height, eyecolor, emailNorm);
	}

	private int createRandomHeight(String gender) {
		double height;
		do {
			height = random.nextGaussian() * 10.0 + 165.0;
		} while (height < 150.0 || height > 190.0);
		if (MALE.equals(gender)) {
			height += 10.0;
		}
		return (int) Math.round(height);
	}

	private LocalDate createRandomDateOfBirth() {
		double age;
		do {
			age = random.nextGaussian() * 22.0 + 45.0;
		} while (age < 1.0 || age > 100.0);

		int month = random.nextInt(11) + 1;
		int day = 0;
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			day = random.nextInt(30) + 1;
			break;
		case 2:
			day = random.nextInt(27) + 1;
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			day = random.nextInt(29) + 1;
			break;
		}
		LocalDate dateOfBirth = LocalDate.of(LocalDate.now().getYear() - (int) Math.round(age), month, day);

		return dateOfBirth;
	}

	private List<String> loadValues(String fileName) throws IOException {
		InputStream input = getClass().getResourceAsStream(fileName);
		BufferedReader r = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		ArrayList<String> values = new ArrayList<String>();
		String line;
		while ((line = r.readLine()) != null) {
			values.add(line);
		}
		r.close();

		return values;
	}

	/**
	 * "Definition" stage of command-line parsing with Apache Commons CLI.
	 * 
	 * @return Definition of command-line options.
	 */
	private static Options generateOptions() {
		final Option fileOption = Option.builder("a").required().hasArg().longOpt(OPT_AMOUNT)
				.desc("amount of dataset entries").build();
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
