package org.csc.databaseRecordsManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.csc.RecordInFileLocation;

public class DatabaseRecordsManagerImpl implements DatabaseRecordsManager {

  private final int valueFileSize;
  private final Stack<RecordInFileLocation> freeBlocs = new Stack<>();
  /**
   * database files named exactly as {currentFile}
   */
  private int currentFile = 0;
  private int currentFileOffset = 0;
  private final Path workingDir;

  public DatabaseRecordsManagerImpl(Path workingDir, int valueFileSize) {
    this.workingDir = workingDir;
    this.valueFileSize = valueFileSize;
  }

  private String getCurrentFilename() {
    return workingDir.resolve(Integer.toString(currentFile)).toString();
  }

  private RecordInFileLocation writeToBlock(byte[] data, int offset, int lengthToWrite,
      RecordInFileLocation block) throws IOException {

    try (RandomAccessFile accessFile = new RandomAccessFile(block.fileName(), "rw")) {
      accessFile.seek(block.offset());
      accessFile.write(data, offset, lengthToWrite);
    }

    return new RecordInFileLocation(block.fileName(), block.offset() + lengthToWrite,
        block.size() - lengthToWrite);
  }

  @Override
  public List<RecordInFileLocation> add(byte[] value) throws IOException {
    List<RecordInFileLocation> wroteBlocks = new ArrayList<>();
    int offset = 0;

    while (offset != value.length && !freeBlocs.isEmpty()) {
      var block = freeBlocs.pop();
      int currentLengthToWrite = Integer.min(value.length - offset, block.size());

      var shrankBlock = writeToBlock(value, offset, currentLengthToWrite, block);

      offset += currentLengthToWrite;
      wroteBlocks.add(new RecordInFileLocation(block.fileName(),
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
        var newBlock = new RecordInFileLocation(getCurrentFilename(), currentFileOffset,
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
  public InputStream openBlockStream(RecordInFileLocation location) throws IOException {
    return new InputStream() {
      final byte[] value = new byte[location.size()];
      int currentPos = -1;

      @Override
      public int read() throws IOException {
        if (value.length == 0) {
          return -1;
        }
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
  public void remove(List<RecordInFileLocation> valueBlocksLocations) throws IOException {
    freeBlocs.addAll(valueBlocksLocations);
  }

  @Override
  public void close() throws IOException {
  }
}
