package com.weborganic.bastille.web;

import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;

/**
 * This generator returns the XML for the Header.
 * 
 * <p>It extends the {@link GetTemplateFile} class as below:
 * <pre>
 * public GetNavigation() {
 *   super("header");
 * }
 * </pre>
 * 
 * @author Christophe Lauret
 * @version 25 May 2010
 */
public final class GetHeader extends GetTemplateFile implements ContentGenerator, Cacheable {

  /**
   * Creates a new header template file generator.
   */
  public GetHeader() {
    super("header");
  }
}
