/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.security;

import java.io.IOException;

import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A generator to obfuscate or see passwords in clear.
 *
 * @author Christophe Lauret
 * @version Bastille 0.6.7
 */
public final class ObfuscatorGenerator implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {
    String password = req.getParameter("password", "");
    if (password.length() > 0) {

      String clear = password;
      String obscur = password;

      if (password.startsWith("OB1:")) {
        clear = Obfuscator.clear(password);
      } else {
        obscur = Obfuscator.obfuscate(password);
      }

      xml.openElement("password", false);
      xml.attribute("clear", clear);
      xml.attribute("obscur", "OB1:"+obscur);
      xml.closeElement();
    } else {
      xml.emptyElement("no-password");
    }

  }

}
