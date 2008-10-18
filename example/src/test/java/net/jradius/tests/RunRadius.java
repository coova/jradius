package net.jradius.tests;

public class RunRadius extends AppTestCase
{
    
    public void setUp() throws Exception
    {
        super.setUp();
    }
    
    public void testRadius()
    {        
        while(true) try { 
            Thread.sleep(1000 * 60 * 60); 
            //JRadiusManager jradius = (JRadiusManager) getAc().getBean("jRadiusManager");
            //jradius.stop();
        } catch (Exception e) { }
    }
}
