import java.io.*;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.lang.IllegalArgumentException;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ReadWriteBuffer
{
	private byte[] buffer;
	private int readerIndex;
	private int writerIndex;

	private ReadWriteBuffer(){}

	public ReadWriteBuffer(int length)
	{
		this.buffer = new byte[length];
	}

	public ReadWriteBuffer(byte[] buffer)
	{
		this.buffer = buffer;
		writerIndex = buffer.length;
	}

	public int capacity()
	{
		return buffer.length;
	}

	public int writerIndex() 
	{
		return writerIndex;
	}

	public int readerIndex()
	{
		return readerIndex;
	}

	public int readByte() 
	{
		return buffer[readerIndex++] & 0xFF;
	}

	public int readShort() 
	{
		return (readByte() << 0x8) | readByte();
	}

	public int readInt()
	{
		return (readByte() << 0x18) | (readByte() << 0x10) | (readByte() << 0x8) | readByte();
	}

	public int readMedium() 
	{
		if (buffer[readerIndex] < 0x0)
			return readShort() ^ 0x8000;
		return readByte();
	}

	public void writeByte(byte value)
	{
		buffer[writerIndex++] = value;
	}

	public void writeShort(int value)
	{
		buffer[writerIndex++] = (byte)(value >> 0x8);
		buffer[writerIndex++] = (byte) value;
	}

	public void writeInt(int value)
	{
		buffer[writerIndex++] = (byte)(value >> 0x18);
		buffer[writerIndex++] = (byte)(value >> 0x10);
		buffer[writerIndex++] = (byte)(value >> 0x8);
		buffer[writerIndex++] = (byte) value;
	}

	public void writeMedium(int value)
	{
		if (value > 127)
			writeShort((value & 0xFFFF) | 0x8000);
		else
			writeByte((byte) value);
	}

	public static ReadWriteBuffer read(String s, int seek, int length) 
	{
		byte[] bytes = new byte[length];
		try {
			RandomAccessFile file = new RandomAccessFile(s, "r");
			FileChannel channel = file.getChannel();
			MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, seek, length);
			buffer.get(bytes, 0, length);
		} catch (IOException io) {
			//System.err.println("IO Error File Not Found: " + s);
		} catch (BufferUnderflowException bufe) {
			//System.err.println("Read Out Bounds Exception: { Seek: " + seek + " , Length: " + length + " }");
		} catch (IllegalArgumentException iae) {
			//System.err.println("Seek Out Bounds Exception: { Seek: " + seek + " , Length: " + length + " }");
		}
		return new ReadWriteBuffer(bytes);
	}

	public static ReadWriteBuffer read(String s) 
	{
		File file = new File(s);
		int length = (int)file.length();
		byte[] bytes = new byte[length];
		try 
		{
			DataInputStream data = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        		data.readFully(bytes,0,length);
			data.close();
		} catch (IOException io) {
			//System.err.println((new StringBuilder()).append("IO ERROR - Directory N/A: ").append(file).append(", Length").append(length).toString());
		}
		return new ReadWriteBuffer(bytes);
	}

	public void write(String s) 
	{
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(s));
        		dos.write(buffer, 0, writerIndex == 0 ? buffer.length : writerIndex);
			dos.close();
		} catch (IOException io) {
			System.err.println("IO Error - Directory N/A: ".concat(s));
		}
	}
}