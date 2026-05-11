package com.messagelogger.writer;

import com.messagelogger.processor.MessageRecord;
import java.io.IOException;
import java.util.List;

public interface MessageWriter {
    void write(List<MessageRecord> batch) throws IOException;
    default void flush() throws IOException {}
    default void close() throws IOException {}
}
