package test;

import my.util.Util;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UtilTest extends TestCase{
	
	public UtilTest(String name) {
		setName(name);
	}
	
	public static Test suite(){
		TestSuite suite=new TestSuite();
		
		for( String s : testOrder ) {
			suite.addTest(new UtilTest(s));
		}
		
		return suite;
	}
	
	static final String[] testOrder = new String[]{
		"testExtractByte",
		"testExtractPutternPerPix",
		"testExtractARGB",
		"testEmbedPix"
	};
	
	public void testExtractByte() throws Exception {
		assertEquals(0, Util.extractByte(4, 0));
		assertEquals(0, Util.extractByte(4, 1));
		assertEquals(1, Util.extractByte(4, 2));
	}
	
	public void testExtractPutternPerPix() throws Exception {
		byte[] epp = Util.extractErrorPutternPerPix(255, 8);
		
		for( int item : epp ) {
			assertEquals(1, item);
		}
		
		epp = Util.extractErrorPutternPerPix(127, 8);
		int[] exp = new int[]{1,1,1,1,1,1,1,0};
		for( int i=0; i<epp.length; i++ ) {
			assertEquals(exp[i], epp[i]);
		}
	}
	
	public void testExtractARGB() throws Exception {
		int[] argb = Util.extractARGB(-1);
		for( int item : argb ) {
			assertEquals(255, item);
		}
	}
	
	public void testEmbedPix() throws Exception {
		int argb = Util.embededPix(-1, 1);
		int[] argbArr = Util.extractARGB(argb);
		int[] exp = new int[]{255, 254, 254, 254};
		for( int i=0; i<argbArr.length; i++ ) {
			assertEquals(exp[i], argbArr[i]);
		}
		
		argb = Util.embededPix(0, 1);
		argbArr = Util.extractARGB(argb);
		exp = new int[]{0,1,1,1};
		for( int i=0; i<argbArr.length; i++ ) {
			assertEquals(exp[i], argbArr[i]);
		}
	}
	
	
}
