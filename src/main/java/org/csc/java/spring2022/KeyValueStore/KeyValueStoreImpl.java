package org.csc.java.spring2022.KeyValueStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import org.csc.java.spring2022.IndexManager.IndexManager;
import org.csc.java.spring2022.IndexManager.IndexManagerImpl;
import org.csc.java.spring2022.ValueStoreManager.ValueStoreManager;
import org.csc.java.spring2022.ValueStoreManager.ValueStoreManagerImpl;

public class KeyValueStoreImpl implements KeyValueStore {

  private final IndexManager indexManager;
  private final ValueStoreManager valueStoreManager;

  private boolean wasClosed = false;

  KeyValueStoreImpl(Path workingDir, int valueFileSize) throws IOException {
    if (valueFileSize <= 0) {
      throw new IllegalArgumentException("valueFileSize should be more than 0");
    }
    if (!workingDir.toFile().exists()) {
      throw new IllegalArgumentException("workingDir not exists");
    }
    if (!workingDir.toFile().isDirectory()) {
      throw new IllegalArgumentException("workingDir is not a directory");
    }
    indexManager = new IndexManagerImpl(workingDir);
    valueStoreManager = new ValueStoreManagerImpl(workingDir, valueFileSize);
  }

  private void checkNotClosed() {
    if (wasClosed) {
      throw new IllegalStateException("Key value store is closed!");
    }
  }

  /**
   * Проверяет, есть ли такой ключ в хранилище
   *
   * @param key
   */
  @Override
  public boolean contains(byte[] key) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);
    return !indexManager.getFileBlocksLocations(key).isEmpty();
  }

  /**
   * По ключу возвращает входной поток из которого можно (лениво) читать значение
   *
   * @param key
   */
  @Override
  public InputStream openValueStream(byte[] key) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);

    if (!contains(key)) {
      throw new IOException("Input stream not exists");
    }
    var inputStreams = indexManager.getFileBlocksLocations(key).stream().map(block -> {
      try {
        return valueStoreManager.openBlockStream(block);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).toList();
    return new SequenceInputStream(Collections.enumeration(inputStreams));
  }

  /**
   * Полностью считывает значение в массив байтов и возвращает его
   *
   * @param key
   */
  @Override
  public byte[] loadValue(byte[] key) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);
    return openValueStream(key).readAllBytes();
  }

  /**
   * Записывает новое значение по ключу. Если ключ уже существует в базе, тогда перезаписывает
   * старое значение
   *
   * @param key
   * @param value
   */
  @Override
  public void upsert(byte[] key, byte[] value) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    remove(key);

    var blocks = valueStoreManager.add(value);
    indexManager.add(key, blocks);
  }

  /**
   * Удаляет значение из базы. Если значение существовало, то возвращает true, иначе false.
   *
   * @param key
   */
  @Override
  public boolean remove(byte[] key) throws IOException {
    checkNotClosed();
    Objects.requireNonNull(key);
    var blocks = indexManager.getFileBlocksLocations(key);
    if (blocks.isEmpty()) {
      return false;
    }
    valueStoreManager.remove(blocks);
    indexManager.remove(key);
    return true;
  }

  /**
   * TestOnly
   * <p>
   * Возвращает IndexManager, соответствующий текущему хранилищу
   */
  @Override
  public IndexManager getIndexManager() {
    checkNotClosed();
    return indexManager;
  }

  /**
   * Closes this stream and releases any system resources associated with it. If the stream is
   * already closed then invoking this method has no effect.
   *
   * <p> As noted in {@link AutoCloseable#close()}, cases where the
   * close may fail require careful attention. It is strongly advised to relinquish the underlying
   * resources and to internally
   * <em>mark</em> the {@code Closeable} as closed, prior to throwing
   * the {@code IOException}.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    wasClosed = true;
    indexManager.close();
    valueStoreManager.close();
  }
}
