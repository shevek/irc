package org.anarres.ircd;

import java.lang.management.ManagementFactory;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class Management {

    public static ObjectName register(String name, Object mbean) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName oname = new ObjectName(
                    "org.anarres.ircd:type=" + name
            );
            mbs.registerMBean(mbean, oname);
            return oname;
        } catch (JMException e) {
            e.printStackTrace();
            /* bleh. */
            return null;
        }
    }

    public static void unregister(ObjectName name) {
        if (name != null) {
            try {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.unregisterMBean(name);
            } catch (JMException e) {
                e.printStackTrace();
                /* Ignore. */
            }
        }
    }

}
