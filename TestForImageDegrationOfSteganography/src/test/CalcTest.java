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
	    "testSplitedAreaDespersion"
	};
	
	@org.junit.Test
    public void testSplitedAreaDespersion() throws Exception {
        byte[] buff = new byte[100];
        for(int i = 0; i<50; i++) {
            buff[i] = -128;
        }
        double result = Calc.splitAreaDispersion(buff, 10, 5);
        assertEquals(0.0, result);
    }
}