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
   * Creates a new PageSeeder User
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

  
  public String getUsername() {
    return _username;
  }

  public String getFirstname() {
    return _firstname;
  }
  
  public String getJSessionId() {
    return _jsessionid;
  }
  public String getSurname() {
    return _surname;
  }
  public String getEmail() {
    return _email;
  }

  // Setters ======================================================================================

  public void setUsername(String _username) {
    this._username = _username;
  }

  public void setJSessionId(String _jsessionid) {
    this._jsessionid = _jsessionid;
  }
  public void setFirstname(String _firstname) {
    this._firstname = _firstname;
  }
  public void setSurname(String _surname) {
    this._surname = _surname;
  }
  public void setEmail(String _email) {
    this._email = _email;
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
