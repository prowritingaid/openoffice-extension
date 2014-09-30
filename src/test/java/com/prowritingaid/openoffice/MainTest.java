/* ProWritingAid, a natural language style checker 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package com.prowritingaid.openoffice;

import junit.framework.TestCase;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.ProofreadingResult;
import com.prowritingaid.openoffice.*;

public class MainTest extends TestCase {
  
  public void testDoProofreading() {
	  SingletonFactory fact = new SingletonFactory();
	  Object r = fact.createInstanceWithContext(null);
	  assertNotNull(fact);
	  String testSentence = "This is a test mistake that I've made";
	  ProofreadingResult result = ((com.prowritingaid.openoffice.Main)r).doProofreading("1", testSentence, new Locale(), 0 , testSentence.length(),
		      new PropertyValue[0]);
	  assertEquals(1, result.aErrors.length);;
  }
  

  public void testVariants() {
  }

  public void testCleanFootnotes() {
//    final Main prog = new Main(null);
//    assertEquals("A house.¹ Here comes more text.", prog.cleanFootnotes("A house.1 Here comes more text."));
  //  assertEquals("A road that's 3.4 miles long.", prog.cleanFootnotes("A road that's 3.4 miles long."));
   // assertEquals("A house.1234 Here comes more text.", prog.cleanFootnotes("A house.1234 Here comes more text."));  // too many digits for a footnote
    String input    = "Das Haus.1 Hier kommt mehr Text2. Und nochmal!3 Und schon wieder ein Satz?4 Jetzt ist aber Schluss.";
    String expected = "Das Haus.¹ Hier kommt mehr Text2. Und nochmal!¹ Und schon wieder ein Satz?¹ Jetzt ist aber Schluss.";
   // assertEquals(expected, prog.cleanFootnotes(input));
  }

}
