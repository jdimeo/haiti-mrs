/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors;

public interface Editor<S, T, E> {
	void createNew(S parent);
	void set(T t);
	T get();
	void setDefault(E obj, boolean isNew);
}
