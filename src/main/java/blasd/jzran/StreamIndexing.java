package blasd.jzran;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class StreamIndexing {
	public static final int END_OF_STREAM = -1;

	public static long[] buildIndex(IndexableGZIPInputStream inputStream, int indexSize) throws IOException {
		// Read a first byte to skip the header
		int firstRead = inputStream.read();

		if (firstRead == END_OF_STREAM) {
			return new long[0];
		}

		byte[] buffer = new byte[indexSize];

		List<Long> nbReadArray = new ArrayList<Long>();

		int nbRead = 0;
		do {
			nbRead = inputStream.read(buffer);

			if (nbRead == END_OF_STREAM) {
				break;
			} else {
				nbReadArray.add(inputStream.getBytesRead());
				nbReadArray.add(inputStream.getBytesWritten());
			}

		} while (true);

		long[] asPrimitiveArray = new long[nbReadArray.size()];

		for (int i = 0; i < asPrimitiveArray.length; i++) {
			asPrimitiveArray[i] = nbReadArray.get(i);
		}

		return asPrimitiveArray;
	}

	/**
	 * 
	 * @param in
	 *            the original {@link InputStream}
	 * @param buffer
	 *            the buffer into which the data is written
	 * @param bufferOffset
	 *            off the start offset in the destination array buffer
	 * @param indexes
	 * @param offset
	 * @param length
	 *            the maximum number of bytes read
	 * @throws IOException
	 *             Extract buffer from inputStream after the n-th
	 */
	public static int extract(InputStream in, byte[] buffer, int bufferOffset, long[] indexes, long offset, int length) throws IOException {
		in.reset();

		GZIPInputStream gis = new GZIPInputStream(in);

		try {
			if (offset == 0 || indexes.length == 0) {
				return gis.read(buffer, bufferOffset, length);
			} else {
				// Read the first byte, like in the indexing procedure. As
				// offset==0, we are not interested in this byte
				int firstRead = gis.read();

				if (firstRead == END_OF_STREAM) {
					return END_OF_STREAM;
				}

				int indexFromWhereToRead = -1;

				long nbCompressedToSkip = -1;
				long nbDecompressedWhenSkipped = -1;

				// Check if the first point is already after the offset
				if (indexes[1] > offset) {
					// Yes: we need to read from the beginning
					nbCompressedToSkip = 0;
					nbDecompressedWhenSkipped = 0;
				} else {
					// Start from the second point
					for (indexFromWhereToRead = 2; indexFromWhereToRead < indexes.length; indexFromWhereToRead += 2) {
						// long nextBytesRead = indexes[indexFromWhereToRead *
						// 2];
						long nextBytesWritten = indexes[indexFromWhereToRead * 2 + 1];

						// Iterate until the position right after what we're
						// looking
						// for
						if (nextBytesWritten > offset) {
							// Keep the indexes for previous point
							nbCompressedToSkip = indexes[indexFromWhereToRead * 2 - 1];
							nbDecompressedWhenSkipped = indexes[indexFromWhereToRead * 2 - 2];
							break;
						}
					}
				}

				{
					int succeedlySkipped = 0;

					// We may need to skip several times, typically if nbToSkip
					// > Integer.MAX_VALUE
					while (succeedlySkipped < nbCompressedToSkip) {
						long previousOffset = succeedlySkipped;

						succeedlySkipped += gis.skip(nbCompressedToSkip);

						if (gis.available() == 0) {
							// We did not succeed skipping until the expected
							// offset: has the file been modified in the
							// meantime?
							return 0;
						}

						if (previousOffset == succeedlySkipped) {
							// We failed skipping until the expected offset. ???
							throw new IllegalStateException("Unable to skip GZip stream until requested position");
						}
					}
				}

				{
					long currentOffset = nbDecompressedWhenSkipped;

					while (currentOffset < offset) {
						// TODO: read a large block, and skip what's before the
						// requested offset
						currentOffset += gis.read(new byte[(int) (offset - currentOffset)]);
					}
				}

				return gis.read(buffer, bufferOffset, length);
			}

		} finally {
			gis.close();
		}

	}
}
