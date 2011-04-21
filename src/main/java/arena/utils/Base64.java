/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.utils;

import org.apache.commons.logging.LogFactory;

/**
 * Encoder / decoder for base 64 strings
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class Base64 {    
    private static char[] B64_ENCODE_ARRAY = new char[] {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', // large alphabet
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', // small alphabet
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'  // numbers + extras
    };
    
    private static byte[] B64_DECODE_ARRAY = new byte[] {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        62, // Plus sign
        -1, -1, -1,
        63, // Slash
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers
        -1, -1, -1, -1, -1, -1, -1,
         0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 
        13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Large letters
        -1, -1, -1, -1, -1, -1,
        26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Small letters
        -1, -1, -1, -1
    };

    /**
     * Encodes a string into base 64 format equals-terminated
     */
    public static String encodeBase64(byte[] inBytes) {
        char[] outChars = new char[(int) Math.ceil(inBytes.length / 3f) * 4];
        LogFactory.getLog(Base64.class).debug("Base64 encoding: in=" + inBytes.length + 
                " bytes, out=" + outChars.length + " chars");
        encodeBase64(inBytes, outChars);

        return new String(outChars);
    }

    public static void encodeBase64(byte[] input, char[] output) {
        encodeBase64(input, output, 0, input.length, 0);
    }

    /**
     * Encodes a byte array from base64
     */
    public static void encodeBase64(byte[] input, char[] output, 
            int inOffset, int inLength, int outOffset) {

        int outIndex = outOffset;
        for (int inIndex = inOffset; inIndex < inLength; ) {
            // Encode three bytes
            int thisPassInBytes = Math.min(inLength - inIndex, 3);

            if (thisPassInBytes == 1) {
                int posOne = (input[inIndex] & 0xFC) >> 2;
                int posTwo = (input[inIndex] & 0x03) << 4;

                output[outIndex] = B64_ENCODE_ARRAY[posOne];
                output[outIndex + 1] = B64_ENCODE_ARRAY[posTwo];
                output[outIndex + 2] = '=';
                output[outIndex + 3] = '=';
            } else if (thisPassInBytes == 2) {
                int posOne = ((input[inIndex] & 0xFC) >> 2);
                int posTwo = ((input[inIndex] & 0x03) << 4) | ((input[inIndex + 1] & 0xF0) >> 4);
                int posThree = ((input[inIndex + 1] & 0x0F) << 2);

                output[outIndex] = B64_ENCODE_ARRAY[posOne];
                output[outIndex + 1] = B64_ENCODE_ARRAY[posTwo];
                output[outIndex + 2] = B64_ENCODE_ARRAY[posThree];
                output[outIndex + 3] = '=';
            } else if (thisPassInBytes == 3) {
                int posOne = ((input[inIndex] & 0xFC) >> 2);
                int posTwo = ((input[inIndex] & 0x03) << 4) | ((input[inIndex + 1] & 0xF0) >> 4);
                int posThree = ((input[inIndex + 1] & 0x0F) << 2) | ((input[inIndex + 2] & 0xC0) >> 6);
                int posFour = (input[inIndex + 2] & 0x3F);

                output[outIndex] = B64_ENCODE_ARRAY[posOne];
                output[outIndex + 1] = B64_ENCODE_ARRAY[posTwo];
                output[outIndex + 2] = B64_ENCODE_ARRAY[posThree];
                output[outIndex + 3] = B64_ENCODE_ARRAY[posFour];
            }
            outIndex += 4;
            inIndex += thisPassInBytes;
        }
    }

    /**
     * Expects the classic base64 "abcdefgh=" syntax (equals padded)
     * and decodes it to original form
     */
    public static byte[] decodeBase64(String input) {
        char[] inBytes = input.toCharArray();
        byte[] outBytes = new byte[(int) (inBytes.length * 0.75f)]; // always mod 4 = 0
        
        LogFactory.getLog(Base64.class).debug("Base64 decoding: in=" + inBytes.length + 
                " chars, out=" + outBytes.length + " bytes");
        int length = decodeBase64(inBytes, outBytes);
        byte returnValue[] = new byte[length];
        System.arraycopy(outBytes, 0, returnValue, 0, length);
        return returnValue;
    }

    public static int decodeBase64(char[] input, byte[] output) {
        if (input.length % 4 != 0) {
            throw new RuntimeException("Invalid base 64 string: length=" + input.length);
        }
        return decodeBase64(input, output, 0, input.length, 0);
    }

    /**
     * Decodes a byte array from base64
     */
    public static int decodeBase64(char[] input, byte[] output, 
            int inOffset, int inLength, int outOffset) {
        if (inLength == 0) {
            return 0;
        }

        int outIndex = outOffset;
        for (int inIndex = inOffset; inIndex < inLength; ) {
            // Decode four bytes
            int thisPassInBytes = Math.min(inLength - inIndex, 4);
            while ((thisPassInBytes > 1) && 
                    (input[inIndex + thisPassInBytes - 1] == '=')) {
                thisPassInBytes--;
            }

            if (thisPassInBytes == 2) {
                int outBuffer = ((B64_DECODE_ARRAY[input[inIndex]] & 0xFF) << 18)
                            | ((B64_DECODE_ARRAY[input[inIndex + 1]] & 0xFF) << 12);
                output[outIndex] = (byte) ((outBuffer >> 16) & 0xFF);
                outIndex += 1;
            } else if (thisPassInBytes == 3) {
                int outBuffer = ((B64_DECODE_ARRAY[input[inIndex]] & 0xFF) << 18)
                            | ((B64_DECODE_ARRAY[input[inIndex + 1]] & 0xFF) << 12)
                            | ((B64_DECODE_ARRAY[input[inIndex + 2]] & 0xFF) << 6);
                output[outIndex] = (byte) ((outBuffer >> 16) & 0xFF);
                output[outIndex + 1] = (byte) ((outBuffer >> 8) & 0xFF);
                outIndex += 2;
            } else if (thisPassInBytes == 4) {
                int outBuffer = ((B64_DECODE_ARRAY[input[inIndex]] & 0xFF) << 18)
                            | ((B64_DECODE_ARRAY[input[inIndex + 1]] & 0xFF) << 12)
                            | ((B64_DECODE_ARRAY[input[inIndex + 2]] & 0xFF) << 6)
                            | (B64_DECODE_ARRAY[input[inIndex + 3]] & 0xFF);
                output[outIndex] = (byte) ((outBuffer >> 16) & 0xFF);
                output[outIndex + 1] = (byte) ((outBuffer >> 8) & 0xFF);
                output[outIndex + 2] = (byte) (outBuffer & 0xFF);
                outIndex += 3;
            }
            inIndex += thisPassInBytes;
        }
        return outIndex;
    }
}
