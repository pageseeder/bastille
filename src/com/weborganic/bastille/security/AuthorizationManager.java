package com.weborganic.bastille.security;


public interface AuthorizationManager {

  
  public boolean isUserAuthorized(User user, String uri);

}
