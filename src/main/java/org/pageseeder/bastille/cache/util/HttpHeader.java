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
package org.pageseeder.bastille.cache.util;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic implementation of an HTTP header.
 *
 * <p>Handles String, Int and Date typed headers.
 *
 * @author Eric Dalquist
 * @author Christophe Lauret
 *
 * @version 27 January 2013
 *
 * @param <T> The type of Header value being stored. Must implement {@link Serializable}
 */
public final class HttpHeader<T extends Serializable> implements Serializable {

  /** As per requirement for <code>Serializable</code> */
  private static final long serialVersionUID = -4637482646600102192L;

  /**
   * Used to help differentiate the different header types.
   */
  public enum Type {

    /** A String Header. {@link javax.servlet.http.HttpServletResponse#setHeader(String, String)} */
    STRING(String.class),

    /** A date Header. {@link javax.servlet.http.HttpServletResponse#setDateHeader(String, long)} */
    DATE(Long.class),

    /** An int Header. {@link javax.servlet.http.HttpServletResponse#setIntHeader(String, int)} */
    INT(Integer.class);

    /**
     * Used to lookup types by class.
     */
    private static final Map<Class<? extends Serializable>, Type> TYPE_LOOKUP =
        new ConcurrentHashMap<>();

    /**
     * The underlying class for the specified type.
     */
    private final Class<? extends Serializable> _type;

    /**
     * Create a new Type
     *
     * @param type The class used for this type.
     */
    Type(Class<? extends Serializable> type) {
      this._type = type;
    }

    /**
     * @return The header type class this Type represents
     */
    public Class<? extends Serializable> getTypeClass() {
      return this._type;
    }

    /**
     * Determines the {@link Type} of the Header.
     *
     * @param clazz The class to test.
     *
     * @return The corresponding type (never <code>null</code>).
     *
     * @throws IllegalArgumentException if the specified class does not match any of the Types
     */
    public static Type determineType(Class<? extends Serializable> clazz) {
      Type lookupType = TYPE_LOOKUP.get(clazz);
      if (lookupType != null) return lookupType;

      for (final Type t : Type.values()) {
        if (clazz == t.getTypeClass()) {
          //If the class explicitly matches add to the lookup cache
          TYPE_LOOKUP.put(clazz, t);
          return t;
        }

        if (clazz.isAssignableFrom(t.getTypeClass())) return t;
      }

      throw new IllegalArgumentException("No Type for class " + clazz);
    }
  }

  /**
   * The name of the header.
   */
  private final String _name;

  /**
   * The value.
   */
  private final T _value;

  /**
   * The type of value for this header.
   */
  private final Type _type;

  /**
   * Create a new Header.
   *
   * @param name  Name of the header, may not be <code>null</code>
   * @param value Value of the header, may not be <code>null</code>
   *
   * @throws NullPointerException if either parameter is <code>null</code>.
   */
  public HttpHeader(String name, T value) {
    if (name == null) throw new NullPointerException("Header cannnot have a null name");
    if (value == null) throw new NullPointerException("Header cannnot have a null value");
    this._name = name;
    this._value = value;
    this._type = Type.determineType(value.getClass());
  }

  /**
   * @return Name of the header; never <code>null</code>
   */
  public String name() {
    return this._name;
  }

  /**
   * @return Value for the header; never <code>null</code>
   */
  public T value() {
     return this._value;
  }

  /**
   * @return The header type
   */
  public Type type() {
     return this._type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this._name == null) ? 0 : this._name.hashCode());
    result = prime * result + ((this._type == null) ? 0 : this._type.hashCode());
    result = prime * result + ((this._value == null) ? 0 : this._value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    HttpHeader<?> other = (HttpHeader<?>) o;
    if (this._name == null) {
      if (other._name != null) return false;
    } else if (!this._name.equals(other._name)) return false;
    if (this._type == null) {
      if (other._type != null) return false;
    } else if (!this._type.equals(other._type)) return false;
    if (this._value == null) {
      if (other._value != null) return false;
    } else if (!this._value.equals(other._value)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Header<" + this._type.getTypeClass().getSimpleName() + "> [name=" + this._name + ", value=" + this._value + "]";
  }

}
