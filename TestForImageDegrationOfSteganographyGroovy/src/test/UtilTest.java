package test;

import java.util.HashMap;

import my.util.Calc;
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
		"testHamingWeight",
		"testRaiseBit",
		"testCountableCode",
		"testAntiCountableCode",
		"testError2Message",
		"testExtractErrorPattern",
		"testA",
		"testB"
	};
	
	public void testA() throws Exception {
		int[] ep = Util.message2Error(138, 136);
		assertEquals(5, ep[0]);
		byte[] eppArray = Util.extractErrorPutternPerPix(ep, 136);
		
		int msg = Util.error2Message(136, ep);
		assertEquals(138, msg);
	}
	
	public void testB() throws Exception {
		int n=5;
		for(int i=0; i<Calc.combination(n, 2); i++)
			Util.println(i + ":" + Util.countableCode(n, 2, i)[0]);
	}
	
	public void testExtractByte() throws Exception {
		assertEquals(0, Util.extractByte(4, 0));
		assertEquals(0, Util.extractByte(4, 1));
		assertEquals(1, Util.extractByte(4, 2));
	}
	
//	public void testExtractPutternPerPix() throws Exception {
//		byte[] epp = Util.extractErrorPutternPerPix(255, 8);
//		
//		for( int item : epp ) {
//			assertEquals(1, item);
//		}
//		
//		epp = Util.extractErrorPutternPerPix(127, 8);
//		int[] exp = new int[]{1,1,1,1,1,1,1,0};
//		for( int i=0; i<epp.length; i++ ) {
//			assertEquals(exp[i], epp[i]);
//		}
//	}
	

	public void testCountableCode() throws Exception {
		int[] result = Util.countableCode(6, 2, 8);
		assertEquals(20, result[0]);

		result = Util.countableCode(1, 0, 0);
		assertEquals(0, result[0]);

		result = Util.countableCode(1, 1, 0);
		assertEquals(1, result[0]);
	}
	
	public void testAntiCountableCode() throws Exception {
		int[] code = Util.countableCode(6, 2, 8);
		int result = Util.antiCountableCode(6, 2, code);
		assertEquals(8, result);
		
		code = Util.countableCode(16, 5, 0);
		result = Util.antiCountableCode(16, 5, code);
		assertEquals(0, result);
	}
	
	public void testCalcOffset() throws Exception {
		int result = Util.calcOffset(8, 0);
		assertEquals(0, result);
		
		result = Util.calcOffset(8, 1);
		assertEquals(1, result);

		result = Util.calcOffset(8, 2);
		assertEquals(9, result);
	}
	
	public void testMessage2Error() throws Exception {
		int[] result = Util.message2Error(0, 136);
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);
		assertEquals(0, result[2]);
		assertEquals(0, result[3]);
		assertEquals(0, result[4]);
		assertEquals(0, result[5]);
		assertEquals(0, result[6]);
		assertEquals(0, result[7]);

		result = Util.message2Error(1, 136);
		assertEquals(1, result[0]);
		assertEquals(0, result[1]);
		assertEquals(0, result[2]);
		assertEquals(0, result[3]);
		assertEquals(0, result[4]);
		assertEquals(0, result[5]);
		assertEquals(0, result[6]);
		assertEquals(0, result[7]);

		result = Util.message2Error(129, 136);
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);
		assertEquals(0, result[2]);
		assertEquals(0, result[3]);
		assertEquals(1, result[4]);
		assertEquals(0, result[5]);
		assertEquals(0, result[6]);
		assertEquals(0, result[7]);
	}
	
	public void testError2Message() throws Exception {
		int[] code = Util.countableCode(6, 2, 8);
		int result = Util.error2Message(6, code);
		assertEquals(15, result);
		
		code = Util.countableCode(16, 5, 0);
		result = Util.error2Message(16, code);
		int expected=0;
		for(int i=0; i<5; i++) {
			expected += Calc.combination(16, i);
		}
		assertEquals(expected, result);
	}
	
	public void testExtractErrorPattern() throws Exception {
		byte[] stego = new byte[100];
		byte[] cover = stego.clone();
		
		int[] result = Util.extractErrorPattern(stego, cover, 0, 10);
		assertEquals(result[0], 0);
		assertEquals(result[1], 0);
		assertEquals(result[2], 0);
		assertEquals(result[3], 0);
		
		stego[0] = 0x01;
		result = Util.extractErrorPattern(stego, cover, 0, 10);
		assertEquals(result[0], 512);
		assertEquals(result[1], 0);
		assertEquals(result[2], 0);
		assertEquals(result[3], 0);
		
	}
	
	public void testHamingWeight() throws Exception {
		int result = Util.hamingWeight(6);
		assertEquals(2, result);

		result = Util.hamingWeight(1);
		assertEquals(1, result);

		result = Util.hamingWeight(0);
		assertEquals(0, result);
	}
	
	public void testRaiseBit() throws Exception {
		int[] code = new int[8];
		Util.raiseBit(code, 3);
		assertEquals(code[0], 8);
		
		Util.raiseBit(code, 64);
		assertEquals(code[0], 8);
		assertEquals(code[2], 1);
		
		Util.raiseBit(code, 255);
		assertEquals(code[0], 8);
		assertEquals(code[2], 1);
		
		code = new int[8];
		Util.raiseBit(code, 31);
//		assertEquals((long)Math.pow(2, 31), code[0]);

		code = new int[8];
		Util.raiseBit(code, 31);
//		assertEquals((int)Math.pow(2, 31), code[0]);
	}
}
