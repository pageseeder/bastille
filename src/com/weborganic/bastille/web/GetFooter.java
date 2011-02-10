package com.weborganic.bastille.web;

import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;

/**
 * This generator returns the XML for the Footer.
 * 
 * <p>It extends the {@link GetTemplateFile} class as below:
 * <pre>
 * public GetNavigation() {
 *   super("footer");
 * }
 * </pre>
 * 
 * @author Christophe Lauret
 * @version 31 May 2010
 */
public final class GetFooter extends GetTemplateFile implements ContentGenerator, Cacheable {

  /**
   * Creates a new footer template file generator.
   */
  public GetFooter() {
    super("footer");
  }
}
