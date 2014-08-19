/**
 * 
 */
package edu.cmu.cs.lti.ark.dageem;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * @author scohen
 * 
 */
public class Dageem {

	private static Alphabet alphabet = new Alphabet();

	private static final String PARSER_Viterbi = "viterbi";
	private static final String PARSER_MBR = "mbr";
	private static final String PARSER_Attachment = "attachment";

	public static boolean canOpenFile(String filename) {
		if (filename == null) {
			return true;
		}
		if (filename.startsWith("@")) {
			return true;
		}
		if (filename.equals("")) {
			return true;
		}

		File f = new File(filename);
		if (f.exists() && !f.isDirectory()) {
			return true;
		}

		System.err.println("Error: Could not open " + filename
				+ " for input. Exiting.");
		return false;
	}

	public static void parse(String grammarFile, String fileToParse,
			String fileToEvaluate, String outputFile, String fileToParseFormat,
			String fileToEvaluateFormat, String outputFileFormat,
			String parserType) {

		if (!canOpenFile(grammarFile)) {
			return;
		}

		if (!canOpenFile(fileToParse)) {
			return;
		}

		if (!canOpenFile(fileToEvaluate)) {
			return;
		}

		SentenceCorpus corpus = new SentenceCorpus(alphabet);

		// denullify strings
		if (grammarFile == null) {
			grammarFile = "";
		}
		if (fileToEvaluate == null) {
			fileToEvaluate = "";
		}
		if (fileToParse == null) {
			fileToParse = "";
		}
		if (fileToEvaluateFormat == null) {
			fileToEvaluateFormat = "";
		}
		if (fileToParseFormat == null) {
			fileToParseFormat = "";
		}
		if (outputFileFormat == null) {
			outputFileFormat = "";
		}
		if (outputFile == null) {
			outputFileFormat = "";
		}
		if (parserType == null) {
			parserType = "";
		}

		try {
			corpus.readCorpus(fileToParse);
			// , fileToParseFormat);

			DMVGrammar dmvGrammar;

			DependencyParser parser;
			if (parserType.equals(PARSER_Viterbi)) {
				dmvGrammar = new DMVGrammar(alphabet);
				dmvGrammar.readGrammar(grammarFile, corpus);

				// System.err.println("Outputting grammar in PCFG form.");
				// dmvGrammar.writePCFG(System.out);

				parser = new DMVParserViterbi(dmvGrammar);
			} else if (parserType.equals(PARSER_MBR)) {
				dmvGrammar = new DMVGrammarEdgePosteriors(alphabet);
				dmvGrammar.readGrammar(grammarFile, corpus);

				parser = new DMVParserPosterior(
						(DMVGrammarEdgePosteriors) dmvGrammar);
			} /*
			 * else if (parserType.equals(PARSER_Attachment)) { parser = new
			 * DMVParserAttachment( grammarFile);
			 * 
			 * }
			 */else {
				System.err.println("Unknown parser type: " + parserType);
				return;
			}

			int[][] dep = parser.parseCorpus(corpus);

			if (!outputFile.equals("")) {
				corpus.writeDependencies(outputFile, dep, outputFileFormat);
			}

			if (!fileToEvaluate.equals("")) {
				int[][] evalDeps = SentenceCorpus.readDependencies(
						fileToEvaluate, fileToEvaluateFormat);

				AttachmentAccuracyEvaluator eval = new AttachmentAccuracyEvaluator();

				double[] evals = eval.eval(evalDeps, dep);

				if (evals != null) {
					String[] evalMsgs = { "Accuracy for all lengths",
							"Accuracy for length <= 10",
							"Accuracy for length <= 20",
							"Accuracy for length <= 40" };
					for (int i = 0; i < evals.length; i++) {
						System.out.println(evalMsgs[i] + ": " + evals[i]);
					}
				}
			}
		}

		catch (IOException e) {
			System.err.println("Problem with input/output files: " + e);
			e.printStackTrace(System.err);

			return;
		}
	}

	public static void logisticNormalEstimation(int iterationsNumber,
			int dropOutputEvery, String grammarInitFile,
			String grammarPrefixFile, String fileToTrain,
			String fileToTrainFormat, boolean memoryEfficient) {
		SentenceCorpus corpus = new SentenceCorpus(alphabet);

		if (!canOpenFile(fileToTrain)) {
			return;
		}

		if (!canOpenFile(grammarInitFile)) {
			return;
		}

		try {
			corpus.readCorpus(fileToTrain);
			// , fileToParseFormat);

			MultinomialDMVGrammar dmvGrammar = new MultinomialDMVGrammar(
					alphabet);

			dmvGrammar.readGrammar(grammarInitFile, corpus);
			dmvGrammar.initMultinomials();

			LogisticNormalInsideOutsideInterface io = new LogisticNormalDMVInsideOutside(
					dmvGrammar);
			LogisticNormalModel model = io.createModel();
			Chart.semiring.setSemiringLogReal();
			LogisticNormalVariationalEM varEM = new LogisticNormalDMVVariationalEM(
					model, alphabet, grammarInitFile, corpus);
			varEM.run(iterationsNumber, dropOutputEvery, grammarPrefixFile,
					corpus, io, memoryEfficient);
		}

		catch (IOException e) {
			System.err.println("Error with input/output files: " + e);
			e.printStackTrace(System.err);

			return;
		}
	}

