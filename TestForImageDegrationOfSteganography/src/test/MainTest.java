package test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import my.img.CoverData;
import my.Main;
import my.img.StegoData;

import java.io.File;
import java.lang.reflect.Method;

public class MainTest extends TestCase{

    int[] msg;
    CoverData cover;
    Main main;
    Method createStegoData, compMsg, createMsg;

	public MainTest(String name) {
		setName(name);
	}

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        File file = new File("./img/origin/").listFiles()[0];
        cover = new CoverData(file);

        createStegoData = Main.class.getDeclaredMethod("createStegoData", new Class[]{CoverData.class, int[].class, int.class, int.class});
        createMsg = Main.class.getDeclaredMethod("createMsg", new Class[]{int.class, int.class});
        compMsg = Main.class.getDeclaredMethod("compMsg", new Class[]{int[].class, int[].class, int.class, int.class});

        createStegoData.setAccessible(true);
        createMsg.setAccessible(true);
        compMsg.setAccessible(true);

        main = new Main();
        msg = (int[]) createMsg.invoke(main, new Object[]{0, cover.sliceBuffWithoutOffset().length});
    }

    public static Test suite(){
		TestSuite suite=new TestSuite();
		
		for( String s : testOrder ) {
			suite.addTest(new MainTest(s));
		}
		
		return suite;
	}
	
	static final String[] testOrder = new String[]{
        "testCreateStegoData",
        "testCalcPSNR"
	};

    public void testCalcPSNR() throws Exception {
        StegoData stego1 = (StegoData) createStegoData.invoke(main, new Object[]{cover, msg, 8, 1});
        StegoData stego2 = (StegoData) createStegoData.invoke(main, new Object[]{cover, msg, 8, 2});

        assertFalse(compBuff(stego1.buff, stego2.buff, stego1.buff_offset));
        assertTrue( stego1.psnr(cover) != stego2.psnr(cover));
    }

	public void testCreateStegoData() throws Exception {
        StegoData stego = (StegoData) createStegoData.invoke(main, new Object[]{cover, msg, 10, 5});

        int[] eMsg = stego.extracting(cover);
        assertTrue((boolean) compMsg.invoke(main, new Object[]{msg, eMsg, 8, 1}));
	}

    private boolean compBuff(byte[] buff1, byte[] buff2, int offset) {
        boolean flag = true;
        for(int i=offset; i<buff1.length; i++) {
            if(buff1[i] != buff2[i]) {
                flag = false;
                break;
            }
        }
        return flag;
    }
}