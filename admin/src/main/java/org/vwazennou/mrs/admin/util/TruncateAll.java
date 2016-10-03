/*
 * Copyright (c) 2014 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.vwazennou.mrs.data.Database;

public class TruncateAll {
	public static void main(String[] args) {
		Session s = Database.connect();
		for (ClassMetadata cm : s.getSessionFactory().getAllClassMetadata().values()) {
			Class<?> c = cm.getMappedClass();
			System.out.println("Truncating " + c);
			s.createQuery("delete from " + c.getName()).executeUpdate();
		}
		Database.disconnect(s);
		Database.disconnect();
	}
}