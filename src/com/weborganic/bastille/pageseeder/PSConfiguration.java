/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.pageseeder;

import java.util.Properties;

import org.weborganic.berlioz.GlobalSettings;

/**
 * Configuration of the PageSeeder server that Bastille should use to connect.
 *
 * <p>Bastille can be configured to connect to a PageSeeder service using the properties are defined
 * in the <code>config-[mode].xml</code> for the key {@value BASTILLE_PAGESEEDER}.
 *
 * <p>To retrieve the PageSeeder server properties, use the {@link #getProperties()}.
 *
 * <p>The properties can contain the following keys:
 * <ul>
 *   <li><code>scheme</code>: the scheme of the PageSeeder server (for example "http")</li>
 *   <li><code>host</code>: the host of the PageSeeder server (for example "localhost")</li>
 *   <li><code>port</code>: the port of the PageSeeder server (for example "8080")</li>
 *   <li><code>siteprefix</code>: the site prefix for PageSeeder server (for example "/ps")</li>
 *   <li><code>servletprefix</code>: the servlet prefix for  the PageSeeder server (for example "/ps/servlet")</li>
 * </ul>
 *
 * <p>For example:
 * <pre>
 *   Properties pageseeder = PSConfiguration.getProperties();
 *   String host = pageseeder.getProperty("host");
 *   String port = pageseeder.getProperty("port", 8080);
 *   ...
 * </pre>
 *
 * <p>The Bastille-PageSeeder properties can also be accessed directly using the Berlioz global
 * settings.
 *
 * <p>For example: <code>GlobalSettings.get("bastille.pageseeder.host")</code>
 *
 * @author Christophe Lauret
 * @version 0.6.5 - 30 May 2011
 * @since 0.6.5
 */
public final class PSConfiguration {

  /** Utility class */
  private PSConfiguration() {
  }

  /**
   * The key for the location of the PageSeeder properties in the configuration.
   */
  public static final String BASTILLE_PAGESEEDER = "bastille.pageseeder";

  /**
   * Returns the configuration of the PageSeeder as a Properties instance.
   *
   * <p>The properties are defined in the <code>config-[mode].xml</code> using the key
   * {@value BASTILLE_PAGESEEDER}.
   *
   * @return the properties of the PageSeeder server to connect to.
   */
  public static Properties getProperties() {
    return GlobalSettings.getNode(BASTILLE_PAGESEEDER);
  }

}
