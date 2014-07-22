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

import org.junit.Assert;
import org.junit.Test;

public class TestStreamIndexing {
	@Test
	public void testIndexing() throws UnsupportedEncodingException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		final int problemSize = 256;

		try {
			// "UTF-8"
			Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8);
			try {
				for (int i = 0; i < problemSize; i++) {
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

		long writtenLength = nbWritten(problemSize);
		for (int possibleStart = 0; possibleStart < writtenLength; possibleStart++) {
			System.out.println(possibleStart + "/" + writtenLength);

			for (int possibleLength = 0; possibleLength < writtenLength - possibleStart; possibleLength++) {
				byte[] buffer = new byte[possibleLength];

				if (possibleLength == 985) {
					int az = 3;
				}

				int nbRead = StreamIndexing.extract(source, buffer, 0, index, possibleStart, possibleLength);

				if (possibleLength == 0) {
					Assert.assertTrue("Start=" + possibleStart, nbRead == 0);
				} else {
					Assert.assertTrue("Start=" + possibleStart + " Length=" + possibleLength, nbRead <= possibleLength && nbRead > 0);
				}

				// System.out.println(new String(buffer,
				// StandardCharsets.UTF_8));
			}
		}
	}

	@Test
	public void testNbWritten() throws UnsupportedEncodingException, IOException {
		// ""
		Assert.assertEquals(0, nbWritten(0));

		// "-0"
		Assert.assertEquals(2, nbWritten(1));

		// "-0-1-2-3-4-5-6-7-8-9"
		Assert.assertEquals(20, nbWritten(10));

		// "-0-1-2-3-4-5-6-7-8-9-10"
		Assert.assertEquals(23, nbWritten(11));

		// "-0-1-2-3-4-5-6-7-8-9-10...-98-99"
		Assert.assertEquals(290, nbWritten(100));

		// "-0-1-2-3-4-5-6-7-8-9-10...-98-99-100"
		Assert.assertEquals(294, nbWritten(101));

		// "-0-1-2-3-4-5-6-7-8-9-10...-998-999"
		Assert.assertEquals(3890, nbWritten(1000));

		// "-0-1-2-3-4-5-6-7-8-9-10...-998-999-1000"
		Assert.assertEquals(3895, nbWritten(1001));
	}

	public long nbWritten(int size) {
		if (size == 0) {
			return 0;
		} else {
			long nbCharWritten = 0;

			// 1 '-' per item written
			nbCharWritten += size;

			int nbDigit = 0;

			long nbAlreadyCOnsidered = 0;
			do {
				nbDigit++;

				long maxNbConsidered = (long) Math.pow(10, nbDigit);

				if (size > 0) {
					long nbWithCurrentNbOfDigits = Math.min(maxNbConsidered, size) - nbAlreadyCOnsidered;
					nbCharWritten += nbDigit * nbWithCurrentNbOfDigits;
					nbAlreadyCOnsidered += nbWithCurrentNbOfDigits;
				}
			} while (size > Math.pow(10, nbDigit));

			return nbCharWritten;
		}
	}
}
