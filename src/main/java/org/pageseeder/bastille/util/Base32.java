/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.util;

/**
 * A Base32 implementation which encodes and decodes according to RFC3548's Base32.
 *
 * <p>This class is a slightly modified version of code originally written by Robert Kaye and
 * Gordon Mohr which is now in the public domain.
 *
 * @see <a href="http://tools.ietf.org/html/rfc3548">RFC 3568 - The Base16, Base32, and Base64 Data Encodings</a>
 *
 * @author Christophe Lauret
 * @version Bastille 0.6.7
 */
public final class Base32 {

  /** Characters used in Base32 Encoding. */
  private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

  /** Lookup table. */
  private static final int[] BASE32_LOOKUP = {
      0xFF, 0xFF, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, // '0', '1', '2', '3', '4', '5', '6', '7'
      0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // '8', '9', ':', ';', '<', '=', '>', '?'
      0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G'
      0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, // 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O'
      0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, // 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W'
      0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 'X', 'Y', 'Z', '[', '\', ']', '^', '_'
      0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g'
      0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, // 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o'
      0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, // 'p', 'q', 'r', 's', 't', 'u', 'v', 'w'
      0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF // 'x', 'y', 'z', '{', '|', '}', '~', 'DEL'
  };

  /** Utility class */
  private Base32() {
  }

  /**
   * Encodes byte array to Base32 String.
   *
   * @param bytes Bytes to encode.
   * @return Encoded byte array <code>bytes</code> as a String.
   */
  public static String encode(final byte[] bytes) {
    int i = 0, index = 0, digit;
    StringBuilder base32 = new StringBuilder((bytes.length + 7) * 8 / 5);

    while (i < bytes.length) {
      int currByte = toUnsigned(bytes[i]);

      /* Is the current digit going to span a byte boundary? */
      if (index > 3) {
        int nextByte = (i + 1 < bytes.length) ? toUnsigned(bytes[i + 1]) : 0;
        digit = currByte & (0xFF >> index);
        index = (index + 5) % 8;
        digit <<= index;
        digit |= nextByte >> (8 - index);
        i++;
      } else {
        digit = (currByte >> (8 - (index + 5))) & 0x1F;
        index = (index + 5) % 8;
        if (index == 0) {
          i++;
        }
      }
      base32.append(BASE32_CHARS.charAt(digit));
    }

    return base32.toString();
  }

  /**
   * Decodes the given Base32 String to a raw byte array.
   *
   * @param base32 A string encoded as base32.
   * @return Decoded <code>base32</code> String as a raw byte array.
   */
  public static byte[] decode(final String base32) {
    byte[] bytes = new byte[base32.length() * 5 / 8];
    int[] state = {0, 0}; // state[0]=index, state[1]=offset
    for (int i = 0; i < base32.length(); i++) {
      int lookup = base32.charAt(i) - '0';
      if (lookup < 0 || lookup >= BASE32_LOOKUP.length) continue;
      int digit = BASE32_LOOKUP[lookup];
      if (digit == 0xFF) continue;
      if (applyDigit(bytes, digit, state)) break;
    }
    return bytes;
  }

  private static int toUnsigned(byte b) {
    return b >= 0 ? b : b + 256;
  }

  /** Writes {@code digit} into {@code bytes} at the position encoded in {@code state} ([index, offset]).
   *  Returns {@code true} when the output buffer is full and decoding should stop. */
  private static boolean applyDigit(byte[] bytes, int digit, int[] state) {
    int index = state[0];
    int offset = state[1];
    if (index <= 3) {
      index = (index + 5) % 8;
      if (index == 0) {
        bytes[offset] |= digit;
        state[0] = index;
        state[1] = offset + 1;
        return state[1] >= bytes.length;
      }
      bytes[offset] |= digit << (8 - index);
    } else {
      index = (index + 5) % 8;
      bytes[offset] |= (digit >>> index);
      offset++;
      if (offset >= bytes.length) {
        state[0] = index;
        state[1] = offset;
        return true;
      }
      bytes[offset] |= digit << (8 - index);
    }
    state[0] = index;
    state[1] = offset;
    return false;
  }
}
