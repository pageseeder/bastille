/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.security;

import java.nio.charset.Charset;

import com.weborganic.bastille.util.Base32;

/**
 * A utility class to obfuscate passwords.
 * 
 * <p>Use this class to avoid leaving the password in clear in the configuration.
 * 
 * <p>Note that this class is not meant to provide any strong cryptography, but is merely there to
 * make passwords harder to guess.
 * 
 * @author Christophe Lauret
 * @version 26 September 2011
 */
public final class Obfuscator {

  /** We use UTF-8. */
  private static final Charset UTF8 = Charset.forName("utf-8");

  /**
   * Utility class.
   */
  private Obfuscator() {
  }

  /**
   * Return the given password to its clear form.
   * 
   * <p><i>Implementation note:</i> this method performs the following transformation: 
   * <pre>
   *   1. "4zh8zUxn1Yug11VtM5ak34tkDOB" (Obfuscated password)
   *   2. "VtM5ak34tkDOB"               (Grab characters after last '1')
   *   3. "BODkt43ka5MtV"               (Reverse characters)
   *   4. "OBQxg43xn5ZgI"               (Perform ROT13 transformation)
   *   5. "password"                    (Decode in Base32 as UTF-8 bytes)
   * </pre>
   * 
   * @param obfuscated the obfuscated password
   * @return The password in clear.
   */
  public static String clear(String obfuscated) {
    int x = obfuscated.lastIndexOf('1');
    if (x > 0) {
      String sub = obfuscated.substring(x+1);
      CharSequence reverse = reverse(sub);
      byte[] bytes = Base32.decode(rot13(reverse).toString());
      return new String(bytes, UTF8);
    } else {
      return obfuscated;
    }
  }

  /**
   * Obfuscate the given password.
   * 
   * <p><i>Implementation note:</i> this method performs the following transformation: 
   * <pre>
   *   1. "password"                    (Original, password in clear)
   *   2. "OBQXG43XN5ZGI"               (Encode in Base32 from UTF-8 bytes)
   *   3. "BODKT43KA5MTV"               (Perform ROT13 transformation)
   *   4. "BODKT43KA5MTV1"              (Append '1' which does not occur in Base32)
   *   5. "BODKT43KA5MTV11GUY1NXUZ8HZ4" (Append random characters)
   *   6. "4ZH8ZUXN1YUG11VTM5AK34TKDOB" (Reverse character sequence)
   *   7. "4zh8zUxn1Yug11VtM5ak34tkDOB" (Convert to Mixed case)
   * </pre>
   * 
   * @param clear the password in clear
   * @return The obfuscated password
   */
  public static String obfuscate(String clear) {
    StringBuilder obfuscated = new StringBuilder();
    String base32 = Base32.encode(clear.toString().getBytes(UTF8));
    obfuscated.append(rot13(base32));
    obfuscated.append('1');
    long r = Math.round(Math.random() * Long.MAX_VALUE);
    final int maxbase = 36;
    obfuscated.append(Long.toString(r, maxbase));
    System.err.println(obfuscated.toString());
    return toMixCase(reverse(obfuscated)).toString();
  }

  /**
   * To obfuscate passwords.
   * 
   * <p>Use this class as:
   * <pre>
   *   java -cp bastille.jar com.weborganic.bastille.security.Obfuscator [password]
   * </pre>
   * 
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: Obfuscator [password]");
      return;
    }
    String password = args[0];
    String obfuscated = obfuscate(password);
    System.out.println(password+" -> OB1:"+obfuscated+" -> "+clear(obfuscated)+" (check)");
  }

  /**
   * Reverse the characters in the specified character sequence.
   * 
   * @param word The word to process.
   * @return The sequence in reverse order.
   */
  private static CharSequence reverse(CharSequence word) {
    StringBuilder reverse = new StringBuilder();
    for (int i = word.length() - 1; i >= 0; i--) {
      reverse.append(word.charAt(i));
    }
    return reverse;
  }

  /**
   * Randomly turn characters in the specified sequence to upper or lower case.
   * 
   * @param word The word to process.
   * @return The same sequence in mixed case.
   */
  private static CharSequence toMixCase(CharSequence word) {
    StringBuilder mixed = new StringBuilder();
    final int length = word.length(); 
    final double fifty_percent = 0.5; 
    for (int i = 0; i < length; i++) {
      boolean up = Math.random() > fifty_percent;
      mixed.append(up? Character.toUpperCase(word.charAt(i)) : Character.toLowerCase(word.charAt(i)));
    }
    return mixed;
  }

  /**
   * Performs a ROT13 transformation of the specified string (handles mixed case)
   * 
   * @param word The word to process.
   * @return The sequence in mixed case.
   */
  private static CharSequence rot13(CharSequence word) {
    StringBuilder rot13 = new StringBuilder();
    final int length = word.length();
    final int alphabetLength = 26;
    for (int i = 0; i < length; i++) {
      int c = word.charAt(i);
      if (c >= 'A' && c <= 'Z') {
        rot13.append((char)((c - 'A' + 13) % alphabetLength + 'A'));
      } else if (c >= 'a' && c <= 'z') {
        rot13.append((char)((c - 'a' + 13) % alphabetLength + 'a'));
      } else {
        rot13.append((char)c);
      }
    }
    return rot13;
  }

}
