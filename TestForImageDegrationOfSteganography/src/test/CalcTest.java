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
		"testPSNR"
	};
	
	public void testPSNR() throws Exception {
		byte[] data = new byte[]{1,4,4,23,124};
		byte[] data2 = data.clone();
		
		assertEquals(-1.0, Calc.PSNR(data, data2, 0));
		
		data2[0] = -128;
		
		assertFalse(-1.0 == Calc.PSNR(data, data2, 0));
	}
}