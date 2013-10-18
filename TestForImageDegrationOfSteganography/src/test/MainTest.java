package test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MainTest extends TestCase{
	
	public MainTest(String name) {
		setName(name);
	}
	
	public static Test suite(){
		TestSuite suite=new TestSuite();
		
		for( String s : testOrder ) {
			suite.addTest(new MainTest(s));
		}
		
		return suite;
	}
	
	static final String[] testOrder = new String[]{
	};
	
	public void testname() throws Exception {
		
	}
}