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

class ListCodeFilesTest {

  @TempDir
  Path tempDir;

  private ListCodeFiles generator;
  private XMLStringWriter xml;
  private StubRequest request;

  @BeforeEach
  void setUp() {
    generator = new ListCodeFiles();
    xml = new XMLStringWriter(false);
    request = new StubRequest(tempDir.toFile());
  }

  @Test
  void getETag_returnsNull() {
    assertNull(generator.getETag(request));
  }

  @Test
  void process_noXsltFolder_writesNotFoundStatus() throws IOException {
    // No xslt subdirectory created — it doesn't exist
    generator.process(request, xml);
    String output = xml.toString();
    assertTrue(output.contains("status=\"not-found\""), "Should report xslt folder as not found");
  }

  @Test
  void process_emptyXsltFolder_writesFolderElement() throws IOException {
    Files.createDirectory(tempDir.resolve("xslt"));

    generator.process(request, xml);

    String output = xml.toString();
    assertTrue(output.contains("type=\"folder\""), "Should report xslt as a folder");
    assertFalse(output.contains("type=\"file\""), "Empty folder should contain no file elements");
  }

  @Test
  void process_xsltFolderWithXslFile_writesFileElement() throws IOException {
    Path xsltDir = Files.createDirectory(tempDir.resolve("xslt"));
    Files.createFile(xsltDir.resolve("template.xsl"));

    generator.process(request, xml);

    String output = xml.toString();
    assertTrue(output.contains("type=\"folder\""), "xslt dir should be reported as folder");
    assertTrue(output.contains("type=\"file\""), "XSL file should be reported as a file");
    assertTrue(output.contains("name=\"template.xsl\""), "File name should appear in output");
  }

  @Test
  void process_nonXslFilesNotListed() throws IOException {
    Path xsltDir = Files.createDirectory(tempDir.resolve("xslt"));
    Files.createFile(xsltDir.resolve("readme.txt"));
    Files.createFile(xsltDir.resolve("config.xml"));

    generator.process(request, xml);

    String output = xml.toString();
    assertFalse(output.contains("readme.txt"), "Non-XSL files should be filtered out");
    assertFalse(output.contains("config.xml"), "Non-XSL files should be filtered out");
  }

  @Test
  void process_subdirectoryIsRecursed() throws IOException {
    Path xsltDir = Files.createDirectory(tempDir.resolve("xslt"));
    Path subDir = Files.createDirectory(xsltDir.resolve("sub"));
    Files.createFile(subDir.resolve("nested.xsl"));

    generator.process(request, xml);

    String output = xml.toString();
    assertTrue(output.contains("name=\"sub\""), "Subdirectory should be listed");
    assertTrue(output.contains("name=\"nested.xsl\""), "Nested XSL file should appear");
  }

  @Test
  void process_mixedContent_onlyXslAndDirectories() throws IOException {
    Path xsltDir = Files.createDirectory(tempDir.resolve("xslt"));
    Files.createFile(xsltDir.resolve("kept.xsl"));
    Files.createFile(xsltDir.resolve("ignored.txt"));
    Files.createDirectory(xsltDir.resolve("subdir"));

    generator.process(request, xml);

    String output = xml.toString();
    assertTrue(output.contains("name=\"kept.xsl\""), "XSL file should be listed");
    assertTrue(output.contains("name=\"subdir\""), "Subdirectory should be listed");
    assertFalse(output.contains("ignored.txt"), "TXT file should be excluded");
  }

  // --- Stubs ---

  static class StubRequest implements ContentRequest {
    private final Map<String, String> params = new HashMap<>();
    private final File privateRoot;
    ContentStatus lastStatus;

    StubRequest(File privateRoot) {
      this.privateRoot = privateRoot;
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
