/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.security;

import com.topologi.diffx.xml.XMLWritable;

/**
 * An interface to represent a user.
 * 
 * <p>To ease interoperability, the XML returned should match:
 * <pre>{@code <user type="[type]"> ... </user>}</pre>
 *
 * <p>All <code>User</code> implementations must be {@link java.io.Serializable}.
 * 
 * @author Christophe Lauret
 * @version 0.6.2
 * @since 0.6.2
 */
public interface User extends XMLWritable {

  /**
   * @return the name of the user.
   */
  String getName();

}
