package org.csc.java.spring2022;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.csc.java.spring2022.IndexManager.IndexManager;
import org.csc.java.spring2022.IndexManager.IndexManagerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class IndexManagerTest {

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
    IndexManager indexManager = new IndexManagerImpl(workingDir);
    FileBlockLocation fileBlockLocation = new FileBlockLocation("some file name", -5000, 56789);
    String key = "Key for test";

    indexManager.add(key.getBytes(), List.of(fileBlockLocation));

    var result = indexManager.getFileBlocksLocations(key.getBytes());
    assertEquals(result, List.of(fileBlockLocation));
  }

  @Test
  public void multipleFileBlockLocationWriteAndRead() throws IOException {
    IndexManager indexManager = new IndexManagerImpl(workingDir);
    ArrayList<FileBlockLocation> expected = new ArrayList<>();
    for (int i = 0; i < 1000; ++i) {
      expected.add(new FileBlockLocation("file block name #" + i, i, -i));
    }
    String key = "Key for test";

    indexManager.add(key.getBytes(), expected);

    var result = indexManager.getFileBlocksLocations(key.getBytes());
    assertEquals(expected, result);
  }

  @Test
  public void multipleReadsAndWrited() throws IOException {
    IndexManager indexManager = new IndexManagerImpl(workingDir);
    ArrayList<FileBlockLocation> expected1 = new ArrayList<>();
    for (int i = 0; i < 10; ++i) {
      expected1.add(new FileBlockLocation("file block name in expected1 #" + i, i, -i));
    }
    ArrayList<FileBlockLocation> expected2 = new ArrayList<>();
    for (int i = 0; i < 10; ++i) {
      expected2.add(new FileBlockLocation("file block name in expected2 #" + i, i * 2, -i * 2));
    }
    String key1 = "Key for expected1";
    String key2 = "Key for expected2";

    indexManager.add(key1.getBytes(), expected1);
    indexManager.add(key2.getBytes(), expected2);

    var result1 = indexManager.getFileBlocksLocations(key1.getBytes());
    assertEquals(expected1, result1);

    var result2 = indexManager.getFileBlocksLocations(key2.getBytes());
    assertEquals(expected2, result2);
  }
}
