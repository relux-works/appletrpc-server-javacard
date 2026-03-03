package com.appletrpc.server;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Base class for appletRPC-generated skeletons.
 *
 * Provides:
 * - CLA validation (subclass sets via constructor)
 * - INS dispatch (subclass implements {@link #dispatch(APDU, byte)})
 * - Response helpers: sendU8, sendU16, sendBytes
 * - Request helpers: readU8, readU16, readBytes
 */
public abstract class AppletBase extends Applet {

    private final byte cla;

    protected AppletBase(byte cla) {
        this.cla = cla;
    }

    @Override
    public void process(APDU apdu) {
        if (selectingApplet()) return;

        byte[] buf = apdu.getBuffer();
        if (buf[ISO7816.OFFSET_CLA] != cla) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        dispatch(apdu, buf[ISO7816.OFFSET_INS]);
    }

    /**
     * Dispatch INS byte to the appropriate handler.
     * Generated skeletons override this with a switch statement.
     */
    protected abstract void dispatch(APDU apdu, byte ins);

    // --- Response helpers ---

    /**
     * Send a single unsigned byte (u8) as response.
     */
    protected void sendU8(APDU apdu, byte value) {
        byte[] buf = apdu.getBuffer();
        buf[0] = value;
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }

    /**
     * Send an unsigned short (u16, big-endian) as response.
     */
    protected void sendU16(APDU apdu, short value) {
        byte[] buf = apdu.getBuffer();
        Util.setShort(buf, (short) 0, value);
        apdu.setOutgoingAndSend((short) 0, (short) 2);
    }

    /**
     * Send an unsigned 32-bit integer (u32, big-endian, 4 bytes) as response.
     * Java Card Classic has no native int; this packs 4 bytes manually.
     */
    protected void sendU32(APDU apdu, int value) {
        byte[] buf = apdu.getBuffer();
        buf[0] = (byte) ((value >> 24) & 0xFF);
        buf[1] = (byte) ((value >> 16) & 0xFF);
        buf[2] = (byte) ((value >> 8) & 0xFF);
        buf[3] = (byte) (value & 0xFF);
        apdu.setOutgoingAndSend((short) 0, (short) 4);
    }

    /**
     * Send a boolean (1 byte: 0x00 = false, 0x01 = true) as response.
     */
    protected void sendBool(APDU apdu, boolean value) {
        byte[] buf = apdu.getBuffer();
        buf[0] = value ? (byte) 0x01 : (byte) 0x00;
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }

    /**
     * Send a byte array as response.
     */
    protected void sendBytes(APDU apdu, byte[] data, short offset, short length) {
        byte[] buf = apdu.getBuffer();
        Util.arrayCopyNonAtomic(data, offset, buf, (short) 0, length);
        apdu.setOutgoingAndSend((short) 0, length);
    }

    /**
     * Send multiple fields already packed in buf[0..length-1] as response.
     */
    protected void sendBuffer(APDU apdu, short length) {
        apdu.setOutgoingAndSend((short) 0, length);
    }

    // --- Request helpers ---

    /**
     * Read P1 as unsigned byte (u8).
     */
    protected byte readP1(APDU apdu) {
        return apdu.getBuffer()[ISO7816.OFFSET_P1];
    }

    /**
     * Read P2 as unsigned byte (u8).
     */
    protected byte readP2(APDU apdu) {
        return apdu.getBuffer()[ISO7816.OFFSET_P2];
    }

    /**
     * Receive command data and return the number of bytes read.
     * After this call, data starts at ISO7816.OFFSET_CDATA in the APDU buffer.
     */
    protected short receiveData(APDU apdu) {
        return apdu.setIncomingAndReceive();
    }

    /**
     * Read a u8 from APDU data at the given offset (relative to CDATA).
     */
    protected byte readU8(byte[] buf, short offset) {
        return buf[(short) (ISO7816.OFFSET_CDATA + offset)];
    }

    /**
     * Read a u16 (big-endian) from APDU data at the given offset (relative to CDATA).
     */
    protected short readU16(byte[] buf, short offset) {
        return Util.getShort(buf, (short) (ISO7816.OFFSET_CDATA + offset));
    }

    /**
     * Read a u32 (big-endian, 4 bytes) from APDU data at the given offset (relative to CDATA).
     */
    protected int readU32(byte[] buf, short offset) {
        short base = (short) (ISO7816.OFFSET_CDATA + offset);
        return ((buf[base] & 0xFF) << 24)
             | ((buf[(short) (base + 1)] & 0xFF) << 16)
             | ((buf[(short) (base + 2)] & 0xFF) << 8)
             | (buf[(short) (base + 3)] & 0xFF);
    }

    /**
     * Read a boolean from APDU data at the given offset (relative to CDATA).
     * 0x00 = false, non-zero = true.
     */
    protected boolean readBool(byte[] buf, short offset) {
        return buf[(short) (ISO7816.OFFSET_CDATA + offset)] != (byte) 0x00;
    }

    /**
     * Pack a u8 into buffer at the given offset. Returns next offset.
     */
    protected short packU8(byte[] buf, short offset, byte value) {
        buf[offset] = value;
        return (short) (offset + 1);
    }

    /**
     * Pack a u16 (big-endian) into buffer at the given offset. Returns next offset.
     */
    protected short packU16(byte[] buf, short offset, short value) {
        Util.setShort(buf, offset, value);
        return (short) (offset + 2);
    }

    /**
     * Pack a u32 (big-endian, 4 bytes) into buffer at the given offset. Returns next offset.
     */
    protected short packU32(byte[] buf, short offset, int value) {
        buf[offset] = (byte) ((value >> 24) & 0xFF);
        buf[(short) (offset + 1)] = (byte) ((value >> 16) & 0xFF);
        buf[(short) (offset + 2)] = (byte) ((value >> 8) & 0xFF);
        buf[(short) (offset + 3)] = (byte) (value & 0xFF);
        return (short) (offset + 4);
    }

    /**
     * Pack a boolean (0x00/0x01) into buffer at the given offset. Returns next offset.
     */
    protected short packBool(byte[] buf, short offset, boolean value) {
        buf[offset] = value ? (byte) 0x01 : (byte) 0x00;
        return (short) (offset + 1);
    }

    /**
     * Pack a byte array into buffer at the given offset. Returns next offset.
     */
    protected short packBytes(byte[] buf, short offset, byte[] src, short srcOff, short srcLen) {
        Util.arrayCopyNonAtomic(src, srcOff, buf, offset, srcLen);
        return (short) (offset + srcLen);
    }
}
