package net.jradius.tests;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class AppTestCase extends TestCase
{
    private ApplicationContext ac;

    /**
     * Loads the relevant ApplicationContext and is responsible
     * for setting up a Hibernate session. This ensures that the
     * lazy-loading mechanism works in our unit tests.
     */
    public void setUp() throws Exception
    {
        setUp(new String[] { "spring-config.xml" });
    }
    

    /**
     * Loads the relevant ApplicationContext and is responsible
     * for setting up a Hibernate session. This ensures that the
     * lazy-loading mechanism works in our unit tests.
     */
    public void setUp(String[] appContexts) throws Exception
    {
        ac = new ClassPathXmlApplicationContext(appContexts);
    }
    
    /**
     * Closes the hibernate session.
     */
    public void tearDown() throws Exception
    {
    }
    
    public ApplicationContext getAc()
    {
        return ac;
    }
    public void setAc(ApplicationContext ac)
    {
        this.ac = ac;
    }
}