	public static void vanillaExpectationMaximizationEstimation(
			int iterationsNumber, int dropOutputEvery, String grammarInitFile,
			String grammarPrefixFile, String fileToTrain,
			String fileToTrainFormat) {

		if (grammarInitFile == null) {
			grammarInitFile = "";
		}
		if (grammarPrefixFile == null) {
			grammarPrefixFile = "";
		}
		if (fileToTrain == null) {
			fileToTrain = "";
		}
		if (fileToTrainFormat == null) {
			fileToTrainFormat = "";
		}

		if (!canOpenFile(fileToTrain)) {
			return;
		}

		if (!canOpenFile(grammarInitFile)) {
			return;
		}

		SentenceCorpus corpus = new SentenceCorpus(alphabet);

		try {
			corpus.readCorpus(fileToTrain);
			// , fileToParseFormat);

			DMVExpectationMaximization em = new DMVExpectationMaximization(
					alphabet);

			em.initGrammar(grammarInitFile, corpus);

			em.run(iterationsNumber, dropOutputEvery, grammarPrefixFile, corpus);
		}

		catch (IOException e) {
			System.err.println("Error with input/output files: " + e);
			e.printStackTrace(System.err);

			return;
		}

	}

	public static void printUsage(final String applicationName,
			final Options options, final OutputStream out) {
		final PrintWriter writer = new PrintWriter(out);
		final HelpFormatter usageFormatter = new HelpFormatter();

		// usageFormatter.printUsage(writer, 80, applicationName, options);

		usageFormatter.printHelp(writer, 80, applicationName, "Help:", options,
				1, 1, "", true);

		writer.close();
	}

	public static Options constructPosixOptions() {
		final Options posixOptions = new Options();

		posixOptions.addOption(new Option("parse", false, "Use the parser"));
		posixOptions.addOption(new Option("em", false,
				"Run expectation-maximization"));
		posixOptions.addOption(new Option("lnem", false,
				"Run logistic normal variational EM"));

		posixOptions.addOption(new Option("parsermodel", true, "Model file"));
		posixOptions.addOption(new Option("parserinput", true,
				"Input file for parser"));
		// posixOptions.addOption(new Option("parserinputformat", true,
		// "Format for input file for parser (conll or plain)"));
		posixOptions.addOption(new Option("parserinputformat", true,
				"Format for input file for parser (can just be plain)"));
		posixOptions.addOption(new Option("parseroutput", true,
				"Output file for parser"));
		// posixOptions.addOption(new Option("parseroutputformat", true,
		// "Format for output file (conll or plain)"));
		posixOptions.addOption(new Option("parseroutputformat", true,
				"Format for output file (can just be plain)"));
		posixOptions.addOption(new Option("evalinput", true,
				"File to evaluate with"));
		// posixOptions.addOption(new Option("evalinputformat", true,
		// "File format for evaluation file (conll or plain)"));
		posixOptions.addOption(new Option("evalinputformat", true,
				"File format for evaluation file (can just be plain)"));
		posixOptions.addOption(new Option("parseralg", true,
				"Parser algorithm (mbr or viterbi)"));

		posixOptions.addOption(new Option("emiter", true,
				"Number of EM iterations"));
		posixOptions.addOption(new Option("emdropiter", true,
				"Number of iterations between writing models"));
		posixOptions.addOption(new Option("initmodel", true,
				"Initialization model"));
		posixOptions.addOption(new Option("modeloutputprefix", true,
				"Prefix of model output"));
		posixOptions.addOption(new Option("traininput", true,
				"Training file input"));
		posixOptions.addOption(new Option("traininputformat", true,
				"Format of training input"));

		posixOptions.addOption(new Option("lnemiter", true,
				"Number of iterations for logistic normal variational EM"));
		posixOptions.addOption(new Option("lnemdropiter", true,
				"Number of iterations between writing logistic normal models"));
		posixOptions.addOption(new Option("lninitmodel", true,
				"Logistic normal initialization model"));
		posixOptions.addOption(new Option("lnmodeloutputprefix", true,
				"Prefix of logistic normal model output"));
		posixOptions.addOption(new Option("lntraininput", true,
				"Training file input for logistic normal variational EM"));
		posixOptions.addOption(new Option("lntraininputformat", true,
				"Format of training input for logistic normal variational EM"));
		posixOptions
				.addOption(new Option(
						"lnmemorysave",
						false,
						"Should repeat variational inference from last point until convergence or be memory efficient?"));

		return posixOptions;
	}

