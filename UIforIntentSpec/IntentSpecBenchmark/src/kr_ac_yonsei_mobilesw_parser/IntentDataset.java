package kr_ac_yonsei_mobilesw_parser;

import java.util.HashMap;

public class IntentDataset{
	private HashMap<String, IntentData> intentDataset = new HashMap<String, IntentData>();

	public void addIntent(IntentData intentdata)
	{
		String key = (intentdata.getAction() != null) ? intentdata.getAction() : "";
		key += intentdata.getComponent();

		this.intentDataset.put(key, intentdata);
	}

	public IntentData getIntent(String ActionAndComponent)
	{
		return intentDataset.get(ActionAndComponent);
	}

	public void remove(String ActionAndComponent)
	{
		intentDataset.remove(ActionAndComponent);
	}

	public void removeAll()
	{
		intentDataset.clear();
	}

	public int length()
	{
		return intentDataset.size();
	}
}