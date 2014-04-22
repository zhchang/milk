package com.mozat.sv3.moml3.tool;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.mozat.sv3.io.FileFormat;
import com.mozat.sv3.io.FileUtil;
import com.mozat.sv3.io.TransUnit;
import com.mozat.sv3.mobon.MobonException;
import com.mozat.sv3.moml3.parser.Moml3Parser;
import com.mozat.sv3.moml3.parser.ParseResult;
import com.mozat.sv3.moml3.tags.Page;
import com.mozat.sv3.moml3.tags.TagUtil;

public class Moml3Compiler {
	public static void main(String[] args) {
		Options options = new Options();
		// options.addOption("i", true, "[input] moml3 file to be compiled");
		// options.addOption("o", true, "[output] compiled sv3 file");
		options.addOption("r", false,
				"make the page right-align and flow right-to-left by default");
		options.addOption(
				"d",
				false,
				"dump the compilation result as json to standard output (for debugging purpose)");
		options.addOption(
				"z",
				false,
				"whether to produce a gzipped binary, note that .gz extension is automatically added if this option is on");
		options.addOption("t", true,
				"[input] translated text resource file (language to be applied)");
		options.addOption(
				"e",
				true,
				"[output] extracted text resource file (English text resource extracted from the input moml3 file)");

		CommandLineParser cparser = new PosixParser();

		CommandLine cmd = null;
		try {
			cmd = cparser.parse(options, args);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		if (cmd.getArgs().length == 2) {
			try {
				String infile = cmd.getArgs()[0];
				String outfile = cmd.getArgs()[1];
				String outResFile = cmd.getOptionValue("e");
				String inResFile = cmd.getOptionValue("t");
				boolean doGzip = cmd.hasOption("z");
				Moml3Parser parser;
				parser = new Moml3Parser(FileUtil.readAll(infile));

				ParseResult result = parser.parseSafely();

				Page page = result.page;
				if (page != null) {
					if (inResFile != null) { // translate the page
						byte[] bytes = FileUtil.readAllBytes(inResFile);
						FileUtil fu = FileFormat.detectFormat(bytes)
								.getFileUtil();
						LinkedHashMap<String, TransUnit> units = fu
								.getUnits(bytes);
						page.translate(units);
					} else if (outResFile != null) { // extract text resource
														// from the page
						String res = page.extractResource();
						OutputStreamWriter writer = new OutputStreamWriter(
								new FileOutputStream(outResFile), "UTF-8");
						writer.write(res);
						writer.close();
					}

					if (cmd.hasOption("r")) {
						page.convertToRightToLeft();
					}

					if (doGzip) {
						GZIPOutputStream gzipOut = new GZIPOutputStream(
								new FileOutputStream(outfile + ".gz"));
						TagUtil.toMobon(page, gzipOut);
						gzipOut.close();
					} else {
						FileOutputStream fileOut = new FileOutputStream(outfile);
						TagUtil.toMobon(page, fileOut);
						fileOut.close();
					}

					if (cmd.hasOption("d")) {
						System.out.println(TagUtil.toJson(page));
					}
				}

				if (result.errors.size() > 0) {
					for (Object error : result.errors) {
						System.err.println(infile + ":" + error);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (MobonException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				printUsage(options);
			}
		} else {
			printUsage(options);
		}
	}

	private static void printUsage(Options options) {
		System.out.println("version: " + getVersion() + "." + getBuildNumber()
				+ "\ncopyright: " + getCopyRight() + "\n");
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"compile "
								+ "[-r] [-d] [-e <extract>] [-t <translate>] <input_moml3> <output_sv3>",
						options);
	}

	public static String getVersion() {
		return "6.5.0";
	}

	public static String getBuildNumber() {
		return "120327";
	}

	public static String getCopyRight() {
		return "Mozat Pte. Ltd. © 2012";
	}
}
