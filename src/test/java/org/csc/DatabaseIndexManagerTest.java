package org.csc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.csc.databaseIndexManager.DatabaseIndexManager;
import org.csc.databaseIndexManager.DatabaseIndexManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class DatabaseIndexManagerTest {

  Path workingDir = Path.of("src/test/java/resources/");

  @BeforeEach
  public void deleteIndexFile() {
    workingDir.resolve("index").toFile().delete();
  }

  @BeforeEach
  public void createWorkingDir() {
    workingDir.toFile().mkdir();
  }

  @Test
  public void singleFileBlockLocationWriteAndRead() throws IOException {
    DatabaseIndexManager databaseIndexManager = new DatabaseIndexManagerImpl(workingDir);
    RecordInFileLocation recordInFileLocation = new RecordInFileLocation("some file name", -5000, 56789);
    String key = "Key for test";

    databaseIndexManager.add(key.getBytes(), List.of(recordInFileLocation));

    var result = databaseIndexManager.getFileBlocksLocations(key.getBytes());
    assertEquals(result, List.of(recordInFileLocation));
  }

  @Test
  public void multipleFileBlockLocationWriteAndRead() throws IOException {
    DatabaseIndexManager databaseIndexManager = new DatabaseIndexManagerImpl(workingDir);
    ArrayList<RecordInFileLocation> expected = new ArrayList<>();
    for (int i = 0; i < 1000; ++i) {
      expected.add(new RecordInFileLocation("file block name #" + i, i, -i));
    }
    String key = "Key for test";

    databaseIndexManager.add(key.getBytes(), expected);

    var result = databaseIndexManager.getFileBlocksLocations(key.getBytes());
    assertEquals(expected, result);
  }

  @Test
  public void multipleReadsAndWrited() throws IOException {
    DatabaseIndexManager databaseIndexManager = new DatabaseIndexManagerImpl(workingDir);
    ArrayList<RecordInFileLocation> expected1 = new ArrayList<>();
    for (int i = 0; i < 10; ++i) {
      expected1.add(new RecordInFileLocation("file block name in expected1 #" + i, i, -i));
    }
    ArrayList<RecordInFileLocation> expected2 = new ArrayList<>();
    for (int i = 0; i < 10; ++i) {
      expected2.add(new RecordInFileLocation("file block name in expected2 #" + i, i * 2, -i * 2));
    }
    String key1 = "Key for expected1";
    String key2 = "Key for expected2";

    databaseIndexManager.add(key1.getBytes(), expected1);
    databaseIndexManager.add(key2.getBytes(), expected2);

    var result1 = databaseIndexManager.getFileBlocksLocations(key1.getBytes());
    assertEquals(expected1, result1);

    var result2 = databaseIndexManager.getFileBlocksLocations(key2.getBytes());
    assertEquals(expected2, result2);
  }
}
