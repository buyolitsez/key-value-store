package org.csc.java.spring2022.IndexManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.csc.java.spring2022.FileBlockLocation;
import org.csc.java.spring2022.NotImplementedException;

public class IndexManagerImpl implements IndexManager {

  private final String indexFilename;
  private final Map<Integer, Integer> fromKeyHashcodeToOffset = new HashMap<>();
  private final Map<Integer, Integer> fromKeyHashcodeToLength = new HashMap<>();
  private int totalyWrote = 0;
  private final RandomAccessFile fileAccess;

  public IndexManagerImpl(Path workingDir) throws IOException {
    Path pathToIndexFile = workingDir.resolve("index");
    pathToIndexFile.toFile().createNewFile();
    indexFilename = pathToIndexFile.toString();
    fileAccess = new RandomAccessFile(indexFilename, "rw");
  }

  @Override
  public void add(byte[] key, List<FileBlockLocation> writtenBlocks) throws IOException {
    int keyHashcode = Arrays.hashCode(key);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(writtenBlocks);
    byte[] currentArray = baos.toByteArray();

    fileAccess.seek(totalyWrote);
    fileAccess.write(currentArray);
    fromKeyHashcodeToOffset.put(keyHashcode, totalyWrote);
    fromKeyHashcodeToLength.put(keyHashcode, currentArray.length);

    totalyWrote += currentArray.length;
  }

  @Override
  public void remove(byte[] key) throws IOException {
//    throw new NotImplementedException();
    //TODO
  }

  @Override
  public List<FileBlockLocation> getFileBlocksLocations(byte[] key) throws IOException {
    int keyHashcode = Arrays.hashCode(key);
    int byteArrayOffset = fromKeyHashcodeToOffset.get(keyHashcode);
    int byteArrayLength = fromKeyHashcodeToLength.get(keyHashcode);

    fileAccess.seek(byteArrayOffset);
    byte[] data = new byte[byteArrayLength];
    fileAccess.read(data);

    ByteArrayInputStream in = new ByteArrayInputStream(data);
    ObjectInputStream is = new ObjectInputStream(in);

    List<FileBlockLocation> result;
    try {
      result = (List<FileBlockLocation>) is.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException("Data is wrong formatted, can't cast to List<FileBlockLocation>");
    }
    return result;
  }

  @Override
  public void close() throws IOException {
    fileAccess.close();
  }
}
