/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

/**
 * Test Triple.
 */
@Test
public class TripleTest {

  public void testOf_Object_Object() {
    Triple<String, String, String> test = Triple.of("A", "B", "C");
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
  }

  //-------------------------------------------------------------------------
  public void testTriple_Object_Object_Object() {
    Triple<String, String, String> test = new Triple<String, String, String>("A", "B", "C");
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getThird(), "C");
  }

  public void testTriple_Object_Object_null() {
    Triple<String, String, String> test = new Triple<String, String, String>("A", "B", null);
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getThird(), null);
  }

  public void testTriple_Object_null_Object() {
    Triple<String, String, String> test = new Triple<String, String, String>("A", null, "C");
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), null);
    assertEquals(test.getThird(), "C");
  }

  public void testTriple_Object_null_null() {
    Triple<String, String, String> test = new Triple<String, String, String>("A", null, null);
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), null);
    assertEquals(test.getThird(), null);
  }

  public void testTriple_null_Object_Object() {
    Triple<String, String, String> test = new Triple<String, String, String>(null, "B", "C");
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getThird(), "C");
  }

  public void testTriple_null_Object_null() {
    Triple<String, String, String> test = new Triple<String, String, String>(null, "B", null);
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getThird(), null);
  }

  public void testTriple_null_null_Object() {
    Triple<String, String, String> test = new Triple<String, String, String>(null, null, "C");
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), null);
    assertEquals(test.getThird(), "C");
  }

  public void testTriple_null_null_null() {
    Triple<String, String, String> test = new Triple<String, String, String>(null, null, null);
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), null);
    assertEquals(test.getThird(), null);
  }

  //-------------------------------------------------------------------------
  public void compareTo() {
    Triple<String, String, String> abc = Triple.of("A", "B", "C");
    Triple<String, String, String> abd = Triple.of("A", "B", "D");
    Triple<String, String, String> acc = Triple.of("A", "C", "C");
    Triple<String, String, String> bac = Triple.of("B", "A", "C");
    
    assertTrue(abc.compareTo(abc) == 0);
    assertTrue(abc.compareTo(abd) < 0);
    assertTrue(abc.compareTo(acc) < 0);
    assertTrue(abc.compareTo(bac) < 0);
    
    assertTrue(abd.compareTo(abc) > 0);
    assertTrue(abd.compareTo(abd) == 0);
    assertTrue(abd.compareTo(acc) < 0);
    assertTrue(abd.compareTo(bac) < 0);
    
    assertTrue(acc.compareTo(abc) > 0);
    assertTrue(acc.compareTo(abd) > 0);
    assertTrue(acc.compareTo(acc) == 0);
    assertTrue(acc.compareTo(bac) < 0);
    
    assertTrue(bac.compareTo(abc) > 0);
    assertTrue(bac.compareTo(abd) > 0);
    assertTrue(bac.compareTo(acc) > 0);
    assertTrue(bac.compareTo(bac) == 0);
  }

  public void compareTo_null() {
    Triple<String, String, String> nnn = Triple.of(null, null, null);
    Triple<String, String, String> naa = Triple.of(null, "A", "A");
    Triple<String, String, String> ann = Triple.of("A", null, null);
    Triple<String, String, String> ana = Triple.of("A", null, "A");
    Triple<String, String, String> aan = Triple.of("A", "A", null);
    Triple<String, String, String> aaa = Triple.of("A", "A", "A");
    
    assertTrue(nnn.compareTo(nnn) == 0);
    assertTrue(nnn.compareTo(naa) < 0);
    assertTrue(nnn.compareTo(ann) < 0);
    assertTrue(nnn.compareTo(ana) < 0);
    assertTrue(nnn.compareTo(aan) < 0);
    assertTrue(nnn.compareTo(aaa) < 0);
    
    assertTrue(naa.compareTo(nnn) > 0);
    assertTrue(naa.compareTo(naa) == 0);
    assertTrue(naa.compareTo(ann) < 0);
    assertTrue(naa.compareTo(ana) < 0);
    assertTrue(naa.compareTo(aan) < 0);
    assertTrue(naa.compareTo(aaa) < 0);
    
    assertTrue(ann.compareTo(nnn) > 0);
    assertTrue(ann.compareTo(naa) > 0);
    assertTrue(ann.compareTo(ann) == 0);
    assertTrue(ann.compareTo(ana) < 0);
    assertTrue(ann.compareTo(aan) < 0);
    assertTrue(ann.compareTo(aaa) < 0);
    
    assertTrue(ana.compareTo(nnn) > 0);
    assertTrue(ana.compareTo(naa) > 0);
    assertTrue(ana.compareTo(ann) > 0);
    assertTrue(ana.compareTo(ana) == 0);
    assertTrue(ana.compareTo(aan) < 0);
    assertTrue(ana.compareTo(aaa) < 0);
    
    assertTrue(aan.compareTo(nnn) > 0);
    assertTrue(aan.compareTo(naa) > 0);
    assertTrue(aan.compareTo(ann) > 0);
    assertTrue(aan.compareTo(ana) > 0);
    assertTrue(aan.compareTo(aan) == 0);
    assertTrue(aan.compareTo(aaa) < 0);
    
    assertTrue(aaa.compareTo(nnn) > 0);
    assertTrue(aaa.compareTo(naa) > 0);
    assertTrue(aaa.compareTo(ann) > 0);
    assertTrue(aaa.compareTo(ana) > 0);
    assertTrue(aaa.compareTo(aan) > 0);
    assertTrue(aaa.compareTo(aaa) == 0);
  }

  public void testEquals() {
    Triple<Integer, String, String> a = new Triple<Integer, String, String>(1, "Hello", "C");
    Triple<Integer, String, String> b = new Triple<Integer, String, String>(1, "Goodbye", "C");
    Triple<Integer, String, String> c = new Triple<Integer, String, String>(2, "Hello", "C");
    Triple<Integer, String, String> d = new Triple<Integer, String, String>(2, "Goodbye", "C");
    Triple<Integer, String, String> e = new Triple<Integer, String, String>(2, "Goodbye", "D");
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);
    assertEquals(a.equals(e), false);
    
    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);
    assertEquals(b.equals(e), false);
    
    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);
    assertEquals(c.equals(e), false);
    
    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);
    assertEquals(d.equals(e), false);
    
    assertEquals(e.equals(a), false);
    assertEquals(e.equals(b), false);
    assertEquals(e.equals(c), false);
    assertEquals(e.equals(d), false);
    assertEquals(e.equals(e), true);
  }

  public void testEquals_null() {
    Triple<Integer, String, String> a = new Triple<Integer, String, String>(1, "Hello", "C");
    Triple<Integer, String, String> b = new Triple<Integer, String, String>(null, "Hello", "C");
    Triple<Integer, String, String> c = new Triple<Integer, String, String>(1, null, "C");
    Triple<Integer, String, String> d = new Triple<Integer, String, String>(null, null, "C");
    Triple<Integer, String, String> e = new Triple<Integer, String, String>(null, null, null);
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);
    assertEquals(a.equals(e), false);
    
    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);
    assertEquals(b.equals(e), false);
    
    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);
    assertEquals(c.equals(e), false);
    
    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);
    assertEquals(d.equals(e), false);
    
    assertEquals(e.equals(a), false);
    assertEquals(e.equals(b), false);
    assertEquals(e.equals(c), false);
    assertEquals(e.equals(d), false);
    assertEquals(e.equals(e), true);
  }

  public void testHashCode() {
    Triple<Integer, String, String> a = new Triple<Integer, String, String>(1, "Hello", "C");
    Triple<Integer, String, String> b = new Triple<Integer, String, String>(null, "Hello", "C");
    Triple<Integer, String, String> c = new Triple<Integer, String, String>(1, null, "C");
    Triple<Integer, String, String> d = new Triple<Integer, String, String>(null, null, "C");
    Triple<Integer, String, String> e = new Triple<Integer, String, String>(null, null, null);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(b.hashCode(), b.hashCode());
    assertEquals(c.hashCode(), c.hashCode());
    assertEquals(d.hashCode(), d.hashCode());
    assertEquals(e.hashCode(), e.hashCode());
    // can't test for different hash codes as they might not be different
  }
  
  public void toList() {
    Triple<String, String, String> a = new Triple<String, String, String>("Jay-Z", "Black Album", "99 Problems");
    List<String> asList = a.toList();
    assertNotNull(asList);
    assertEquals(3, asList.size());
    assertEquals("Jay-Z", asList.get(0));
    assertEquals("Black Album", asList.get(1));
    assertEquals("99 Problems", asList.get(2));
  }

  public void toFirstPair() {
    Triple<String, String, String> a = new Triple<String, String, String>("Jay-Z", "Black Album", "99 Problems");
    Pair<String, String> pair = a.toFirstPair();
    assertNotNull(pair);
    assertEquals("Jay-Z", pair.getFirst());
    assertEquals("Black Album", pair.getSecond());
  }

  public void toSecondPair() {
    Triple<String, String, String> a = new Triple<String, String, String>("Jay-Z", "Black Album", "99 Problems");
    Pair<String, String> pair = a.toSecondPair();
    assertNotNull(pair);
    assertEquals("Black Album", pair.getFirst());
    assertEquals("99 Problems", pair.getSecond());
  }

}
