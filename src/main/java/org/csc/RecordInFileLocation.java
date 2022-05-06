package org.csc;

import java.io.Serializable;

public record RecordInFileLocation(String fileName, int offset, int size) implements Serializable {

}
