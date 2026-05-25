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
package org.pageseeder.bastille.security;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObfuscatorTest {

  // --- round-trip tests (obfuscate → clear) ---

  @Test
  void roundTrip_commonPassword() {
    assertEquals("password", Obfuscator.clear(Obfuscator.obfuscate("password")));
  }

  @Test
  void roundTrip_emptyString() {
    assertEquals("", Obfuscator.clear(Obfuscator.obfuscate("")));
  }

  @Test
  void roundTrip_singleChar() {
    assertEquals("x", Obfuscator.clear(Obfuscator.obfuscate("x")));
  }

  @Test
  void roundTrip_passwordContainingDigits() {
    assertEquals("p4ssw0rd", Obfuscator.clear(Obfuscator.obfuscate("p4ssw0rd")));
  }

  @Test
  void roundTrip_passwordContainingDigitOne() {
    // The digit '1' in the password is Base32-encoded so it never appears in the
    // obfuscated form, keeping the separator logic intact.
    assertEquals("p1ssword", Obfuscator.clear(Obfuscator.obfuscate("p1ssword")));
  }

  @Test
  void roundTrip_specialCharacters() {
    assertEquals("p@$$w0rd!", Obfuscator.clear(Obfuscator.obfuscate("p@$$w0rd!")));
  }

  @Test
  void roundTrip_unicode() {
    String pw = "pàsswörd";
    assertEquals(pw, Obfuscator.clear(Obfuscator.obfuscate(pw)));
  }

  @Test
  void roundTrip_longPassword() {
    String pw = "this-is-a-very-long-password-with-many-characters-0123456789!";
    assertEquals(pw, Obfuscator.clear(Obfuscator.obfuscate(pw)));
  }

  @Test
  void roundTrip_whitespace() {
    assertEquals("pass word", Obfuscator.clear(Obfuscator.obfuscate("pass word")));
  }

  // --- clear() with known fixed value (from class javadoc) ---

  @Test
  void clear_knownObfuscatedValue() {
    // Example taken directly from the Obfuscator javadoc
    assertEquals("password", Obfuscator.clear("4zh8zUxn1Yug11VtM5ak34tkDOB"));
  }

  // --- clear() passthrough behaviour ---

  @Test
  void clear_noSeparator_returnsOriginal() {
    // No '1' character → condition x > 0 is false, original returned unchanged
    assertEquals("noseparator", Obfuscator.clear("noseparator"));
  }

  @Test
  void clear_separatorAtPositionZero_returnsOriginal() {
    // '1' at index 0 → x == 0, which is not > 0, so original is returned
    assertEquals("1atstart", Obfuscator.clear("1atstart"));
  }

  @Test
  void clear_separatorAtEnd_returnsEmptyPassword() {
    // Last '1' is the final character → sub is empty → decoded to empty string
    assertEquals("", Obfuscator.clear("anything1"));
  }

  // --- obfuscate() produces non-deterministic output ---

  @RepeatedTest(5)
  void obfuscate_twoCallsProduceDifferentResults() {
    String a = Obfuscator.obfuscate("password");
    String b = Obfuscator.obfuscate("password");
    // Random salt makes collisions astronomically unlikely; fail if they match
    assertNotEquals(a, b, "Two obfuscations of the same password should differ");
  }

  // --- obfuscated output structure ---

  @Test
  void obfuscate_resultContainsSeparatorDigit() {
    // The algorithm embeds '1' as a separator; it must be present
    String obfuscated = Obfuscator.obfuscate("password");
    assertTrue(obfuscated.contains("1"), "Obfuscated value must contain the '1' separator");
  }

  @Test
  void obfuscate_resultIsNotPlaintext() {
    String obfuscated = Obfuscator.obfuscate("password");
    assertNotEquals("password", obfuscated);
  }
}
