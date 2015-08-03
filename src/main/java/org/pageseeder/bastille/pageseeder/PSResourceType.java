/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.pageseeder;

import org.pageseeder.berlioz.Beta;

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
