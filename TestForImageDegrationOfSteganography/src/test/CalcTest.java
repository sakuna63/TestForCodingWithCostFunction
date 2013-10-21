package test;

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
		"testFactorial2",
		"testCombination",
		"testPSNR"
	};
	
	public void testFactorial() throws Exception {
		long num = Calc.factorial(5);
		assertEquals(120, num);
	}
	
	public void testFactorial2() throws Exception {
		long num = Calc.factorial(5, 2);
		assertEquals(60, num);
	}
	
	public void testCombination() throws Exception{
		long num = Calc.combination(5, 2);
	}
	
	public void testPSNR() throws Exception {
		byte[] data = new byte[]{1,4,4,23,124};
		byte[] data2 = data.clone();
		
		assertEquals(-1.0, Calc.PSNR(data, data2, 0));
		
		data2[0] = -128;
		
		assertFalse(-1.0 == Calc.PSNR(data, data2, 0));
	}
}