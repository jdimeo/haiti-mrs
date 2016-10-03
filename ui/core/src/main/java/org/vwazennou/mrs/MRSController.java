/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.data.Option;
import org.vwazennou.mrs.data.ParentOf;
import org.vwazennou.mrs.dictionary.Dictionary;
import org.vwazennou.mrs.dictionary.DictionaryEntry;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.patient.PatientGroup;
import org.vwazennou.mrs.script.Directive;
import org.vwazennou.mrs.script.DirectiveTextRegistry;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.search.SearchField;
import org.vwazennou.mrs.search.SearchFieldRegistry;
import org.vwazennou.mrs.task.DirectiveReport;
import org.vwazennou.mrs.task.PatientIndex;
import org.vwazennou.mrs.task.Query;
import org.vwazennou.mrs.task.Query.QueryCallback;
import org.vwazennou.mrs.task.excel.DataExport;
import org.vwazennou.mrs.task.excel.DataExport.ExportType;
import org.vwazennou.mrs.task.excel.DataExport.SortOrder;
import org.vwazennou.mrs.task.excel.MedicineReport;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.commons.lang.Utilities;
import com.datamininglab.commons.logging.LogContext;
import com.datamininglab.foundation.data.DataList;
import com.datamininglab.foundation.data.lut.LookupTable;
import com.datamininglab.viz.gui.Controller;
import com.datamininglab.viz.gui.Controller.Task;
import com.datamininglab.viz.gui.swt.controls.data.DataTable;

import gnu.trove.procedure.TObjectProcedure;

public class MRSController extends Controller {
	private Session session;
	private Client client;
	private Formulary formulary;
	private Dictionary dictionary;
	private DirectiveTextRegistry directiveText;
	private SearchFieldRegistry searchFields;
	
	private LookupTable<String, PatientGroup> patientGroups;
	private LookupTable<String, ClinicTeam>   clinicTeams;
	private LookupTable<Integer, Directive>   directives;
	private Prescription[]                    standardScripts;
	private Set<String>                       uniqueCommunities;
	private Set<String>                       uniqueCities;
	private Set<String>                       uniqueProviders;
	
	public MRSController() {
		searchFields = new SearchFieldRegistry()
				.register(Patient.class)
				.register(Visit.class)
				.register(Prescription.class);
	}
	
	/** For advanced use only! */
	public Session getSession() {
		return session;
	}
	public Formulary getFormulary() { 
		return formulary;
	}
	public Dictionary getDictionary() {
		return dictionary;
	}
	public LookupTable<PatientGroup, String> getPatientGroups() {
		return patientGroups;
	}
	public LookupTable<ClinicTeam, String> getClinicTeams() {
		return clinicTeams;
	}
	public LookupTable<Directive, Integer> getDirectives() {
		return directives;
	}
	public DirectiveTextRegistry getDirectiveText() {
		return directiveText;
	}
	public SearchFieldRegistry getSearchFields() {
		return searchFields;
	}
	public Set<String> getUniqueCommunities() {
		return uniqueCommunities;
	}
	public Set<String> getUniqueCities() {
		return uniqueCities;
	}
	public Set<String> getUniqueProviders() {
		return uniqueProviders;
	}
	public Prescription[] getStandardScripts() {
		return standardScripts;
	}
	
	public MRSController initialize() {
		LogContext.info("Initializing MRS...");
		session = Database.connect();
		client = Client.getAll(session).getOrAdd(Option.CLIENT_ID.longValue())
				.setName(Option.CLIENT_NAME.toString());
		
		dictionary    = new Dictionary(session);
		directiveText = new DirectiveTextRegistry(session);
		formulary     = new Formulary(session);
		DictionaryEntry.setGlobalDictionary(dictionary);
		
		patientGroups = PatientGroup.getAll(session);
		clinicTeams   = ClinicTeam.getAll(session);
		directives    = Directive.getAll(session);
		
		uniqueCities      = loadUnique(Patient.class, "city");
		uniqueCommunities = loadUnique(Patient.class, "community");
		uniqueProviders   = loadUnique(Visit.class, "provider");
		loadStandardScripts();
		
		return this;
	}
	
	private Set<String> loadUnique(Class<?> c, String prop) {
		LogContext.info("Loading unique " + Utilities.pluralize(prop) + "...");
		Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		
		List<?> l = session.createCriteria(c).setProjection(
				Projections.distinct(Projections.property(prop))).list();
		for (Object o : l) {
			String str = ObjectUtils.toString(o);
			if (!str.isEmpty()) { set.add(str); }
		}		return set;
	}
	
	private void loadStandardScripts() {
		LogContext.info("Loading standard prescriptions...");
		List<?> l = session.createCriteria(Prescription.class)
		                   .add(Restrictions.isNull("visit")).list();
		int i = 0;
		standardScripts = new Prescription[l.size()];
		for (Object o : l) {
			Prescription p = (Prescription) o;
			p.inflateChildren(getFormulary());
			standardScripts[i++] = p;
		}
	}
	
