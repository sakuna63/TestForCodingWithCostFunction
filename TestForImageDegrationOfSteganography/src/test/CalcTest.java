package test;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

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
		"testPSNR"
	};
	
	public void testPSNR() throws Exception {
		File file = new File("./img/");
		BufferedImage img1 = ImageIO.read(file.listFiles()[0]);
		BufferedImage img2 = ImageIO.read(file.listFiles()[0]);
		
		assertEquals(-1.0, Calc.PSNR(img1, img2));
		
//		for(int i=0; i<img1.getHeight(); i++) {
//			for(int j=0; j<img1.getWidth(); j++) {
//				img1.setRGB(j, i, (0 << 24 | 1 << 16 | 1 << 8 | 1) & 0x00ffffff);
//				Util.print(Util.extractARGB(img1.getRGB(j, i))[0]);
//				Util.print(Util.extractARGB(img1.getRGB(j, i))[1]);
//				Util.print(Util.extractARGB(img1.getRGB(j, i))[2]);
//				Util.print(Util.extractARGB(img1.getRGB(j, i))[3]);
//				Util.print(Util.extractARGB(img2.getRGB(j, i))[0]);
//				Util.print(Util.extractARGB(img2.getRGB(j, i))[1]);
//				Util.print(Util.extractARGB(img2.getRGB(j, i))[2]);
//				Util.print(Util.extractARGB(img2.getRGB(j, i))[3]);
//				System.out.println("");
//			}
//		}
//
//		
//		
//		double mse = 1;
//		double exp = 10 * Math.log10( Math.pow(255, 2) / mse );
//		assertEquals(exp, Calc.PSNR(img1, img2));
	}
}