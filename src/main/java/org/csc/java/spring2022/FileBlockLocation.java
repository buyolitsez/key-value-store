package org.csc.java.spring2022;

import java.io.Serializable;
import java.util.Objects;

/**
 * Класс-дескриптор блока, в котором хранится значение.
 * <p>
 * Если вам это потребуется, можете заменить этот record на class.
 */
public record FileBlockLocation(String fileName, int offset, int size) implements Serializable {

}
