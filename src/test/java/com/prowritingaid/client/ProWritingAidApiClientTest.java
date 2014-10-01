package com.prowritingaid.client;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import junit.framework.TestCase;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.ProofreadingResult;
import com.prowritingaid.client.*;

public class ProWritingAidApiClientTest extends TestCase {
	  public void testAnalyze() throws ClientProtocolException, IOException {
		  ProWritingAidApiClient client = new ProWritingAidApiClient("");
		  TagAnalysisResponse result = client.analyze("This is a teest", "grammar", WritingStyle.Creative);
		  assertEquals(1, result.tags.length);
		  assertEquals(10, result.tags[0].startPos);
		  assertEquals(14, result.tags[0].endPos);
		  assertEquals("teest", result.tags[0].subcategory);
	  }
}
