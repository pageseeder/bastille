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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PSMLOverviewsTest {

  @TempDir
  Path tempDir;

  // --- getContents() ---

  @Test
  void getContents_nonexistentDirectory_returnsEmptyList() {
    File absent = new File(tempDir.toFile(), "nonexistent");
    List<File> result = PSMLOverviews.getContents(absent);
    assertTrue(result.isEmpty());
  }

  @Test
  void getContents_emptyDirectory_returnsEmptyList() {
    List<File> result = PSMLOverviews.getContents(tempDir.toFile());
    assertTrue(result.isEmpty());
  }

  @Test
  void getContents_directoryWithPsmlFiles_returnsThem() throws IOException {
    new File(tempDir.toFile(), "a.psml").createNewFile();
    new File(tempDir.toFile(), "b.psml").createNewFile();

    List<File> result = PSMLOverviews.getContents(tempDir.toFile());
    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(f -> f.getName().endsWith(".psml")));
  }

  @Test
  void getContents_directoryWithMixedFiles_returnsOnlyPsml() throws IOException {
    new File(tempDir.toFile(), "doc.psml").createNewFile();
    new File(tempDir.toFile(), "doc.xml").createNewFile();
    new File(tempDir.toFile(), "notes.txt").createNewFile();

    List<File> result = PSMLOverviews.getContents(tempDir.toFile());
    assertEquals(1, result.size());
    assertEquals("doc.psml", result.get(0).getName());
  }

  @Test
  void getContents_fileInsteadOfDirectory_returnsEmptyList() throws IOException {
    File f = new File(tempDir.toFile(), "file.psml");
    f.createNewFile();

    List<File> result = PSMLOverviews.getContents(f);
    assertTrue(result.isEmpty());
  }

  // --- lastModified() ---

  @Test
  void lastModified_emptyList_returnsZero() {
    assertEquals(0L, PSMLOverviews.lastModified(Collections.emptyList()));
  }

  @Test
  void lastModified_singleFile_returnsItsTimestamp() throws IOException {
    File f = new File(tempDir.toFile(), "doc.psml");
    f.createNewFile();
    long ts = f.lastModified();

    assertEquals(ts, PSMLOverviews.lastModified(Collections.singletonList(f)));
  }

  @Test
  void lastModified_multipleFiles_returnsMostRecent() throws IOException {
    File older = new File(tempDir.toFile(), "old.psml");
    File newer = new File(tempDir.toFile(), "new.psml");
    older.createNewFile();
    // Ensure different timestamps by setting them explicitly
    older.setLastModified(1000L);
    newer.createNewFile();
    newer.setLastModified(2000L);

    long result = PSMLOverviews.lastModified(List.of(older, newer));
    assertEquals(2000L, result);
  }

  @Test
  void lastModified_reverseOrder_stillReturnsMostRecent() throws IOException {
    File older = new File(tempDir.toFile(), "old.psml");
    File newer = new File(tempDir.toFile(), "new.psml");
    older.createNewFile();
    older.setLastModified(1000L);
    newer.createNewFile();
    newer.setLastModified(3000L);

    long result = PSMLOverviews.lastModified(List.of(newer, older));
    assertEquals(3000L, result);
  }
}
