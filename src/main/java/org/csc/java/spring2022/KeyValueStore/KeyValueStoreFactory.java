package org.csc.java.spring2022.KeyValueStore;

import java.io.IOException;
import java.nio.file.Path;
import org.csc.java.spring2022.KeyValueStore.KeyValueStore;
import org.csc.java.spring2022.NotImplementedException;

public final class KeyValueStoreFactory {

  private KeyValueStoreFactory() {
  }

  public static KeyValueStore create(Path workingDir, int valueFileSize) throws IOException {
    return new KeyValueStoreImpl(workingDir, valueFileSize);
  }
}
