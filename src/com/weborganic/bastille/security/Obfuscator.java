package com.weborganic.bastille.security;

import java.nio.charset.Charset;

import com.weborganic.bastille.util.Base32;

/**
 * A utility class to obfuscate passwords the config.
 * 
 * @author Christophe Lauret
 * @version 23 September 2011
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
   * Obfuscate the given password.
   * 
   * @param obfuscated the obfuscated password
   * @return The password in clear.
   */
  public static String clear(String obfuscated) {
    int x = obfuscated.lastIndexOf('1');
    if (x > 0) {
      String sub = obfuscated.substring(x+1);
      String reverse = reverse(sub);
      byte[] bytes = Base32.decode(reverse);
      return new String(bytes, UTF8);
    } else {
      return obfuscated;
    }
  }

  /**
   * Obfuscate the given password.
   * 
   * @param clear the password in clear
   * @return The obfuscated password
   */
  public static String obfuscate(String clear) {
    String base32 = Base32.encode(clear.getBytes(UTF8));
    StringBuilder obfuscated = new StringBuilder();
    obfuscated.append(base32);
    obfuscated.append('1');
    long r = Math.round(Math.random() * Long.MAX_VALUE);
    final int maxbase = 36;
    obfuscated.append(Long.toString(r, maxbase));
    return toMixCase(reverse(obfuscated.toString()));
  }

  /**
   * To obfuscate passwords.
   * 
   * <p>
   * 
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
/*    if (args.length < 1) {
      System.err.println("Usage: Obfuscator [password]");
      return; 
    } */
    String password = "test"; //args[0];
    String obfuscated = obfuscate(password);
    System.out.println(password+" -> OBF:"+obfuscate(password)+" -> "+clear(obfuscated));
  }

  /**
   * 
   */
  private final static String reverse(String word) {
    StringBuilder reverse = new StringBuilder();
    for (int i = word.length() - 1; i >= 0; i--) {
      reverse.append(word.charAt(i));
    }
    return reverse.toString();
  }

  /**
   * 
   */
  private final static String toMixCase(String word) {
    StringBuilder mixed = new StringBuilder();
    for (int i = 0; i < word.length(); i++) {
      boolean up = Math.random() > 0.5;
      mixed.append(up? Character.toUpperCase(word.charAt(i)) : Character.toLowerCase(word.charAt(i)));
    }
    return mixed.toString();
  }

}
