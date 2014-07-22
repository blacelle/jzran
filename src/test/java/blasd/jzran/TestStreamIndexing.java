package blasd.jzran;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

public class TestStreamIndexing {
	@Test
	public void testIndexing() throws UnsupportedEncodingException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try {
			// "UTF-8"
			Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8);
			try {
				for (int i = 0; i < 1024; i++) {
					writer.write("-");
					writer.write(Integer.toString(i));
				}
			} finally {
				writer.close();
			}
		} finally {
			output.close();
		}

		InputStream source = new ByteArrayInputStream(output.toByteArray());

		IndexableGZIPInputStream gis = new IndexableGZIPInputStream(source);

		long[] index = StreamIndexing.buildIndex(gis, 1024);

		byte[] buffer = new byte[1024];
		int nbRead = StreamIndexing.extract(source, buffer, 0, index, 100, 100);

		System.out.println(new String(buffer, StandardCharsets.UTF_8));
	}
}
