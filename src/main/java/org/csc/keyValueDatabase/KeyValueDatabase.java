package org.csc.keyValueDatabase;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import org.csc.databaseIndexManager.DatabaseIndexManager;

public interface KeyValueDatabase extends Closeable {

  boolean contains(byte[] key) throws IOException;

  InputStream openValueStream(byte[] key) throws IOException;

  /**
   * Fully read value to array of bytes and returns it
   */
  byte[] loadValue(byte[] key) throws IOException;

  /**
   * Same as put in java.util.map
   */
  void upsert(byte[] key, byte[] value) throws IOException;

  /**
   * @return true if there was store with such key, false otherwise
   */
  boolean remove(byte[] key) throws IOException;

  /**
   * TestOnly
   * <p>
   * Returns IndexManager, of current store
   */
  DatabaseIndexManager getIndexManager();
}
