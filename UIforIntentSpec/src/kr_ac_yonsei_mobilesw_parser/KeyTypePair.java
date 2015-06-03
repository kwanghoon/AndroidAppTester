package kr_ac_yonsei_mobilesw_parser;

public class KeyTypePair{
	private String key;
	private String type; 

	public KeyTypePair()
	{
	}

	public KeyTypePair(String key, String type)
	{
		this.key = key;
		this.type = type;
	}

	public void setPair(String key, String type)
	{
		this.key = key;
		this.type = type;
	}

	public String getKey()
	{
		return key;
	}

	public String getType()
	{
		return type;
	}
}