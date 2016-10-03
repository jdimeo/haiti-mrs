/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui;

import org.vwazennou.mrs.dictionary.DictionaryEntry.CompoundStr;
import org.vwazennou.mrs.dictionary.Str;

import com.datamininglab.commons.icons.eri.IconsERI;
import com.datamininglab.commons.icons.ms.IconsMS;
import com.datamininglab.viz.gui.UIAction;


public final class MRSActions {
	private MRSActions() {
		// Prevent initialization
	}
	
	public static final UIAction CLOSE             = new UIAction(Str.CLOSE,               null, IconsERI.CLOSE, "Esc");
	public static final UIAction HELP              = new UIAction(Str.HELP,                null, IconsMS.HELP, "F1");
	public static final UIAction EXIT              = new UIAction(Str.EXIT,                null, null, null);
	public static final UIAction ABOUT             = new UIAction(new CompoundStr(Str.ABOUT, " ", Str.APP_TITLE), null, null, "Ctrl+F1");
	
	public static final UIAction SET_DATA_PATH     = new UIAction(Str.SET_DATA_PATH,       null, IconsMS._576_640, null);
	public static final UIAction SET_PATIENT_GROUP = new UIAction(Str.SET_PATIENT_GROUP,   null, IconsMS.USERS, null);
	public static final UIAction SET_CLINIC_TEAM   = new UIAction(Str.SET_CLINIC_TEAM,     null, IconsMS.USER_CALENDAR, null);
	public static final UIAction SET_LANGUAGE      = new UIAction(Str.LANGUAGE,            null, null, null);
	
	public static final UIAction NEW_PATIENT       = new UIAction(Str.NEW_PATIENT,         null, IconsSilk.USER_ADD, "Ctrl+N");
	public static final UIAction EDIT_PATIENT      = new UIAction(Str.EDIT_PATIENT,        null, IconsSilk.USER_EDIT, "Ctrl+O");
	public static final UIAction SAVE_PATIENT      = new UIAction(Str.SAVE,                null, IconsMS.SAVE, "Ctrl+S");	
	public static final UIAction DELETE_PATIENT    = new UIAction(Str.DELETE_PATIENT,      null, IconsSilk.USER_DELETE, null);
	public static final UIAction SUMMARIZE_PATIENT = new UIAction(Str.PATIENT_SUMMARY,     null, IconsMS._480_752, null);
	
	public static final UIAction SHOW_VISITS       = new UIAction(Str.SHOW_VISITS,         null, IconsMS._704_736, null);
	public static final UIAction NEW_VISIT         = new UIAction(Str.NEW_VISIT,           null, IconsSilk.TABLE_ADD, "Ctrl+N");
	public static final UIAction EDIT_VISIT        = new UIAction(Str.EDIT_VISIT,          null, IconsSilk.TABLE_EDIT, "Ctrl+O");
	public static final UIAction SAVE_VISIT        = new UIAction(Str.SAVE,                null, IconsMS.SAVE, "Ctrl+S");
	public static final UIAction DELETE_VISIT      = new UIAction(Str.DELETE_VISIT,        null, IconsSilk.TABLE_DELETE, null);
	
	public static final UIAction SHOW_URINE_EDITOR = new UIAction(Str.ADD_RESULTS,         null, null, null);
	public static final UIAction ADD               = new UIAction(Str.ADD,                 null, IconsMS.ADD_BLUE, null);

	public static final UIAction SHOW_SCRIPTS      = new UIAction(Str.SHOW_VISITS,         null, IconsMS._704_736, null);
	public static final UIAction NEW_SCRIPT        = new UIAction(Str.NEW_PRESCRIPTION,    null, IconsSilk.PILL_ADD, "Ctrl+N");
	public static final UIAction EDIT_SCRIPT       = new UIAction(Str.EDIT_PRESCRIPTION,   null, IconsSilk.PILL_GO, "Ctrl+O");
	public static final UIAction SAVE_SCRIPT       = new UIAction(Str.SAVE,                null, IconsMS.SAVE, "Ctrl+S");
	public static final UIAction DELETE_SCRIPT     = new UIAction(Str.DELETE_PRESCRIPTION, null, IconsSilk.PILL_DELETE, null);
	
