package test;

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
		"testCountableCode",
		"testHamingWeight",
		"testPSNR"
	};
	
	public void testCountableCode() throws Exception {
		int result = Calc.countableCode(6, 2, 8);
		assertEquals(20, result);

		result = Calc.countableCode(1, 0, 0);
		assertEquals(0, result);

		result = Calc.countableCode(1, 1, 0);
		assertEquals(1, result);
	}
	
	public void testHamingWeight() throws Exception {
		int result = Calc.hamingWeight(6);
		assertEquals(2, result);

		result = Calc.hamingWeight(1);
		assertEquals(1, result);

		result = Calc.hamingWeight(0);
		assertEquals(0, result);
	}
	
	public void testPSNR() throws Exception {
		byte[] data = new byte[]{1,4,4,23,124};
		byte[] data2 = data.clone();
		
		assertEquals(-1.0, Calc.PSNR(data, data2, 0));
		
		data2[0] = -128;
		
		assertFalse(-1.0 == Calc.PSNR(data, data2, 0));
	}
}