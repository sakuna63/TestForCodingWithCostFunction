package test;

import java.math.BigInteger;

import my.util.Calc;
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
		"testFactorial",
		"testCombination",
		"testPSNR"
	};
	
	public void testFactorial() throws Exception {
		BigInteger num = Calc.factorial(5);
		assertEquals(120, num.longValue());
		
		num = Calc.factorial(144);
		assertTrue(num.longValue() > 0);
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