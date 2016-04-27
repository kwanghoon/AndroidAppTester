package com.example.java;

public class IntentFilter {
	private String cmp, action, category, typ;
	private Boolean exported;
	
	public IntentFilter(String cmp, String action, String category, Boolean exported) {
		this.cmp = cmp;
		this.action = action;
		this.category = category;
		this.exported = exported;
	}
	
	public String toString() {
		return "{cmp=" + this.cmp +"\n"+
				(action !="" && action != null ? " act="+ this.action  :" " ) +"\n"+
				(category !="" && category != null ? " cat=["+ this.category + "]" : " ") +"\n"+
				(typ !="" && typ != null ? " typ="+ this.typ :" ")+"\n" + 
				(exported != null && exported == false ? " internal=True " : "") + "}";
	}
	
	public boolean isInternal()
	{
		return exported != null && exported == false ? true : false;
	}

	public String toStringIfInternal() {
		return exported != null && exported == false ? "{cmp=" + this.cmp +"\n"+
				(action !="" && action != null ? " act="+ this.action  :" " ) +"\n"+
				(category !="" && category != null ? " cat=["+ this.category + "]" : " ") +"\n"+
				(typ !="" && typ != null ? " typ="+ this.typ :" ")+"\n" + "}" : "";
	}
}
