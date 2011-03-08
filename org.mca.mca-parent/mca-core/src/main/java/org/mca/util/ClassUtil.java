/**
 * 
 */
package org.mca.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Cyril
 *
 */
public abstract class ClassUtil {

	/** Log */
	private final static Log LOG = LogFactory.getLog(ClassUtil.class);

	/**
	 * 
	 * @param object
	 * @param fieldname
	 * @return
	 */
	public static Object getValueOfField(Object object, String fieldname){
		return getValueOfField(object, object.getClass(), fieldname);
	}

	/**
	 * 
	 * @param object
	 * @param classObject
	 * @param fieldname
	 * @return
	 */
	public static Object getValueOfField(Object object, Class classObject, String fieldname){
		try {
			Field field = classObject.getDeclaredField(fieldname);
			field.setAccessible(true);
			return field.get(object);
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * 
	 * @param object
	 * @param methodname
	 * @param parameters
	 * @param parametersValues
	 * @return
	 */
	public static Object invokeMethod(Object object, String methodname, Class[] parameters, Object[] parametersValues){
		return invokeMethod(object, object.getClass(), methodname, parameters, parametersValues);
	}

	/**
	 * 
	 * @param object
	 * @param classObject
	 * @param methodname
	 * @param parameters
	 * @param parametersValues
	 * @return
	 */
	public static Object invokeMethod(Object object, Class classObject, String methodname, Class[] parameters, Object[] parametersValues){
		try {
			Method method = classObject.getDeclaredMethod(methodname, parameters);
			method.setAccessible(true);
			Object result = method.invoke(object, parametersValues);;
			if (LOG.isDebugEnabled()) {
				LOG.debug("---------- invokeMethod ----------");
				LOG.debug("		classloader ---> " + Thread.currentThread().getContextClassLoader());
				LOG.debug("		object ---> " + object);
				LOG.debug("		classObject ---> " + classObject.getName());
				LOG.debug("		methodname --> " + methodname);
				String sParameters = "[";
				for (Class parameter : parameters) {
					sParameters += parameter.getName() + ";";
				}
				sParameters += "]";
				LOG.debug("		parameters --> " + sParameters);
				String sParameterValues = "[";
				for (Object parameterValue : parametersValues) {
					sParameterValues += parameterValue + ";";
				}
				sParameterValues += "]";
				LOG.debug("		parametersValues --> " + sParameterValues);
				LOG.debug("		result --> " + result);
			}
			return result;
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param object
	 * @param classObject
	 * @param log
	 */
	public static void logFields(Object object,Class classObject, Log log) {

		try{
			Field[] fields = classObject.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Object value = field.get(object);
				log.debug("		" + field.getName() + " = " + value);
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 
	 * @param classname
	 * @return
	 */
	public static Field[] getFields(Class theClass) {
		final Field[] fields = theClass.getFields();
		Arrays.sort(fields, new FieldComparator());
		return fields;
	}

}
