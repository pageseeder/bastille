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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Base32Test {

  // RFC 4648 test vectors (no padding)

  @Test
  void encode_empty() {
    assertEquals("", Base32.encode(new byte[0]));
  }

  @Test
  void encode_oneByte() {
    assertEquals("MY", Base32.encode(bytes("f")));
  }

  @Test
  void encode_twoBytes() {
    assertEquals("MZXQ", Base32.encode(bytes("fo")));
  }

  @Test
  void encode_threeBytes() {
    assertEquals("MZXW6", Base32.encode(bytes("foo")));
  }

  @Test
  void encode_fourBytes() {
    assertEquals("MZXW6YQ", Base32.encode(bytes("foob")));
  }

  @Test
  void encode_fiveBytes() {
    assertEquals("MZXW6YTB", Base32.encode(bytes("fooba")));
  }

  @Test
  void encode_sixBytes() {
    assertEquals("MZXW6YTBOI", Base32.encode(bytes("foobar")));
  }

  @Test
  void decode_empty() {
    assertArrayEquals(new byte[0], Base32.decode(""));
  }

  @Test
  void decode_twoChars() {
    assertArrayEquals(bytes("f"), Base32.decode("MY"));
  }

  @Test
  void decode_fourChars() {
    assertArrayEquals(bytes("fo"), Base32.decode("MZXQ"));
  }

  @Test
  void decode_tenChars() {
    assertArrayEquals(bytes("foobar"), Base32.decode("MZXW6YTBOI"));
  }

  @Test
  void decode_caseInsensitive() {
    assertArrayEquals(bytes("foobar"), Base32.decode("mzxw6ytboi"));
  }

  @Test
  void decode_ignoresPaddingChars() {
    // '=' is outside the lookup table and is silently skipped; output length
    // is determined by total input length, so only the first byte encodes 'f'.
    byte[] result = Base32.decode("MY======");
    assertNotNull(result);
    assertTrue(result.length > 0);
    assertEquals((byte) 'f', result[0]);
  }

  @Test
  void decode_ignoresUnrecognisedChars() {
    assertArrayEquals(bytes("f"), Base32.decode("M-Y"));
  }

  @Test
  void roundTrip_ascii() {
    byte[] original = bytes("Hello, World!");
    assertArrayEquals(original, Base32.decode(Base32.encode(original)));
  }

  @Test
  void roundTrip_fiveBytes_boundary() {
    // 5 bytes encode to exactly 8 Base32 chars with no leftover bits
    byte[] original = {0x00, 0x01, 0x02, 0x03, 0x04};
    assertArrayEquals(original, Base32.decode(Base32.encode(original)));
  }

  @Test
  void roundTrip_allByteValues() {
    byte[] original = new byte[256];
    for (int i = 0; i < 256; i++) {
      original[i] = (byte) i;
    }
    assertArrayEquals(original, Base32.decode(Base32.encode(original)));
  }

  private static byte[] bytes(String s) {
    return s.getBytes(StandardCharsets.US_ASCII);
  }
}
