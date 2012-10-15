/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.pageseeder;

import org.weborganic.berlioz.Beta;

/**
 * Type for resource requested on PageSeeder used to simplify the creation of URLs.
 *
 * @author Christophe Lauret
 * @version 0.6.2 - 11 April 2011
 * @since 0.6.2
 */
@Beta public enum PSResourceType {

  /**
   * A PageSeeder Servlet.
   */
  SERVLET,

  /**
   * A PageSeeder Service.
   */
  SERVICE,

  /**
   * Any resource on PageSeeder.
   */
  RESOURCE

}
