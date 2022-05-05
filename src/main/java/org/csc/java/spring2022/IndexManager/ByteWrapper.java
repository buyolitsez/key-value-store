package org.csc.java.spring2022.IndexManager;

import java.util.Arrays;

/**
 * Вспомогательная обертка над массивом байтов, понадобится для хранения Map<ByteWrapper,
 * List<FileBlockLocation>> в {@link IndexManager}
 */
final class ByteWrapper {

  private final byte[] data;

  ByteWrapper(byte[] data) {
    this.data = data;
  }

  byte[] getBytes() {
    return data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ByteWrapper that = (ByteWrapper) o;
    return Arrays.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }
}