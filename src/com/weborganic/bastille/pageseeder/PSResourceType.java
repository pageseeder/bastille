package com.weborganic.bastille.pageseeder;

import org.weborganic.berlioz.Beta;

/**
 * 
 * @author Christophe Lauret 
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
   * A UI page (full page only).
   */
  PAGE,

  /**
   * A UI block.
   */
  BLOCK,

  /**
   * Another Resource.
   */
  RESOURCE

}
