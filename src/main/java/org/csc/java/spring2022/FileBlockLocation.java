package org.csc.java.spring2022;

/**
 * Класс-дескриптор блока, в котором хранится значение.
 * <p>
 * Если вам это потребуется, можете заменить этот record на class.
 */
record FileBlockLocation(String fileName, int offset, int size) {

}