	public static void usePosixParser(final String[] commandLineArguments) {
		final CommandLineParser cmdLinePosixParser = new PosixParser();
		final Options posixOptions = constructPosixOptions();
		CommandLine commandLine;
		try {
			boolean ranSomething = false;
			commandLine = cmdLinePosixParser.parse(posixOptions,
					commandLineArguments);
			if (commandLine.hasOption("parse")) {

				boolean b = true;

				if (commandLine.getOptionValue("parserinput") == null)
				{
					b = false;
					System.err.println("-parserinput needs to be specified.");
				}

				if (commandLine.getOptionValue("parseroutput") == null)
				{
					b = false;
					System.err.println("-parseroutput needs to be specified.");
				}

				if (commandLine.getOptionValue("parseralg") == null)
				{
					b = false;
					System.err.println("-parseralg needs to be specified.");
				}

				if (b) {
					parse(commandLine.getOptionValue("parsermodel"),
							commandLine.getOptionValue("parserinput"),
							commandLine.getOptionValue("evalinput"),
							commandLine.getOptionValue("parseroutput"),
							commandLine.getOptionValue("parserinputformat"),
							commandLine.getOptionValue("evalinputformat"),
							commandLine.getOptionValue("parseroutputformat"),
							commandLine.getOptionValue("parseralg"));
				}
				ranSomething = true;
			}
			if (commandLine.hasOption("em")) {
				boolean b = true;

				try {
					Integer.parseInt(commandLine.getOptionValue("emiter"));
				} catch (NumberFormatException e) {
					System.err
							.println("Invalid number of iterations (-emiter)");

					b = false;
				}

				try {
					Integer.parseInt(commandLine.getOptionValue("emdropiter"));
				} catch (NumberFormatException e) {
					System.err
							.println("Invalid number of iteration steps (-emdropiter)");

					b = false;
				}

				if (commandLine.getOptionValue("initmodel") == null) {
					b = false;
					System.err.println("-initmodel needs to be specified.");
				}

				if (commandLine.getOptionValue("traininput") == null) {
					b = false;
					System.err.println("-traininput needs to be specified.");
				}

				if (commandLine.getOptionValue("modeloutputprefix") == null) {
					b = false;
					System.err
							.println("-modeloutputprefix needs to be specified.");
				}

				if (b) {
					vanillaExpectationMaximizationEstimation(
							Integer.parseInt(commandLine
									.getOptionValue("emiter")),
							Integer.parseInt(commandLine
									.getOptionValue("emdropiter")),
							commandLine.getOptionValue("initmodel"),
							commandLine.getOptionValue("modeloutputprefix"),
							commandLine.getOptionValue("traininput"),
							commandLine.getOptionValue("traininputformat"));
				}

				ranSomething = true;
			}
			if (commandLine.hasOption("lnem")) {
				boolean b = true;

				try {
					Integer.parseInt(commandLine.getOptionValue("lnemiter"));
				} catch (NumberFormatException e) {
					System.err
							.println("Invalid number of iterations (-lnemiter)");

					b = false;
				}

				try {
					Integer.parseInt(commandLine.getOptionValue("lnemdropiter"));
				} catch (NumberFormatException e) {
					System.err
							.println("Invalid number of iteration steps (-lnemdropiter)");

					b = false;
				}

				if (commandLine.getOptionValue("lninitmodel") == null) {
					b = false;
					System.err.println("-lninitmodel needs to be specified.");
				}

				if (commandLine.getOptionValue("lntraininput") == null) {
					b = false;
					System.err.println("-lntraininput needs to be specified.");
				}

				if (commandLine.getOptionValue("lnmodeloutputprefix") == null) {
					b = false;
					System.err
							.println("-lnmodeloutputprefix needs to be specified.");
				}

				if (b) {
					logisticNormalEstimation(Integer.parseInt(commandLine
							.getOptionValue("lnemiter")),
							Integer.parseInt(commandLine
									.getOptionValue("lnemdropiter")),
							commandLine.getOptionValue("lninitmodel"),
							commandLine.getOptionValue("lnmodeloutputprefix"),
							commandLine.getOptionValue("lntraininput"),
							commandLine.getOptionValue("lntraininputformat"),
							commandLine.hasOption("lnmemorysave"));
				}

				ranSomething = true;
			}
			if (!ranSomething) {
				printUsage("JDageem", posixOptions, System.out);
			}
		} catch (ParseException parseException) // checked exception
		{
			System.err.println("error with command-line arguments:\n"
					+ parseException.getMessage());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		usePosixParser(args);
	}
}
