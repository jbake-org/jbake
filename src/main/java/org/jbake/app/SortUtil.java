package org.jbake.app;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;

/**
 * Provides Sort related functions
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class SortUtil {
	public static final int NORMAL = 1;
	public static final int REVERSE = -1;

	public static Comparator<Map<String, Object>> getComparator() {
		return getComparator(NORMAL);
	}

	public static Comparator<Map<String, Object>> getComparator(final int order) {
		return new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> c1, Map<String, Object> c2) {
				if (c1.get("date") != null && c2.get("date") != null) {
					if ((c1.get("date") instanceof Date) && (c2.get("date") instanceof Date)) {
						return ((Date) c1.get("date")).compareTo((Date) c2.get("date")) * order;
					}
				}
				return 0;
			}
		};
	}

}
