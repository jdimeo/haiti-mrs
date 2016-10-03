/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.script;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.vwazennou.mrs.dictionary.Language;

import com.datamininglab.foundation.util.Structures.TwoKeyHashMap;

public class DirectiveTextRegistry {
	private TwoKeyHashMap<Integer, Language, DirectiveText> textMap;
	
	public DirectiveTextRegistry(Session s) {
		textMap = new TwoKeyHashMap<>();
		for (Object o : s.createCriteria(DirectiveText.class).list()) {
			DirectiveText dt = (DirectiveText) o;
			int n = dt.getDirective().getCode();
			textMap.put(n, dt.getLanguage(), dt);
		}
	}
	
	public DirectiveText get(Language l, int directiveNum) {
		Map<Language, DirectiveText> map = textMap.get(directiveNum);
		if (map == null || map.isEmpty()) { return null; }
		
		DirectiveText dt = map.get(l);
		if (dt != null) { return dt; }
		
		return map.values().iterator().next();
	}
	
	public String getString(Prescription p, Language l) {
		return getString(p, l, 0, Integer.MAX_VALUE);
	}
	public String getString(Prescription p, Language l, int from, int to) {
		if (p == null) { return StringUtils.EMPTY; }
		
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int pos = 0;
		String curr = StringUtils.EMPTY;
		
		for (PrescriptionDirective pd : p.getDirectives()) {
			if (i++ < from) { continue; }
			if (i > to) { break; }
			
			if (sb.length() > 0) {
				sb.append(curr, pos, curr.length()).append('\n');
			}
			
			int n = pd.getDirective().getCode();
			
			DirectiveText txt = get(l, n);
			sb.append(n).append(". ");
			curr = txt.getTitle() + "\n" + txt.getText(); pos = 0;
			
			for (PrescriptionDirectiveBlank pdb : pd.getBlanks()) {
				String val = pdb.getValue();
				if (val != null) {
					int j = curr.indexOf('_', pos);
					sb.append(curr, pos, j).append(val);
					pos = j + 1;
				}
			}
		}
		sb.append(curr, pos, curr.length());
		return sb.toString();
	}
}
