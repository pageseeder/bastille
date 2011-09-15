/*
 *  Copyright (c) 2011 Allette Systems pty. ltd.
 */
package com.weborganic.bastille.web.general.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.servlet.HttpRequestWrapper;

/**
 * The HttpSevlet Helper Class.
 * 
 * 
 * @author Ciber Cai
 * @version 10 August 2011
 */
public class HttpServletUtils {

  /**
   * 
   * @param req
   * @return
   */
  public static HttpServletResponse getHttpResponse(ContentRequest req){
    if (req instanceof HttpRequestWrapper) {
      HttpServletResponse hres = ((HttpRequestWrapper)req).getHttpResponse();
      return hres;
    } else {
      return null;
    }
  }

  /***
   * 
   * @param req
   * @return
   */
  public static HttpServletRequest getHttpRequest(ContentRequest req){
    if (req instanceof HttpRequestWrapper) {
      HttpServletRequest hreq = ((HttpRequestWrapper)req).getHttpRequest();
      return hreq;
    } else {
      return null;
    }
  }

  /***
   * 
   * @param req
   * @return
   */
  public static HttpSession getSession(ContentRequest req){
    return getHttpRequest(req).getSession();
  }

  /***
   * 
   * @param req
   * @param create
   * @return
   */
  public static HttpSession getSession(ContentRequest req, boolean create){
    return getHttpRequest(req).getSession(create);
  }

  /***
   * 
   * @param req
   * @return
   */
  public static String getMethod(ContentRequest req){
    return getHttpRequest(req).getMethod();
  }


}
