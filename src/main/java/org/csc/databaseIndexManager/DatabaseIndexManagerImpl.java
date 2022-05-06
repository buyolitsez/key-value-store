package org.csc.databaseIndexManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.csc.RecordInFileLocation;

public class DatabaseIndexManagerImpl implements DatabaseIndexManager {

  private final String indexFilename;
  private final File fromKeyHashcodeToOffsetFile;
  private final File fromKeyHashcodeToLengthFile;
  private Map<Integer, Integer> fromKeyHashcodeToOffset;
  private Map<Integer, Integer> fromKeyHashcodeToLength;
  private int totalyWrote = 0;
  private final RandomAccessFile fileAccess;

  public DatabaseIndexManagerImpl(Path workingDir) throws IOException {
    Path pathToIndexFile = workingDir.resolve("index");
    pathToIndexFile.toFile().createNewFile();
    indexFilename = pathToIndexFile.toString();
    fileAccess = new RandomAccessFile(indexFilename, "rw");
    fromKeyHashcodeToOffsetFile = workingDir.resolve("fromKeyHashCodeToOffset").toFile();
    fromKeyHashcodeToLengthFile = workingDir.resolve("fromKeyHashcodeToLength").toFile();

    try (var lengthInput = new FileInputStream(fromKeyHashcodeToLengthFile);
        var offsetInput = new FileInputStream(fromKeyHashcodeToOffsetFile)) {
      fromKeyHashcodeToOffset = convertBytes(offsetInput.readAllBytes());
      fromKeyHashcodeToLength = convertBytes(lengthInput.readAllBytes());
    } catch (IOException e) {
      fromKeyHashcodeToOffset = new HashMap<>();
      fromKeyHashcodeToLength = new HashMap<>();
    }
  }

  private byte[] getBytes(Object obj) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(obj);
    return baos.toByteArray();
  }

  @SuppressWarnings("unchecked")
  private <T> T convertBytes(byte[] value) throws IOException {
    ByteArrayInputStream in = new ByteArrayInputStream(value);
    ObjectInputStream is = new ObjectInputStream(in);

    T result;
    try {
      result = (T) is.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException("Data is wrong formatted, can't cast to List<FileBlockLocation>");
    }
    return result;
  }

  @Override
  public void add(byte[] key, List<RecordInFileLocation> writtenBlocks) throws IOException {
    int keyHashcode = Arrays.hashCode(key);

    byte[] currentArray = getBytes(writtenBlocks);

    fileAccess.seek(totalyWrote);
    fileAccess.write(currentArray);
    fromKeyHashcodeToOffset.put(keyHashcode, totalyWrote);
    fromKeyHashcodeToLength.put(keyHashcode, currentArray.length);

    totalyWrote += currentArray.length;
  }

  @Override
  public void remove(byte[] key) throws IOException {
    //TODO
    int keyHashcode = Arrays.hashCode(key);
    fromKeyHashcodeToOffset.remove(keyHashcode);
    fromKeyHashcodeToLength.remove(keyHashcode);
  }

  @Override
  public List<RecordInFileLocation> getFileBlocksLocations(byte[] key) throws IOException {
    int keyHashcode = Arrays.hashCode(key);
    if (!fromKeyHashcodeToOffset.containsKey(keyHashcode)) {
      return List.of();
    }
    int byteArrayOffset = fromKeyHashcodeToOffset.get(keyHashcode);
    int byteArrayLength = fromKeyHashcodeToLength.get(keyHashcode);

    fileAccess.seek(byteArrayOffset);
    byte[] data = new byte[byteArrayLength];
    fileAccess.read(data);

    List<RecordInFileLocation> result = convertBytes(data);
    if (result.isEmpty()) {
      return List.of(new RecordInFileLocation("zero size!", 0, 0));
    }
    return result;
  }

  @Override
  public void close() throws IOException {
    fileAccess.close();
    try (var lengthOutput = new FileOutputStream(fromKeyHashcodeToLengthFile);
        var offsetOutput = new FileOutputStream(fromKeyHashcodeToOffsetFile)) {
      offsetOutput.write(getBytes(fromKeyHashcodeToOffset));
      lengthOutput.write(getBytes(fromKeyHashcodeToLength));
    }
  }
}
