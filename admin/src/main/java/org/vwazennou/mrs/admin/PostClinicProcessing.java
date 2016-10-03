/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.admin;

import org.vwazennou.mrs.admin.merge.MergeClients;
import org.vwazennou.mrs.admin.util.DedupPatientsVisits;
import org.vwazennou.mrs.admin.util.DeleteBlankVisits;
import org.vwazennou.mrs.admin.util.MergeClinics;
import org.vwazennou.mrs.admin.util.UpdateRanks;
import org.vwazennou.mrs.data.Database;

public final class PostClinicProcessing {
	private PostClinicProcessing() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		// Always start with a fresh copy of the master (assumes the runner
		// of the script didn't actually edit/change data)
		Database.getClientDirectory().delete();
		MergeClients.main(args);
		MergeClinics.main(args);
		DeleteBlankVisits.main(args);
		DedupPatientsVisits.main(args);
		UpdateRanks.main(args);
	}
}
