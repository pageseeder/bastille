package com.weborganic.bastille.pageseeder;

import org.weborganic.berlioz.Beta;

/**
 * Type for resource requested on PageSeeder used to simplify the creation of URLs.
 * 
 * @author Christophe Lauret
 * @version 11 April 2011
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
   * Another Resource.
   */
  RESOURCE

}
