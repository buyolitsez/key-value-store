package org.csc.java.spring2022.ValueStoreManager;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.csc.java.spring2022.FileBlockLocation;

/**
 * Класс, который занимается записью/чтением значений в файлы
 */
public interface ValueStoreManager extends Closeable {

  /**
   * Записывает значение в файл, возвращает блоки, в которые было записано значение для добавления
   * этой информации в индекс
   */
  List<FileBlockLocation> add(byte[] value) throws IOException;

  /**
   * Возвращает входной поток из которого можно читать значение из конкретного блока
   */
  InputStream openBlockStream(FileBlockLocation location) throws IOException;

  /**
   * Добавляет удаленные блоки в список свободных блоков
   */
  void remove(List<FileBlockLocation> valueBlocksLocations) throws IOException;
}
