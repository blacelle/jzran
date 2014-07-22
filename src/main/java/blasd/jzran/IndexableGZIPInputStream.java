package blasd.jzran;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class IndexableGZIPInputStream extends GZIPInputStream {

	public IndexableGZIPInputStream(InputStream in, int size) throws IOException {
		super(in, size);
	}

	public IndexableGZIPInputStream(InputStream in) throws IOException {
		super(in);
	}

	public long getBytesRead() {
		return inf.getBytesRead();
	}

	public long getBytesWritten() {
		return inf.getBytesWritten();
	}
}
