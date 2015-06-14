package com.example.java;

public class IntentFilter {
	private String cmp, action, category, typ;
	
	public IntentFilter(String cmp, String action, String category) {
		this.cmp = cmp;
		this.action = action;
		this.category = category;
	}
	
	public String toString() {
		return "{cmp=" + this.cmp +"\n"+
				(action !="" && action != null ? " act="+ this.action  :" " ) +"\n"+
				(category !="" && category != null ? " cat=["+ this.category + "]" : " ") +"\n"+
				(typ !="" && typ != null ? " typ="+ this.typ :" ")+"}";
	}

}