	@Override
	public void shutDown() {
		super.shutDown();
		System.out.println("Closing database connection...");
		Database.disconnect(session);
		Database.disconnect();
	}
	
	public class SaveObjects extends Task implements TObjectProcedure<Object> {
		private Object[] arr;
		public SaveObjects(Object... arr) {
			super(false);
			this.arr = arr;
			if (arr.length > 0) { add(this); }
		}
		
		@Override
		public void doTask() {
			status.newTask(Str.SAVING + " " + arr[0] + "...");
			try {
				for (Object o : arr) {
					ParentOf.recurse(getFormulary(), o, this);
					session.saveOrUpdate(o);
				}
				session.getTransaction().commit();
				session.beginTransaction();
			} catch (HibernateException ex) {
				status.setError(Str.ERROR_SAVING.toString() + arr[0] + " (" + ex.getMessage() + ")");
			}
		}
		
		@Override
		public boolean execute(Object o) {
			if (o instanceof MRSMergable) {
				MRSMergable m = (MRSMergable) o;
				if (m.getOriginalClient() == null) {
					m.setOriginalClient(client);
					client.markModified();
				}
			}
			return true;
		}
	}
	
	public class DeleteObject extends Task {
		private Object o;
		public DeleteObject(Object o) {
			super(false);
			this.o = o;
			add(this);
		}
		
		@Override
		public void doTask() {
			status.newTask("Deleting " + o + "...");
			try {
				session.delete(o);
				session.getTransaction().commit();
				session.beginTransaction();
			} catch (HibernateException ex) {
				status.setError("Error deleting " + o + ": " + ex.getMessage());
			}
		}
	}
	
	public class SaveDictionary extends Task {
		public SaveDictionary() {
			super(false);
			add(this);
		}
		
		@Override
		public void doTask() {
			status.newTask(Str.SAVING_DICTIONARY.toString() + "...");
			dictionary.applyChanges();
		}
	}
	
	public class RefreshDictionary extends Task {
		private Language lang;
		private DataTable<DictionaryEntry> table;
		
		public RefreshDictionary(Language lang, DataTable<DictionaryEntry> table) {
			super(false);
			this.lang  = lang;
			this.table = table;
			add(this);
		}
		
		@Override
		public void doTask() {
			dictionary.applyChanges();
			DataList<DictionaryEntry> list = new DataList<>();
			list.addAll(dictionary.getEntires(lang));
			table.setData(list);
		}
	}
	
	public class ExportToExcel extends Task {
		private String file; 
		private ExportType type;
		private SortOrder order;
		
		public ExportToExcel(String file, ExportType type, SortOrder order) {
			super(false);
			this.file = file;
			this.type = type;
			this.order = order;
			add(this);
		}
		
		@Override
		public void doTask() {
			Language l = dictionary.getLanguage();
			if (type == ExportType.LADS) {
				l = dictionary.getOrAddLanguage("Kreyol", "Haiti");
			}
			new DataExport(l, type, order).export(status, file);
		}
	}
	
	public class ExportMedReport extends Task {
		private String file; 
		public ExportMedReport(String file) {
			super(false);
			this.file = file;
			add(this);
		}
		
		@Override
		public void doTask() {
			new MedicineReport(formulary).export(status, file);
		}
	}
	
	public class ExportPatientIndex extends Task {
		private String file;
		public ExportPatientIndex(String file) {
			super(false);
			this.file = file;
			add(this);
		}
		
		@Override
		public void doTask() {
			new PatientIndex(file).generate(status);
		}
	}
	
	public class ExportDirectiveReport extends Task {
		public ExportDirectiveReport() {
			super(true);
			add(this);
		}
		
		@Override
		public void doTask() {
			new DirectiveReport().generate(status);
		}
	}
	
	public class QueryAll<T> extends Task {
		private Class<T>  type;
		private Criterion crit;
		private QueryAllCallback<T> callback;
		
		public QueryAll(Class<T> type, Criterion crit, QueryAllCallback<T> callback) {
			super(false);
			this.type = type;
			this.crit = crit;
			this.callback = callback;
			add(this);
		}
		
		@Override
		public void doTask() {
			status.newTask(Str.QUERYING + "...");
			Criteria c = session.createCriteria(type);
			if (crit != null) { c.add(crit); }
			c.setReadOnly(true);
			callback.handleResults(c.list());
		}
	}

	public class PerformQuery extends Task {
		private Query query;
		
		public PerformQuery(Object query, Collection<SearchField> fields, QueryCallback callback) {
			super(false);
			this.query = new Query(session, formulary, query, fields, callback);
			add(this);
		}
		
		@Override
		public void doTask() {
			query.perform(status);
		}
		
		public Object[] getResults() {
			return query.getResults();
		}
	}
	
	public interface QueryAllCallback<T> {
		void handleResults(List<T> list);
	}
}