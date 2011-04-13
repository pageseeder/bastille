package com.weborganic.bastille.security.ps;

import java.io.IOException;
import java.io.Serializable;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.security.User;

/**
 * Represents a PageSeeder User.
 * 
 * @author Christophe Lauret
 * @version 7 April 2011
 */
public final class PageSeederUser implements User, Serializable {

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
   * The Member's jsession id.
   */
  private String _jsessionid = null;

  /**
   * Creates a new PageSeeder User.
   * @param id the ID of the user in PageSeeder.
   */
  public PageSeederUser(Long id) {
    this._id = id;
  }

  /**
   * @return The PageSeeder Member ID of this user.
   */
  public Long id() {
    return this._id;
  }

  // Getters ======================================================================================

  /**
   * @return the PageSeeder username for this user.
   */
  public String getUsername() {
    return _username;
  }

  /**
   * @return the ID of this user session in PageSeeder (changes after each login) 
   */
  public String getJSessionId() {
    return _jsessionid;
  }

  /**
   * @return the PageSeeder first name for this user.
   */
  public String getFirstname() {
    return _firstname;
  }

  /**
   * @return the PageSeeder surname for this user.
   */
  public String getSurname() {
    return _surname;
  }

  /**
   * @return the PageSeeder email for this user.
   */
  public String getEmail() {
    return _email;
  }

  /**
   * @return same as username.
   */
  public String getName() {
    return this._username;
  }

  // Setters ======================================================================================

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
    this._jsessionid = jsessionid;
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

  // XML Writer ===================================================================================

  /**
   * A PageSeeder User as XML.
   * 
   * <pre>{@code
   *  <user type="pageseeder">
   *    <id>[member_id]</id>
   *    <username>[member_username]</username>
   *    <firstname>[member_firstname]</firstname>
   *    <surname>[member_surname]</surname>
   *    <email>[member_email]</email>
   *  </user>
   * }</pre>
   * 
   * {@inheritDoc}
   */
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("user");
    xml.attribute("type", "pageseeder");
    xml.element("id", this._id.toString());
    if (this._username != null) xml.element("username", this._username);
    if (this._firstname != null) xml.element("firstname", this._firstname);
    if (this._surname != null) xml.element("surname", this._surname);
    if (this._email != null) xml.element("email", this._email);
    xml.closeElement();
  }

}
