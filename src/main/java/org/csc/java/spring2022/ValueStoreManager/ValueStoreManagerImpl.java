package org.csc.java.spring2022.ValueStoreManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.csc.java.spring2022.FileBlockLocation;

public class ValueStoreManagerImpl implements ValueStoreManager {

  private final int valueFileSize;
  private final Stack<FileBlockLocation> freeBlocs = new Stack<>();
  /**
   * database files named exactly as {currentFile}
   */
  private int currentFile = 0;
  private int currentFileOffset = 0;
  private final Path workingDir;

  public ValueStoreManagerImpl(Path workingDir, int valueFileSize) {
    this.workingDir = workingDir;
    this.valueFileSize = valueFileSize;
  }

  private String getCurrentFilename() {
    return workingDir.resolve(Integer.toString(currentFile)).toString();
  }

  private FileBlockLocation writeToBlock(byte[] data, int offset, int lengthToWrite,
      FileBlockLocation block) throws IOException {

    try (RandomAccessFile accessFile = new RandomAccessFile(block.fileName(), "rw")) {
      accessFile.seek(block.offset());
      accessFile.write(data, offset, lengthToWrite);
    }

    return new FileBlockLocation(block.fileName(), block.offset() + lengthToWrite,
        block.size() - lengthToWrite);
  }

  @Override
  public List<FileBlockLocation> add(byte[] value) throws IOException {
    List<FileBlockLocation> wroteBlocks = new ArrayList<>();
    int offset = 0;

    while (offset != value.length && !freeBlocs.isEmpty()) {
      var block = freeBlocs.pop();
      int currentLengthToWrite = Integer.min(value.length - offset, block.size());

      var shrankBlock = writeToBlock(value, offset, currentLengthToWrite, block);

      offset += currentLengthToWrite;
      wroteBlocks.add(new FileBlockLocation(block.fileName(),
          block.offset(), currentLengthToWrite));
      if (shrankBlock.size() != 0) {
        freeBlocs.add(shrankBlock);
      }
    }

    while (offset != value.length) {
      int currentLengthToWrite = Integer.min(value.length - offset,
          valueFileSize - currentFileOffset);
      if (currentLengthToWrite == 0) {
        currentFile++;
        currentFileOffset = 0;
        var newFile = new File(getCurrentFilename());
        newFile.createNewFile();
      } else {
        var newBlock = new FileBlockLocation(getCurrentFilename(), currentFileOffset,
            currentLengthToWrite);
        writeToBlock(value, offset, currentLengthToWrite, newBlock);

        wroteBlocks.add(newBlock);
        offset += currentLengthToWrite;
        currentFileOffset += currentLengthToWrite;
      }
    }
    return wroteBlocks;
  }

  @Override
  public InputStream openBlockStream(FileBlockLocation location) throws IOException {
    return new InputStream() {
      final byte[] value = new byte[location.size()];
      int currentPos = -1;

      @Override
      public int read() throws IOException {
        if (currentPos == -1) {
          try (var accessFile = new RandomAccessFile(location.fileName(), "r")) {
            accessFile.seek(location.offset());
            accessFile.read(value, 0, location.size());
          }
          currentPos = 0;
        }
        if (currentPos == value.length) {
          return -1;
        }
        return value[currentPos++];
      }
    };
  }

  @Override
  public void remove(List<FileBlockLocation> valueBlocksLocations) throws IOException {
    freeBlocs.addAll(valueBlocksLocations);
  }

  @Override
  public void close() throws IOException {}
}
