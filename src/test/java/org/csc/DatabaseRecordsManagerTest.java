package org.csc;

import java.io.InputStream;
import java.util.List;
import java.util.Random;
import org.csc.databaseRecordsManager.DatabaseRecordsManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseRecordsManagerTest {

  Path workingDir = Path.of("src/test/java/resources/");
  Random random = new Random(5);

  @BeforeEach
  public void createWorkingDir() {
    workingDir.toFile().mkdir();
  }

  private String getPath(String s) {
    return workingDir.resolve(s).toString();
  }

  private void checkStream(byte[] expected, InputStream in) throws IOException {
    for (byte x : expected) {
      int y = in.read();
      assertEquals(x, y);
    }
    assertEquals(-1, in.read());
  }

  @Test
  void singleAdd() throws IOException {
    var manager = new DatabaseRecordsManagerImpl(workingDir, 10);
    byte[] value = new byte[10];
    random.nextBytes(value);

    var blocks = manager.add(value);
    var expected = List.of(new RecordInFileLocation(getPath("0"), 0, 10));
    assertEquals(expected, blocks);
  }

  @Test
  void twoAdds() throws IOException {
    var manager = new DatabaseRecordsManagerImpl(workingDir, 15);
    byte[] value1 = new byte[10];
    byte[] value2 = new byte[10];
    random.nextBytes(value1);
    random.nextBytes(value2);

    var blocks1 = manager.add(value1);
    var expected1 = List.of(new RecordInFileLocation(getPath("0"), 0, 10));
    assertEquals(expected1, blocks1);

    var blocks2 = manager.add(value2);
    var expected2 = List.of(
        new RecordInFileLocation(getPath("0"), 10, 5),
        new RecordInFileLocation(getPath("1"), 0, 5));
    assertEquals(expected2, blocks2);
  }

  @Test
  void addAndOpenBlockStream() throws IOException {
    var manager = new DatabaseRecordsManagerImpl(workingDir, 10);
    byte[] value = new byte[10];
    random.nextBytes(value);

    var blocks = manager.add(value);
    var expected = List.of(new RecordInFileLocation(getPath("0"), 0, 10));
    assertEquals(expected, blocks);
    var input = manager.openBlockStream(blocks.get(0));
    checkStream(value, input);
  }

  @Test
  void addAndRemove() throws IOException {
    var manager = new DatabaseRecordsManagerImpl(workingDir, 10);
    byte[] value1 = new byte[10];
    byte[] value2 = new byte[10];
    random.nextBytes(value1);
    random.nextBytes(value2);

    var blocks1 = manager.add(value1);
    var expected1 = List.of(new RecordInFileLocation(getPath("0"), 0, 10));
    assertEquals(expected1, blocks1);
    checkStream(value1, manager.openBlockStream(blocks1.get(0)));

    manager.remove(blocks1);

    var blocks2 = manager.add(value2);
    var expected2 = List.of(new RecordInFileLocation(getPath("0"), 0, 10));
    assertEquals(expected2, blocks2);
    checkStream(value2, manager.openBlockStream(blocks2.get(0)));
  }
}
