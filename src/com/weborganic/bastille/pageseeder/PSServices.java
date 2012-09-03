/*
 * Copyright (c) 2012 Allette Systems (Australia) Pty. Ltd.
 */
package com.weborganic.bastille.pageseeder;


/**
 * A utility class for PageSeeder Services.
 *
 * <p>Provides useful constants for Services used in this project.
 *
 * @author Christophe Lauret
 * @version 21 November 2011
 */
public final class PSServices {

  /**
   * Utility class.
   */
  private PSServices() {
  }

  /**
   * Returns the URL to invoke the group member creation service.
   *
   * @param group The name of the group
   *
   * @return <code>/groups/[groupname]/members/create</code>.
   */
  public static String toCreateGroupMember(String group) {
    return "/groups/"+group+"/members/create";
  }

  /**
   * Returns the URL to invoke the group member search service.
   *
   * @param group The name of the group
   *
   * @return <code>/groups/[groupname]/members/find</code>.
   */
  public static String toFindGroupMember(String group) {
    return "/groups/"+group+"/members/find";
  }

  /**
   * Returns the URL to invoke the group member activation service.
   *
   * @param username The user name
   *
   * @return <code>/members/[username]/activate</code>.
   */
  public static String toActivateMember(String username) {
    return "/members/"+username+"/activate";
  }

  /**
   * Returns the URL to invoke the group member details service.
   *
   * @param group    The name of the group
   * @param username The user name
   *
   * @return <code>/groups/[groupname]/members/[username]/details</code>.
   */
  public static String toGroupMemberDetails(String group, String username) {
    return "/groups/"+group+"/members/"+username+"/details";
  }

  /**
   * Returns the URL to invoke the group member edit service.
   *
   * @param group    The name of the group
   * @param username The user name
   *
   * @return <code>/groups/[groupname]/members/[username]/edit</code>.
   */
  public static String toEditGroupMember(String group, String username) {
    return "/groups/"+group+"/members/"+username+"/edit";
  }

  /**
   * Returns the URL to invoke the group member registration service.
   *
   * @param group    The name of the group
   * @param username The user name
   *
   * @return <code>/groups/[groupname]/members/[username]/registration</code>.
   */
  public static String toGroupMemberRegistration(String group, String username) {
    return "/groups/"+group+"/members/"+username+"/registration";
  }

  /**
   * Returns the URL to invoke the group member invitation service.
   *
   * @param group The name of the group to register
   *
   * @return <code>/groups/[groupname]/members/invite</code>.
   */
  public static String toGroupMemberInvite(String group) {
    return "/groups/"+group+"/members/invite";
  }

  /**
   * Returns the URL to invoke the group member registration service.
   *
   * @param username The user name
   *
   * @return <code>/members/[username]/projects</code>.
   */
  public static String toMemberProjects(String username) {
    return "/members/"+username+"/projects";
  }

  /**
   * Returns the URL to invoke the memberships service.
   *
   * @param username The user name
   *
   * @return <code>/members/[username]/memberships</code>.
   */
  public static String toMemberships(String username) {
    return "/members/"+username+"/memberships";
  }

  /**
   * Returns the URL to reset the session of the current user.
   *
   * @return <code>/resetsession</code>.
   */
  public static String toResetSession() {
    return "/resetsession";
  }

}
