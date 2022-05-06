package org.csc.databaseIndexManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import org.csc.RecordInFileLocation;

public interface DatabaseIndexManager extends Closeable {

  /**
   * Create connection key -> listOf(FileBlockLocation)
   */
  void add(byte[] key, List<RecordInFileLocation> writtenBlocks) throws IOException;

  void remove(byte[] key) throws IOException;

  List<RecordInFileLocation> getFileBlocksLocations(byte[] key) throws IOException;
}
