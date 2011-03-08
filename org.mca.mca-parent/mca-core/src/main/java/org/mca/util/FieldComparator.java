package org.mca.util;

import java.lang.reflect.Field;
import java.util.Comparator;

public class FieldComparator implements Comparator {
	public FieldComparator() {}

	public int compare(Object o1, Object o2) {
		Field f1 = (Field)o1;
		Field f2 = (Field)o2;
		if (f1 == f2)
			return 0;
		if (f1.getDeclaringClass() == f2.getDeclaringClass())
			return f1.getName().compareTo(f2.getName());
		if (f1.getDeclaringClass().isAssignableFrom(
				f2.getDeclaringClass()))
			return -1;
		return 1;
	}
}
