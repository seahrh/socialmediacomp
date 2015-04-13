package cs4242.a3;
import static cs4242.a3.StringUtil.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.English;
import org.languagetool.rules.RuleMatch;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;



public final class SpellChecker {
	
	private static final JLanguageTool LANGUAGE_TOOL = new JLanguageTool(new BritishEnglish());
	
	// Loading the language rules is expensive, so do it in the static block
	
	static {
		try {
			LANGUAGE_TOOL.activateDefaultPatternRules();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	private SpellChecker() {
		// Private constructor, not meant to be instantiated
	}

	public static int countLexicalErrors(String text) throws IOException {
		
		List<RuleMatch> matches = LANGUAGE_TOOL.check(text);
		return matches.size();
	}
	
	public static void main(String[] args) throws IOException {
		String inPath = System.getProperty("input.file.path");
		String outPath = System.getProperty("output.file.path");
		int textIndex = Ints.tryParse(System.getProperty("col.index.text"));
		BufferedReader br = null;
		BufferedWriter bw = null;
		String line = "";
		String text = "";
		int lexicalErrors = 0;
		File inFile = new File(inPath);
		File outFile = new File(outPath);
		List<String> values = null;
		StringBuilder sb;
		System.out.printf(
				"Checking for lexical errors...\n\t%s\n", inPath);
		long startTime = System.currentTimeMillis();
		int count = 0;
		
		try {

			br = new BufferedReader(new FileReader(inFile));
			bw = new BufferedWriter(new FileWriter(outFile));
			
			line = br.readLine();
			sb = new StringBuilder(line);
			sb.append("\tlexical_errors\n");
			bw.write(sb.toString());
			
			
			while ((line = br.readLine()) != null) {
				values = Splitter.on(TAB_SEPARATOR).trimResults()
						.splitToList(line);
				text = values.get(textIndex);
				lexicalErrors = countLexicalErrors(text);
				
				// End of line may have multiple tab chars
				// Trim all tab chars from end of line
				
				line = TAB_SEPARATOR.trimTrailingFrom(line);
				
				sb = new StringBuilder(line);
				sb.append("\t");
				sb.append(lexicalErrors);
				sb.append("\n");
				
				bw.write(sb.toString());
				
				count++;
				
				if (count % 100 == 0) {
					System.out.printf("Checked %s lines\n", count);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			if (br != null) {
				br.close();
			}
			if (bw != null) {
				bw.close();
			}
		}
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Checked total %s lines. Run time: %ss\n", count, elapsedTime / 1000);
	}

}
