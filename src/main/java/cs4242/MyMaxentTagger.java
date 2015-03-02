package cs4242;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.base.Optional;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class MyMaxentTagger extends MaxentTagger {

	public MyMaxentTagger(String zipFilePath, String modelFilename,
			Properties config) throws IOException {
		boolean printLoading = true;
		File zipFile = new File(zipFilePath);
		InputStream is;
		ZipInputStream zis = null;
		DataInputStream dis = null;
		ZipEntry entry;
		boolean found = false;
		try {

			is = new FileInputStream(zipFile);
			zis = new ZipInputStream(is);

			while ((entry = zis.getNextEntry()) != null) {

				// Find zip entry with this name

				if (entry.getName().equals(modelFilename)) {
					found = true;
					dis = new DataInputStream(zis);
					readModelAndInit(config, dis, printLoading);
				}
			}
		} finally {
			if (zis != null) {
				zis.close();
			}
		}
		checkArgument(found, "Tagger model [%s] not found in zip file [%s]", modelFilename, zipFilePath);
	}

}
