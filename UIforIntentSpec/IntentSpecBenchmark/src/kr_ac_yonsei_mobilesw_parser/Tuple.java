package kr_ac_yonsei_mobilesw_parser;

public class Tuple <T> {
	private T value;
	private String inp; 

	public Tuple()
	{
	}

	public Tuple(T value, String inp)
	{
		this.value = value;
		this.inp = inp;
	}

	public void setTuple(T value, String inp)
	{
		this.value = value;
		this.inp = inp;
	}

	public T getValue()
	{
		return value;
	}

	public String getInp()
	{
		return inp;
	}
}