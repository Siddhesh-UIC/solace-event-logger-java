package com.messagelogger.writer;

import com.messagelogger.processor.MessageRecord;
import java.io.IOException;
import java.util.List;

public interface MessageWriter {
    void write(List<MessageRecord> batch) throws IOException;
    void flush() throws IOException;
    void close() throws IOException;
}
