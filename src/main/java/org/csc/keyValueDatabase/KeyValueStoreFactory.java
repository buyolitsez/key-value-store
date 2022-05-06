package org.csc.keyValueDatabase;

import java.io.IOException;
import java.nio.file.Path;

public final class KeyValueStoreFactory {

  private KeyValueStoreFactory() {
  }

  public static KeyValueDatabase create(Path workingDir, int valueFileSize) throws IOException {
    return new KeyValueDatabaseImpl(workingDir, valueFileSize);
  }
}
