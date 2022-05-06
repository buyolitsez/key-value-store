package org.csc.databaseRecordsManager;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.csc.RecordInFileLocation;

/**
 * Uses for load/read values to files
 */
public interface DatabaseRecordsManager extends Closeable {

  /**
   * Write value to file, returns blocks with information about wrote positions
   */
  List<RecordInFileLocation> add(byte[] value) throws IOException;

  InputStream openBlockStream(RecordInFileLocation location) throws IOException;

  void remove(List<RecordInFileLocation> valueBlocksLocations) throws IOException;
}
