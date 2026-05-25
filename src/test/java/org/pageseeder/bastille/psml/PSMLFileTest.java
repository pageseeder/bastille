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
package org.pageseeder.bastille.psml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PSMLFileTest {

  @TempDir
  Path tempDir;

  // --- path() ---

  @Test
  void path_returnsConstructorValue() {
    PSMLFile psml = new PSMLFile("content/foo/bar.psml", null);
    assertEquals("content/foo/bar.psml", psml.path());
  }

  // --- file() ---

  @Test
  void file_returnsConstructorValue() {
    File f = new File("/some/path/bar.psml");
    PSMLFile psml = new PSMLFile("content/bar.psml", f);
    assertEquals(f, psml.file());
  }

  @Test
  void file_nullAllowed() {
    PSMLFile psml = new PSMLFile("content/bar.psml", null);
    assertNull(psml.file());
  }

  // --- exists() ---

  @Test
  void exists_nullFile_returnsFalse() {
    PSMLFile psml = new PSMLFile("content/missing.psml", null);
    assertFalse(psml.exists());
  }

  @Test
  void exists_nonexistentFile_returnsFalse() {
    File absent = new File(tempDir.toFile(), "absent.psml");
    PSMLFile psml = new PSMLFile("content/absent.psml", absent);
    assertFalse(psml.exists());
  }

  @Test
  void exists_presentFile_returnsTrue() throws IOException {
    File f = new File(tempDir.toFile(), "present.psml");
    assertTrue(f.createNewFile());
    PSMLFile psml = new PSMLFile("content/present.psml", f);
    assertTrue(psml.exists());
  }

  // --- getBase() ---

  @Test
  void getBase_fileInSubfolder() {
    File f = new File("/root/content/foo/bar.psml");
    PSMLFile psml = new PSMLFile("content/foo/bar.psml", f);
    assertEquals("/content/foo/", psml.getBase());
  }

  @Test
  void getBase_fileAtRoot() {
    File f = new File("/root/bar.psml");
    PSMLFile psml = new PSMLFile("bar.psml", f);
    assertEquals("/", psml.getBase());
  }

  @Test
  void getBase_deeplyNestedFile() {
    File f = new File("/root/a/b/c/doc.psml");
    PSMLFile psml = new PSMLFile("a/b/c/doc.psml", f);
    assertEquals("/a/b/c/", psml.getBase());
  }

  // --- toString() ---

  @Test
  void toString_existingFile_containsOK() throws IOException {
    File f = new File(tempDir.toFile(), "doc.psml");
    assertTrue(f.createNewFile());
    PSMLFile psml = new PSMLFile("content/doc.psml", f);
    assertTrue(psml.toString().contains("OK"));
  }

  @Test
  void toString_missingFile_containsNotFound() {
    File absent = new File(tempDir.toFile(), "nope.psml");
    PSMLFile psml = new PSMLFile("content/nope.psml", absent);
    assertTrue(psml.toString().contains("NOT_FOUND"));
  }
}
