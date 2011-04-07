package com.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.security.ps.PageSeederAuthenticator;

/**
 * A servlet to login.
 * 
 * @author Christophe Lauret
 * @version 7 April 2011
 */
public class LoginServlet extends HttpServlet {

  ServletContext context = null;
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.context = config.getServletContext();
  }


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    login(req, res);
  }

  public void login(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    String username = req.getParameter("username");
    String password = req.getParameter("password");

    // Get the authenticator
    PageSeederAuthenticator authenticator = new PageSeederAuthenticator();
    HttpSession session = req.getSession();

    // Grab the request
    HttpServletRequest saved = (HttpServletRequest)session.getAttribute(Constants.SESSION_REQUEST_ATTRIBUTE);
    
    // Already logged in?
    User user = (User)session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
    if (user != null) {
      authenticator.logout(user);
      session.invalidate();
      session = req.getSession();
    }

    // Perform login
    user = authenticator.login(username, password);
    session.setAttribute(Constants.SESSION_USER_ATTRIBUTE, user);

    // Forward the original request
    if (saved != null) {
      RequestDispatcher dispatcher = this.context.getRequestDispatcher(saved.getRequestURI());
      dispatcher.forward(saved, res);
    } else {
      RequestDispatcher dispatcher = this.context.getRequestDispatcher("/");
      dispatcher.forward(req, res);
    }

  }

}
