package test;

import java.math.BigInteger;

import my.util.Calc;
import my.util.Util;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CalcTest extends TestCase{
	
	public CalcTest(String name) {
		setName(name);
	}
	
	public static Test suite(){
		TestSuite suite=new TestSuite();
		
		for( String s : testOrder ) {
			suite.addTest(new CalcTest(s));
		}
		
		return suite;
	}
	
	static final String[] testOrder = new String[]{
		"testda",
		"testFactorial",
		"testFactorial2",
		"testCombination",
		"testPSNR"
	};
	
	public void testda() throws Exception {
		int msg = 130, n = 136;
		int[] i = Util.calcErrorWeightAndNum(msg, n);
		int[] c = Util.countableCode(n, i[0], i[1]);
		int[] e = Util.message2Error(msg, n);
		int m = Util.error2Message(n, e);
		
		assertEquals(130, m);
	}
	
	public void testFactorial() throws Exception {
		long num = Calc.factorial(5);
		assertEquals(30, num);
		
//		num = Calc.factorial(144);
//		assertTrue(num.longValue() > 0);
	}
	
	public void testFactorial2() throws Exception {
		long num = Calc.factorial(10, 4, 2);
		assertEquals(7560 * 5, num);
	}
	
	public void testCombination() throws Exception{
		long num = Calc.combination(5, 2);
		assertEquals(10, num);
		
		num = Calc.combination(144, 144);
		assertEquals(1, num);

		num = Calc.combination(144, 1);
		assertEquals(144, num);
	}
	
	public void testPSNR() throws Exception {
		byte[] data = new byte[]{1,4,4,23,124};
		byte[] data2 = data.clone();
		
		assertEquals(-1.0, Calc.PSNR(data, data2, 0));
		
		data2[0] = -128;
		
		assertFalse(-1.0 == Calc.PSNR(data, data2, 0));
	}
}