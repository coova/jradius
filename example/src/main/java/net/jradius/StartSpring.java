package net.jradius;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StartSpring
{
    public static void main(String argv[]) 
    {
        ApplicationContext ac = new ClassPathXmlApplicationContext(new String[] { "spring-config.xml" });
    }
}
