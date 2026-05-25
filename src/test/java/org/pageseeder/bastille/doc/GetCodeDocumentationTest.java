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
package org.pageseeder.bastille.doc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.content.Environment;
import org.pageseeder.berlioz.content.Location;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GetCodeDocumentationTest {

  @TempDir
  Path tempDir;

  private GetCodeDocumentation generator;
  private XMLStringWriter xml;
  private StubRequest request;

  @BeforeEach
  void setUp() {
    generator = new GetCodeDocumentation();
    xml = new XMLStringWriter(XML.NamespaceAware.No);
    request = new StubRequest(tempDir.toFile());
  }

  @Test
  void getETag_returnsNull() {
    assertNull(generator.getETag(request));
  }

  @Test
  void process_noPathParameter_writesClientError() throws IOException {
    generator.process(request, xml);
    String output = xml.toString();
    assertTrue(output.contains("<error"), "Should write an error element");
    assertTrue(output.contains("type=\"client\""), "Error type should be 'client'");
    assertTrue(output.contains("path"), "Error message should mention the 'path' parameter");
    assertEquals(ContentStatus.BAD_REQUEST, request.lastStatus);
  }

  @Test
  void process_unsupportedFileType_writesClientError() throws IOException {
    Path txtFile = Files.createFile(tempDir.resolve("style.txt"));
    request.setParameter("path", txtFile.getFileName().toString());

    generator.process(request, xml);

    String output = xml.toString();
    assertTrue(output.contains("<error"), "Should write an error element for unsupported file type");
    assertTrue(output.contains("type=\"client\""), "Error type should be 'client'");
    assertEquals(ContentStatus.BAD_REQUEST, request.lastStatus);
  }

  @Test
  void process_xslFileDoesNotExist_writesClientError() throws IOException {
    request.setParameter("path", "nonexistent.xsl");

    generator.process(request, xml);

    String output = xml.toString();
    assertTrue(output.contains("<error"), "Should write an error element for missing file");
    assertTrue(output.contains("type=\"client\""), "Error type should be 'client'");
    assertEquals(ContentStatus.BAD_REQUEST, request.lastStatus);
  }

  // --- Stubs ---

  static class StubRequest implements ContentRequest {
    private final Map<String, String> params = new HashMap<>();
    private final File privateRoot;
    ContentStatus lastStatus;

    StubRequest(File privateRoot) {
      this.privateRoot = privateRoot;
    }

    void setParameter(String name, String value) {
      params.put(name, value);
    }

    @Override public String getBerliozPath() { return "/test"; }
    @Override public String getParameter(String name) { return params.get(name); }
    @Override public String getParameter(String name, String def) { return params.getOrDefault(name, def); }
    @Override public int getIntParameter(String name, int def) { return def; }
    @Override public long getLongParameter(String name, long def) { return def; }
    @Override public String[] getParameterValues(String name) { return null; }
    @Override public Enumeration<String> getParameterNames() { return Collections.emptyEnumeration(); }
    @Override public Object getAttribute(String name) { return null; }
    @Override public void setAttribute(String name, Object value) { /* no-op stub */ }
    @Override public Date getDateParameter(String name) { return null; }
    @Override public Cookie[] getCookies() { return new Cookie[0]; }
    @Override public HttpSession getSession() { return null; }
    @Override public Environment getEnvironment() { return new StubEnvironment(privateRoot); }
    @Override public Location getLocation() { return null; }
    @Override public void setStatus(ContentStatus status) { this.lastStatus = status; }
    @Override public void setRedirect(String url, ContentStatus status) { /* no-op stub */ }
  }

  static class StubEnvironment implements Environment {
    private final File root;

    StubEnvironment(File root) {
      this.root = root;
    }

    @Override public File getPublicFolder() { return root; }
    @Override public File getPrivateFolder() { return getPublicFolder(); }
    @Override public File getPublicFile(String path) { return new File(root, path); }
    @Override public File getPrivateFile(String path) { return new File(root, path); }
    @Override public String getProperty(String name) { return null; }
    @Override public String getProperty(String name, String def) { return def; }
    @Override public int getProperty(String name, int def) { return def; }
    @Override public boolean getProperty(String name, boolean def) { return def; }
  }
}
