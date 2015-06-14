package kr_ac_yonsei_mobilesw_parser;

import java.util.ArrayList;

public class IntentData{
	private String action;
	private ArrayList<String> category = new ArrayList<String>();
	private Boolean data;
	private Boolean type;
	private String componentPkg;
	private String componentCls;
	private ArrayList<KeyTypePair> extra = new ArrayList<KeyTypePair>();
	private Boolean flag;
	
	public static enum ComponentType { 
			Activity, Service, BroadcastReceiver, ContentProvider 
		};
	
	private ComponentType compType;

	public void setAction(String action)
	{
		this.action = action;
	}

	public String getAction()
	{
		return action;
	}

	public void addCategory(String data)
	{
		category.add(data);
	}

	public String getCategory(int index)
	{
		return category.get(index);
	}

	public Object[] getCategoryArray()
	{
		return category.toArray();
	}

	public int lengthCategory()
	{
		return category.size();
	}

	public void setData(Boolean data)
	{
		this.data = data;
	}

	public Boolean getData()
	{
		return data;
	}

	public void setType(Boolean type)
	{
		this.type = type;
	}

	public Boolean getType()
	{
		return type;
	}

	public void setComponentPkg(String componentPkg)
	{
		this.componentPkg = componentPkg;
	}

	public String getComponentPkg()
	{
		return componentPkg;
	}

	public void setComponentCls(String componentCls)
	{
		this.componentCls = componentCls;
	}

	public String getComponentCls()
	{
		return componentCls;
	}

	public String getComponent()
	{
		if(componentPkg == null && componentCls == null)
		{
			return "";
		}
		else if(componentPkg == null)
		{
			return componentCls;
		}
		else if(componentCls == null)
		{
			return componentPkg;
		}
		else
		{
			return componentPkg + "/" + componentCls;
		}
	}
	
	public void setComponentType(String comptypestr) {
		if (comptypestr == null || "".equals(comptypestr))
			return;
		
		for (ComponentType ct : ComponentType.values()) {
			if ( ct.toString().equals(comptypestr) )
				compType = ct;
		}
	}
	
	public String getComponentType() {
		if (compType == null) return null;
		else return compType.toString();
	}

	public void addExtra(KeyTypePair extra)
	{
		this.extra.add(extra);
	}

	public KeyTypePair getExtra(int index)
	{
		return extra.get(index);
	}

	public Object[] getExtraArray()
	{
		return extra.toArray();
	}

	public int lengthExtra()
	{
		return extra.size();
	}

	public void setFlag(Boolean flag)
	{
		this.flag = flag;
	}

	public Boolean getFlag()
	{
		return flag;
	}

}