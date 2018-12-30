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

import com.datengaertnerei.testdata.domain.Person;
import com.datengaertnerei.testdata.domain.PostalAddress;
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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
import org.hibernate.query.Query;

/**
 * Random person generator.
 *
 * @author Jens Dibbern
 */
public class PersonGenerator {

  private static Log log = LogFactory.getLog(PersonGenerator.class);

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

  private List<PostalAddress> addressList;

  /**
   * Starts random person generator, creates the given amount of records and persists them.
   *
   * @param args passed to cmd line parser
   */
  public static void main(String[] args) {
    CommandLine commandLine = generateCommandLine(generateOptions(), args);
    if (commandLine == null) {
      return;
    }
    int amount = 0;
    try {
      amount = Integer.parseInt(commandLine.getOptionValue(OPT_AMOUNT));
    } catch (NumberFormatException parseException) {
      log.error("Unable to parse amount option.", parseException);
      return;
    }

    PersonGenerator generator;
    try {
      generator = new PersonGenerator();
    } catch (IOException initException) {
      log.error("Unable to initialize person generator.", initException);
      return;
    }

    // initialize persistence layer and fill address list
    final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
    Session session = sessionFactory.openSession();
    generator.getAddressList(session);

    // create given amount of random person rows
    for (int i = 0; i < amount; i++) {
      session.save(generator.createRandomPerson());
    }
    // shutdown persistence layer
    session.close();
    sessionFactory.close();
  }

  /**
   * Loads previously persisted PostalAddress object from database.
   *
   * @param session Hibernate persistence session
   */
  private void getAddressList(Session session) {
    CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
    CriteriaQuery<PostalAddress> criteriaQuery = criteriaBuilder.createQuery(PostalAddress.class);
    Root<PostalAddress> root = criteriaQuery.from(PostalAddress.class);
    criteriaQuery.select(root);
    Query<PostalAddress> query = session.createQuery(criteriaQuery);
    addressList = query.list();
  }

  /**
   * Default ctor loads value lists and initializes Random.
   *
   * @throws IOException exception during loading of values renders the generator useless
   */
  private PersonGenerator() throws IOException {
    surnames = loadValues(RES_SURNAMES);
    femaleNames = loadValues(RES_FEMALE);
    maleNames = loadValues(RES_MALE);
    eyecolors = loadValues(RES_EYECOLORS);

    random = new Random(System.currentTimeMillis());
  }

  /**
   * Creates a single random person object with linked address.
   *
   * @return the new person object
   */
  private Person createRandomPerson() {
    String gender = random.nextBoolean() ? MALE : FEMALE; // (add diverse if you like)
    String firstname;
    if (MALE.equals(gender)) {
      firstname = maleNames.get(random.nextInt(maleNames.size())).trim();
    } else {
      firstname = femaleNames.get(random.nextInt(femaleNames.size())).trim();
    }
    String surname = surnames.get(random.nextInt(surnames.size())).trim();
    String eyecolor = eyecolors.get(random.nextInt(eyecolors.size())).trim();

    LocalDate dateOfBirth = createRandomDateOfBirth();
    String emailAddress = createEmailAddress(firstname, surname, dateOfBirth);
    int height = createRandomHeight(gender);

    Person randomPerson =
        new Person(firstname, surname, gender, dateOfBirth, height, eyecolor, emailAddress);
    randomPerson.setAddress(addressList.get(random.nextInt(addressList.size())));

    return randomPerson;
  }

  /**
   * Creates a valid and (most probably) unique email address at a test domain. Since the top level
   * domain .test is reserved, these email addresses will never be routed to any real world email
   * server. You have to configure your test email server for this.
   *
   * @param firstname the given name of the person
   * @param surname the family name of the person
   * @param dateOfBirth the date of birth of the person (to avoid duplicates)
   * @return the new email address as string
   */
  private String createEmailAddress(String firstname, String surname, LocalDate dateOfBirth) {
    StringBuilder email =
        new StringBuilder()
            .append(firstname)
            .append(surname)
            .append(dateOfBirth.getYear())
            .append(EMAIL_TEST);

    return Normalizer.normalize(email.toString(), Form.NFKC).replaceAll("[^\\p{ASCII}]", "");
  }

  /**
   * Creates a reasonable human height.
   *
   * @param gender male or female (add diverse if you like)
   * @return the new height as int
   */
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

  /**
   * Creates a valid and reasonable human date of birth.
   *
   * @return the date of birth
   */
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
      default: // there is no other month
        break;
    }

    return LocalDate.of(LocalDate.now().getYear() - (int) Math.round(age), month, day);
  }

  /**
   * Loads value list with classloader for human-readable random person attributes.
   *
   * @param fileName name of the files
   * @return list of values
   * @throws IOException values could not be loaded
   */
  private List<String> loadValues(String fileName) throws IOException {
    InputStream input = getClass().getResourceAsStream(fileName);
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
    ArrayList<String> values = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    reader.close();

    return values;
  }

  /**
   * Creates commons cli options.
   *
   * @return options as object
   */
  private static Options generateOptions() {
    final Option fileOption =
        Option.builder("a")
            .required()
            .hasArg()
            .longOpt(OPT_AMOUNT)
            .desc("amount of dataset entries")
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
}