	public static final UIAction NEW_DIRECTIVE     = new UIAction(Str.NEW_DIRECTIVE,       Str.NEW_DIRECTIVE, IconsMS.ADD_BLUE, null);
	public static final UIAction PREV_DIRECTIVE    = new UIAction(Str.PREVIOUS_DIRECTIVE,  Str.PREVIOUS_DIRECTIVE, IconsERI.ARROW_UP_BLUE, null);
	public static final UIAction NEXT_DIRECTIVE    = new UIAction(Str.NEXT_DIRECTIVE,      Str.NEXT_DIRECTIVE, IconsMS.ARROW_DOWN_BLUE, null);
	public static final UIAction SAVE_DIRECTIVE    = new UIAction(Str.SAVE,                null, IconsMS.SAVE, "Ctrl+S");
	public static final UIAction DELETE_DIRECTIVE  = new UIAction(Str.REMOVE,              Str.REMOVE, IconsMS.DELETE, null);

	public static final UIAction EDIT_DICTIONARY   = new UIAction(Str.EDIT_DICTIONARY,     null, IconsMS.GLOBE, null);
	public static final UIAction SAVE_DICTIONARY   = new UIAction(Str.SAVE,                null, IconsMS.SAVE, "Ctrl+S");
	public static final UIAction EDIT_TRANSLATION  = new UIAction(Str.EDIT_TRANSLATION,    null, IconsSilk.PENCIL, "F2");
	public static final UIAction DELETE_PHRASE     = new UIAction(Str.DELETE_PHRASE,       null, IconsMS.DELETE, null);
	
	public static final UIAction SHOW_SEARCH       = new UIAction(Str.SEARCH,              null, IconsMS.MAGNIFYING_GLASS, "Ctrl+F");
	public static final UIAction DO_SEARCH         = new UIAction(Str.SEARCH,              null, IconsMS.MAGNIFYING_GLASS, null);
	public static final UIAction EXPAND_ALL        = new UIAction(Str.EXPAND_ALL,          null, IconsMS.ADD_BLUE, null); 
	public static final UIAction COLLAPSE_ALL      = new UIAction(Str.COLLAPSE_ALL,        null, IconsMS.SUBTRACT_BLUE, null);
	public static final UIAction PREVIOUS_SEARCH   = new UIAction(Str.PREVIOUS_SEARCH,     null, IconsMS.ARROW_LEFT_GREEN_CIRCLE, "BS");
	public static final UIAction NEXT_SEARCH       = new UIAction(Str.NEXT_SEARCH,         null, IconsMS.ARROW_RIGHT_GREEN_CIRCLE, null);
	
	public static final UIAction EXPORT_PT_IDX     = new UIAction(Str.EXPORT_PT_IDX,       null, IconsMS._80_64, null);
	public static final UIAction EXPORT_TO_EXCEL   = new UIAction(Str.EXPORT_TO_EXCEL,     null, IconsMS.LOGO_EXCEL, "Ctrl+E");
	public static final UIAction EXPORT_MED_REPORT = new UIAction(Str.EXPORT_MED_REPORT,   null, IconsMS.CLIPBOARD_CHECK, null);
	public static final UIAction EXPORT_DIR_REPORT = new UIAction(Str.EXPORT_DIR_REPORT,   null, null, null);
	
	public static final UIAction SHOW_DATA_TABLE   = new UIAction(Str.DATA_TABLE,          null, IconsMS.TABLE, "Ctrl+T");
	public static final UIAction REFRESH_DATA      = new UIAction(Str.REFRESH,             null, IconsMS.PAGE_REFRESH, "F5");
	public static final UIAction PREVIOUS_PAGE     = new UIAction(Str.PREVIOUS_PAGE,       null, IconsMS.ARROW_LEFT_GREEN_CIRCLE, null);
	public static final UIAction NEXT_PAGE         = new UIAction(Str.NEXT_PAGE,           null, IconsMS.ARROW_RIGHT_GREEN_CIRCLE, null);
}