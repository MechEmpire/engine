package com.mechempire.engine.runtime;

import com.mechempire.engine.core.IByteReader;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/16 下午2:50
 */
@Component
public class CommandMessageReader implements IByteReader {

    private Integer offset;

    private byte[] commandSeq;

    public CommandMessageReader() {
    }

    public CommandMessageReader(byte[] commandSeq) {
        this.offset = 0;
        this.commandSeq = commandSeq;
    }

    @Override
    public byte readByte() {
        return commandSeq[offset++];
    }

    @Override
    public int readInt() {
        byte[] intBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            intBytes[i] = commandSeq[offset++];
        }
        return ByteBuffer.wrap(intBytes).getInt();
    }

    @Override
    public double readDouble() {
        byte[] doubleBytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            doubleBytes[i] = commandSeq[offset++];
        }
        return ByteBuffer.wrap(doubleBytes).getDouble();
    }

    @Override
    public void reset() {
        this.offset = 0;
    }

    public void setCommandSeq(byte[] commandSeq) {
        this.commandSeq = commandSeq;
    }
}