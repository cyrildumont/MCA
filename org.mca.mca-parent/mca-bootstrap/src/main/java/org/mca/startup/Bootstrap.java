package org.mca.startup;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

/**
 * 
 */

/**
 * @author Cyril
 *
 */
public class Bootstrap implements Daemon{

	public final static String COMPONENT_NAME = "org.mca.start.Bootstrap";

	private static final Logger logger = Logger.getLogger(COMPONENT_NAME);
	
	private static Bootstrap daemon = null;

	private ClassLoader loader = null;

	private static String DIR_LIB = "/lib";

	private String configFile;
	
	public void destroy() {

	}

	public void init(DaemonContext context) throws Exception{
		
		logger.finest("Bootstrap -- init");
		
		//ClassLoaderFactory.updateCurrentClassloader(System.getProperty("mca.home") + DIR_LIB);
		loader = createClassLoader();	
		Thread.currentThread().setContextClassLoader(loader);
		String[] args = context.getArguments();
		this.configFile = args[0];
	}

	/**
	 * Cr�ation d'un classLoader
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */

	private ClassLoader createClassLoader() throws IOException, MalformedURLException {

		ClassLoader classLoader = ClassLoaderFactory.createClassLoader(System.getProperty("mca.home") + DIR_LIB);

		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		// Register the server classloader
		ObjectName objectName;
		try {
			objectName = new ObjectName("MCA:type=ClassLoader,name=shared");
			mBeanServer.registerMBean(classLoader, objectName);
			

		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		}

		return classLoader;
	}

	/**
	 * D�marrage du service
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {
		
		logger.finest("Bootstrap -- start");
		Class mcaClass = loader.loadClass("org.mca.core.MCA");
		Class[] paramClass = new Class[]{};
		Constructor constructor = mcaClass.getConstructor(paramClass);
		Object mcaDaemon = constructor.newInstance();
		logger.finest("Bootstrap -- configfile : " + configFile );
        Method method = mcaDaemon.getClass().getMethod("start",new Class[]{String.class});
        Object[] parameters = new Object[]{configFile};
        method.invoke(mcaDaemon, parameters);
 
	}

	public void stop() throws Exception {
		System.exit(0);
	}

	public static void main(String[] args) {

		if (daemon == null) {
			daemon = new Bootstrap();
		}

		try {
			String command = "start";
			if (args.length > 0) {
				command = args[args.length - 1];
			}
			if (command.equals("start")){
				daemon.start();
			} else if (command.equals("stop")) {
				daemon.stop();
			} else {
				System.err.println("la commande " + command + " n'existe pas.");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
