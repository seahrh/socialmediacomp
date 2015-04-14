package cs4242.a3;

import static com.google.common.base.Preconditions.checkState;
import static cs4242.a3.StringUtil.TAB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;

public final class ArffGenerator {

	private static Set<String> ids;

	private ArffGenerator() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) {

		List<FeatureVector> fvs = null;
		Instances data = null;
		Instances other = null;
		long startTime = System.currentTimeMillis();

		try {

			fvs = extract();
			data = FeatureVector.getInstances(fvs, ids);
			//other = loadArff(System.getProperty("a3.input.crtdt.file.path"));
			//data = leftJoin(data, other, "id");
			saveArff(data);

		} catch (Exception e) {
			e.printStackTrace();
		}

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);

	}

	public static List<FeatureVector> extract() throws IOException {
		String inPath = System.getProperty("input.file.path");
		String outPath = System.getProperty("duplicates.file.path");
		int classIndex = Ints.tryParse(System.getProperty("col.index.class"));
		int idIndex = Ints.tryParse(System.getProperty("col.index.id"));
		int textIndex = Ints.tryParse(System.getProperty("col.index.text"));
		int lexicalErrorsIndex = Ints.tryParse(System
				.getProperty("col.index.lexerr"));
		final int NUMBER_OF_COLUMNS = 4;
		BufferedReader br = null;
		BufferedWriter bw = null;
		String line = "";
		String clazz = "";
		String id = "";
		String text = "";
		int lexicalErrors = 0;
		File inFile = new File(inPath);
		File outFile = new File(outPath);
		ids = new HashSet<String>();
		List<String> values = null;
		int duplicates = 0;
		int valuesSize = 0;
		StringBuilder sb;
		List<FeatureVector> data = new ArrayList<FeatureVector>(2000);
		System.out.printf("Extracting features...\n\t%s\n", inPath);

		try {

			br = new BufferedReader(new FileReader(inFile));
			bw = new BufferedWriter(new FileWriter(outFile));

			// Skip the header row

			br.readLine();

			while ((line = br.readLine()) != null) {

				values = Splitter.on(TAB).trimResults()
						.splitToList(line);

				valuesSize = values.size();

				checkState(valuesSize == NUMBER_OF_COLUMNS,
						"Expecting %s columns but found %s at line [%s]",
						NUMBER_OF_COLUMNS, valuesSize, line);

				clazz = values.get(classIndex);
				id = values.get(idIndex);
				text = values.get(textIndex);
				lexicalErrors = Ints.tryParse(values.get(lexicalErrorsIndex));

				if (ids.contains(id)) {
					sb = new StringBuilder(line);
					sb.append("\n");
					bw.write(sb.toString());
					duplicates++;
				} else {
					ids.add(id);
					data.add(new FeatureVector(clazz, id, text, lexicalErrors));

				}

			}

		} finally {
			if (br != null) {
				br.close();
			}
			if (bw != null) {
				bw.close();
			}
		}

		System.out.printf("Found %s duplicates\n", duplicates);
		if (duplicates > 0) {
			System.out.printf("Logged duplicates\n\t%s\n", outPath);
		}

		return data;
	}
	
	public static Instances leftJoin(Instances left, Instances right, String predicateName) {
		final int LEFT_NUMBER_OF_ATTRIBUTES = left.numAttributes();
		final int RIGHT_NUMBER_OF_ATTRIBUTES = right.numAttributes();
		final int LEFT_NUMBER_OF_INSTANCES = left.size();
		final int RIGHT_NUMBER_OF_INSTANCES = right.size();
		final String RELATION_NAME = left.relationName();
		final String CLASS_NAME = left.classAttribute().name();
		Attribute attr;
		String aname;
		ArrayList<Attribute> attrs = new ArrayList<Attribute>(LEFT_NUMBER_OF_ATTRIBUTES + RIGHT_NUMBER_OF_ATTRIBUTES);
		ArrayList<String> mergeAttributeNames = new ArrayList<String>(RIGHT_NUMBER_OF_ATTRIBUTES);
		SparseInstance joined;
		SparseInstance li;
		DenseInstance ri;
		double lpred;
		double rpred;
		double val;
		boolean matched = false;
		
		for (int i = 0; i < LEFT_NUMBER_OF_ATTRIBUTES; i++) {
			attrs.add(left.attribute(i));
		}
		
		// Skip the first two attributes, which are always class and id
		
		for (int i = 2; i < RIGHT_NUMBER_OF_ATTRIBUTES; i++) {
			attr = right.attribute(i);
			aname = attr.name();
			attrs.add(attr);
			mergeAttributeNames.add(aname);
			System.out.println(aname);
		}
		
		Instances result = new Instances(RELATION_NAME, attrs, LEFT_NUMBER_OF_INSTANCES + RIGHT_NUMBER_OF_INSTANCES);
		
		for (int i = 0; i < LEFT_NUMBER_OF_INSTANCES; i++) {
			
			matched = false;
			joined = new SparseInstance(1);
			joined.setDataset(result);
			
			li = (SparseInstance) left.instance(i);
			joined = (SparseInstance) joined.mergeInstance(li);
			lpred = li.value(result.attribute(predicateName));
			
			for (int j = 0; j < RIGHT_NUMBER_OF_INSTANCES; j++) {
				
				ri = (DenseInstance) right.instance(i);
				rpred = ri.value(right.attribute(predicateName));
				
				if (lpred == rpred) {
					// Matching predicate, add attributes to merge
					
					for (String name : mergeAttributeNames) {
						
						val = ri.value(right.attribute(name));
						joined.setValue(result.attribute(name), val);
					}
					
					matched = true;
				}
			}
			
			if (!matched) {
				for (String name : mergeAttributeNames) {
					joined.setMissing(result.attribute(name));
				}
			}
			
			result.add(joined);
		}
		
		result.setClass(result.attribute(CLASS_NAME));
		
		return result;
	}

	public static void saveArff(Instances data) throws IOException {
		ArffSaver arff = new ArffSaver();
		String filePath = System.getProperty("output.file.path");
		arff.setInstances(data);
		arff.setFile(new File(filePath));

		arff.writeBatch();
		System.out.printf("Saved %s attributes and %s instances:\n\t%s\n",
				data.numAttributes(), data.size(), filePath);

	}
	
	public static Instances loadArff(String filePath) throws IOException {
		ArffLoader loader = new ArffLoader();
		loader.setSource(new File(filePath));
		//loader.getStructure();
		Instances data = loader.getDataSet();
		
		return data;
	}
	
	

}
