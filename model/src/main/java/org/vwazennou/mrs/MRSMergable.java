/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs;

import org.vwazennou.mrs.data.Client;

public interface MRSMergable {
	int UNMERGED = 0;
	
	long getId();
	long getOriginalId();
	void setOriginalId(long id);
	
	Client getOriginalClient();
	void setOriginalClient(Client c);
	
	int getVersion();
}
