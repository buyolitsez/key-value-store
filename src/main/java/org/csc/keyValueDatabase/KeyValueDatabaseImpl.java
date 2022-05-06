package org.csc.keyValueDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import org.csc.databaseIndexManager.DatabaseIndexManager;
import org.csc.databaseRecordsManager.DatabaseRecordsManager;
import org.csc.databaseRecordsManager.DatabaseRecordsManagerImpl;
import org.csc.databaseIndexManager.DatabaseIndexManagerImpl;

public class KeyValueDatabaseImpl implements KeyValueDatabase {

  private final DatabaseIndexManager databaseIndexManager;
  private final DatabaseRecordsManager databaseRecordsManager;

  private boolean wasClosed = false;

  KeyValueDatabaseImpl(Path workingDir, int valueFileSize) throws IOException {
    if (valueFileSize <= 0) {
      throw new IllegalArgumentException("valueFileSize should be more than 0");
    }
    if (!workingDir.toFile().exists()) {
      throw new IllegalArgumentException("workingDir not exists");
    }
    if (!workingDir.toFile().isDirectory()) {
      throw new IllegalArgumentException("workingDir is not a directory");
    }
    databaseIndexManager = new DatabaseIndexManagerImpl(workingDir);
    databaseRecordsManager = new DatabaseRecordsManagerImpl(workingDir, valueFileSize);
  }

  private void checkNotClosed() {
    if (wasClosed) {
      throw new IllegalStateException("Key value store is closed!");
    }
  }

  @Override
  public boolean contains(byte[] key) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);
    return !databaseIndexManager.getFileBlocksLocations(key).isEmpty();
  }

  @Override
  public InputStream openValueStream(byte[] key) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);

    if (!contains(key)) {
      throw new IOException("Input stream not exists");
    }
    var inputStreams = databaseIndexManager.getFileBlocksLocations(key).stream().map(block -> {
      try {
        return databaseRecordsManager.openBlockStream(block);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).toList();
    return new SequenceInputStream(Collections.enumeration(inputStreams));
  }

  @Override
  public byte[] loadValue(byte[] key) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);
    return openValueStream(key).readAllBytes();
  }

  @Override
  public void upsert(byte[] key, byte[] value) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    remove(key);

    var blocks = databaseRecordsManager.add(value);
    databaseIndexManager.add(key, blocks);
  }

  @Override
  public boolean remove(byte[] key) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);
    var blocks = databaseIndexManager.getFileBlocksLocations(key);
    if (blocks.isEmpty()) {
      return false;
    }
    databaseRecordsManager.remove(blocks);
    databaseIndexManager.remove(key);
    return true;
  }

  @Override
  public DatabaseIndexManager getIndexManager() {
    checkNotClosed();
    return databaseIndexManager;
  }

  @Override
  public void close() throws IOException {
    wasClosed = true;
    databaseIndexManager.close();
    databaseRecordsManager.close();
  }
}
