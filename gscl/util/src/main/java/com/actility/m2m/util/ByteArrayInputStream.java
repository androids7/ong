/*
 * Copyright   Actility, SA. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Actility, SA.,  4, rue Ampere 22300 LANNION FRANCE
 * or visit www.actility.com if you need additional
 * information or have any questions.
 * 
 * id $Id: ByteArrayInputStream.java 6057 2013-10-14 11:50:13Z mlouiset $
 * author $Author: mlouiset $
 * version $Revision: 6057 $
 * lastrevision $Date: 2013-10-14 13:50:13 +0200 (Mon, 14 Oct 2013) $
 * modifiedby $LastChangedBy: mlouiset $
 * lastmodified $LastChangedDate: 2013-10-14 13:50:13 +0200 (Mon, 14 Oct 2013) $
 */

package com.actility.m2m.util;

import java.io.InputStream;

/**
 * None synchronized <code>ByteArrayInputStream</code>. It contains an internal buffer that contains bytes that may be read from
 * the stream. An internal counter keeps track of the next byte to be supplied by the <code>read</code> method.
 * <p>
 * Closing a <tt>ByteArrayInputStream</tt> has no effect. The methods in this class can be called after the stream has been
 * closed without generating an <tt>IOException</tt>.
 */
public final class ByteArrayInputStream extends InputStream {

    /**
     * An array of bytes that was provided by the creator of the stream. Elements <code>buf[0]</code> through
     * <code>buf[count-1]</code> are the only bytes that can ever be read from the stream; element <code>buf[pos]</code> is the
     * next byte to be read.
     */
    private final byte[] buf;

    /**
     * The index of the next character to read from the input stream buffer. This value should always be nonnegative and not
     * larger than the value of <code>count</code>. The next byte to be read from the input stream buffer will be
     * <code>buf[pos]</code>.
     */
    private int pos;

    /**
     * The currently marked position in the stream. ByteArrayInputStream objects are marked at position zero by default when
     * constructed. They may be marked at another position within the buffer by the <code>mark()</code> method. The current
     * buffer position is set to this point by the <code>reset()</code> method.
     * <p>
     * If no mark has been set, then the value of mark is the offset passed to the constructor (or 0 if the offset was not
     * supplied).
     *
     * @since JDK1.1
     */
    private int mark = 0;

    /**
     * The index one greater than the last valid character in the input stream buffer. This value should always be nonnegative
     * and not larger than the length of <code>buf</code>. It is one greater than the position of the last byte within
     * <code>buf</code> that can ever be read from the input stream buffer.
     */
    private final int count;

    /**
     * Creates a <code>ByteArrayInputStream</code> so that it uses <code>buf</code> as its buffer array. The buffer array is not
     * copied. The initial value of <code>pos</code> is <code>0</code> and the initial value of <code>count</code> is the length
     * of <code>buf</code>.
     *
     * @param buf the input buffer.
     */
    public ByteArrayInputStream(byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    /**
     * Creates <code>ByteArrayInputStream</code> that uses <code>buf</code> as its buffer array. The initial value of
     * <code>pos</code> is <code>offset</code> and the initial value of <code>count</code> is the minimum of
     * <code>offset+length</code> and <code>buf.length</code>. The buffer array is not copied. The buffer's mark is set to the
     * specified offset.
     *
     * @param buf the input buffer.
     * @param offset the offset in the buffer of the first byte to read.
     * @param length the maximum number of bytes to read from the buffer.
     */
    public ByteArrayInputStream(byte buf[], int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }

    /**
     * Reads the next byte of data from this input stream. The value byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available because the end of the stream has been reached, the value
     * <code>-1</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the stream has been reached.
     */
    public int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    /**
     * Reads up to <code>len</code> bytes of data into an array of bytes from this input stream. If <code>pos</code> equals
     * <code>count</code>, then <code>-1</code> is returned to indicate end of file. Otherwise, the number <code>k</code> of
     * bytes read is equal to the smaller of <code>len</code> and <code>count-pos</code>. If <code>k</code> is positive, then
     * bytes <code>buf[pos]</code> through <code>buf[pos+k-1]</code> are copied into <code>b[off]</code> through
     * <code>b[off+k-1]</code> in the manner performed by <code>System.arraycopy</code>. The value <code>k</code> is added into
     * <code>pos</code> and <code>k</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of
     *         the stream has been reached.
     */
    public int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException("Byte array must not be null");
        } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException("Offset and length parameters go beyond the byte buffer length");
        }
        if (pos >= count) {
            return -1;
        }
        if (pos + len > count) {
            len = count - pos;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    /**
     * Skips <code>n</code> bytes of input from this input stream. Fewer bytes might be skipped if the end of the input stream
     * is reached. The actual number <code>k</code> of bytes to be skipped is equal to the smaller of <code>n</code> and
     * <code>count-pos</code>. The value <code>k</code> is added into <code>pos</code> and <code>k</code> is returned.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     */
    public long skip(long n) {
        if (pos + n > count) {
            n = count - pos;
        }
        if (n < 0) {
            return 0;
        }
        pos += n;
        return n;
    }

    /**
     * Returns the number of bytes that can be read from this input stream without blocking. The value returned is
     * <code>count&nbsp;- pos</code>, which is the number of bytes remaining to be read from the input buffer.
     *
     * @return the number of bytes that can be read from the input stream without blocking.
     */
    public int available() {
        return count - pos;
    }

    /**
     * Tests if this <code>InputStream</code> supports mark/reset. The <code>markSupported</code> method of
     * <code>ByteArrayInputStream</code> always returns <code>true</code>.
     *
     * @since JDK1.1
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Set the current marked position in the stream. ByteArrayInputStream objects are marked at position zero by default when
     * constructed. They may be marked at another position within the buffer by this method.
     * <p>
     * If no mark has been set, then the value of the mark is the offset passed to the constructor (or 0 if the offset was not
     * supplied).
     *
     * @since JDK1.1
     */
    public void mark(int readAheadLimit) {
        mark = pos;
    }

    /**
     * Resets the buffer to the marked position. The marked position is 0 unless another position was marked or an offset was
     * specified in the constructor.
     */
    public void reset() {
        pos = mark;
    }

    /**
     * Closing a <tt>ByteArrayInputStream</tt> has no effect. The methods in this class can be called after the stream has been
     * closed without generating an <tt>IOException</tt>.
     * <p>
     */
    public void close() {
    }

}
