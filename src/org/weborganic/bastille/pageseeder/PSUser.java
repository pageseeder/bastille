/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.pageseeder;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.weborganic.bastille.security.User;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Represents a PageSeeder User.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.1 - 19 December 2012
 * @since 0.8.1
 */
public class PSUser implements User, Serializable {

  /**
   * As per requirement for the {@link Serializable} interface.
   */
  private static final long serialVersionUID = 146742323984333167L;

  /**
   * The PagerSeeder Member ID.
   */
  private final Long _id;

  /**
   * The Member's email.
   */
  private String _email = null;

  /**
   * The Member's first name.
   */
  private String _firstname = null;

  /**
   * The Member's surname.
   */
  private String _surname = null;

  /**
   * The Member's username.
   */
  private String _username = null;

  /**
   * The Member's PageSeeder session.
   */
  private PSSession _session = null;

  /**
   * The groups the user is a member of.
   */
  private String[] _memberOf = null;

  /**
   * Creates a new PageSeeder User.
   * @param id the ID of the user in PageSeeder.
   */
  public PSUser(Long id) {
    this._id = id;
  }

  /**
   * @return The PageSeeder Member ID of this user.
   */
  public final Long id() {
    return this._id;
  }

  // Getters
  // ----------------------------------------------------------------------------------------------

  /**
   * @return the PageSeeder username for this user.
   */
  public final String getUsername() {
    return this._username;
  }

  /**
   * @return the ID of this user session in PageSeeder (changes after each login)
   */
  public final String getJSessionId() {
    return this._session.getJSessionId();
  }

  /**
   * Return the PageSeeder session for this user.
   *
   * @return the last connected time stamp.
   */
  public final PSSession getSession() {
    return this._session;
  }

  /**
   * @return the PageSeeder first name for this user.
   */
  public final String getFirstname() {
    return this._firstname;
  }

  /**
   * @return the PageSeeder surname for this user.
   */
  public final String getSurname() {
    return this._surname;
  }

  /**
   * @return the PageSeeder email for this user.
   */
  public final String getEmail() {
    return this._email;
  }

  /**
   * @return same as username.
   */
  @Override
  public final String getName() {
    return this._username;
  }

  /**
   * @return the groups the user is a member of.
   */
  public final List<String> memberOf() {
    return Arrays.asList(this._memberOf);
  }

  /**
   * Indicates whether the user is a member of the specified group.
   *
   * @param group The group to check membership of.
   * @return the group the user is a member of.
   */
  public final boolean isMemberOf(String group) {
    if (this._memberOf == null) return false;
    for (String g : this._memberOf) {
      if (g.equals(group)) return true;
    }
    return false;
  }

  // Setters
  // ----------------------------------------------------------------------------------------------

  /**
   * @param username the PageSeeder username for this user.
   */
  protected void setUsername(String username) {
    this._username = username;
  }

  /**
   * @param jsessionid the PageSeeder username for this user.
   */
  protected void setJSessionId(String jsessionid) {
    this._session = new PSSession(jsessionid);
  }

  /**
   * @param firstname the PageSeeder username for this user.
   */
  protected void setFirstname(String firstname) {
    this._firstname = firstname;
  }

  /**
   * @param surname the PageSeeder username for this user.
   */
  protected void setSurname(String surname) {
    this._surname = surname;
  }

  /**
   * @param email the PageSeeder username for this user.
   */
  protected void setEmail(String email) {
    this._email = email;
  }

  /**
   * @param groups the PageSeeder groups the user is a member of.
   */
  protected void setMemberOf(List<String> groups) {
    this._memberOf = groups.toArray(new String[]{});
  }

  // Setters
  // ----------------------------------------------------------------------------------------------

  /**
   * A PageSeeder User as XML.
   *
   * <p>Note: The password is never included.
   *
   * <pre>{@code
   *  <user type="pageseeder">
   *    <id>[member_id]</id>
   *    <username>[member_username]</username>
   *    <firstname>[member_firstname]</firstname>
   *    <surname>[member_surname]</surname>
   *    <email>[member_email]</email>
   *    <member-of groups="[group0],[group1]"/>
   *  </user>
   * }</pre>
   *
   * {@inheritDoc}
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("user");
    xml.attribute("type", "pageseeder");
    xml.element("id", this._id.toString());
    if (this._username != null) xml.element("username", this._username);
    if (this._firstname != null) xml.element("firstname", this._firstname);
    if (this._surname != null) xml.element("surname", this._surname);
    if (this._email != null) xml.element("email", this._email);
    if (this._memberOf != null) {
      String[] memberOf = this._memberOf;
      xml.openElement("member-of");
      StringBuilder groups = new StringBuilder();
      for (int i = 0; i < memberOf.length; i++) {
        if (i > 0) groups.append(',');
        groups.append(memberOf[i]);
      }
      xml.attribute("groups", groups.toString());
      xml.closeElement();
    }
    xml.closeElement();
  }

}
