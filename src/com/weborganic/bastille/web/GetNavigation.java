package com.weborganic.bastille.web;

import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;

/**
 * This generator returns the XML for the Navigation.
 * 
 * <p>It extends the {@link GetTemplateFile} class as below:
 * <pre>
 * public GetNavigation() {
 *   super("navigation");
 * }
 * </pre>
 * 
 * @author Christophe Lauret
 * @version 31 May 2010
 */
public final class GetNavigation  extends GetTemplateFile implements ContentGenerator, Cacheable {

  /**
   * Creates a new navigation template file generator.
   */
  public GetNavigation() {
    super("navigation");
  }
}
