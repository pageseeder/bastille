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
package org.pageseeder.bastille.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathsTest {

  // --- normalize ---

  @Test
  void normalize_root() {
    assertEquals("/", Paths.normalize("/"));
  }

  @Test
  void normalize_simplePath_unchanged() {
    assertEquals("/a/b/c", Paths.normalize("/a/b/c"));
  }

  @Test
  void normalize_trailingSlash_removed() {
    assertEquals("/a/b", Paths.normalize("/a/b/"));
  }

  @Test
  void normalize_rootTrailingSlash_unchanged() {
    // Root "/" must not be trimmed to ""
    assertEquals("/", Paths.normalize("/"));
  }

  @Test
  void normalize_doubleSlash_collapsed() {
    assertEquals("/a/b", Paths.normalize("/a//b"));
  }

  @Test
  void normalize_multipleDoubleSlashes_collapsed() {
    assertEquals("/a/b/c", Paths.normalize("//a//b//c"));
  }

  @Test
  void normalize_leadingSelfRef_removed() {
    assertEquals("a/b", Paths.normalize("./a/b"));
  }

  @Test
  void normalize_selfRefInMiddle_removed() {
    assertEquals("/a/b", Paths.normalize("/a/./b"));
  }

  @Test
  void normalize_parentRef_resolved() {
    assertEquals("/a/c", Paths.normalize("/a/b/../c"));
  }

  @Test
  void normalize_parentRef_atDepthOne() {
    assertEquals("/b", Paths.normalize("/a/../b"));
  }

  @Test
  void normalize_multipleParentRefs_resolved() {
    assertEquals("/c", Paths.normalize("/a/b/../../c"));
  }

  // --- path ---

  @Test
  void path_sameRoot() {
    assertEquals(".", Paths.path("/", "/"));
  }

  @Test
  void path_samePath() {
    assertEquals(".", Paths.path("/a", "/a"));
  }

  @Test
  void path_siblingPages() {
    assertEquals("../b", Paths.path("/a", "/b"));
  }

  @Test
  void path_fromNormalisedPath() {
    // /a/a/../c normalises to /a/c; relative from /a/c to /a is ../../a
    assertEquals("../../a", Paths.path("/a/a/../c", "/a"));
  }

  @Test
  void path_fromRootToChild() {
    assertEquals("b", Paths.path("/", "/b"));
  }

  @Test
  void path_fromRootToNestedChild() {
    assertEquals("b/c", Paths.path("/", "/b/c"));
  }

  @Test
  void path_deeperFrom() {
    // /a/b/c has 3 slashes → 3 ".." steps → "../../../x"
    assertEquals("../../../x", Paths.path("/a/b/c", "/x"));
  }

  @Test
  void path_toDeeper() {
    assertEquals("../b/c", Paths.path("/a", "/b/c"));
  }
}
