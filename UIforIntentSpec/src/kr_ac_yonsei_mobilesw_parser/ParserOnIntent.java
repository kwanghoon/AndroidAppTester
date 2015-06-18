package kr_ac_yonsei_mobilesw_parser;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class ParserOnIntent <T> {
	IntentDataset intentDataset = new IntentDataset();
	IntentData intentData;
	MalformedIntentException mexp;

	public ArrayList<Tuple<T>> pReturn(T a, String inp)
	{
		Tuple<T> tuple = new Tuple<T>();
		tuple.setTuple(a, inp);
		ArrayList<Tuple<T>> tupleList = new ArrayList<Tuple<T>>();
		tupleList.add(tuple);

		return tupleList;
	}

	public ArrayList<Tuple<T>> failure()
	{
		ArrayList<Tuple<T>> tupleList = new ArrayList<Tuple<T>>();
		return tupleList;
	}

	public ArrayList<Tuple<Character>> item(String inp)
	{
		ArrayList<Tuple<Character>> tupleList = new ArrayList<Tuple<Character>>();

		if(inp.length() == 0)
		{
			return tupleList;
		}
		else if(inp.length() > 0)
		{
			Tuple<Character> tuple = new Tuple<Character>();
			tuple.setTuple(inp.charAt(0), inp.substring(1, inp.length()));
			tupleList.add(tuple);

			return tupleList;
		}

		return null;
	}

	public ArrayList<Tuple<Character>> digit(String inp)
	{
		ArrayList<Tuple<Character>> tuplelist;

		ParserOnIntent<Character> sp = new ParserOnIntent<Character>();

		tuplelist = item(inp);
		if(tuplelist.isEmpty() == false && Character.isDigit(tuplelist.get(0).getValue()))
		{
			return sp.pReturn(tuplelist.get(0).getValue(), tuplelist.get(0).getInp());
		}

		return sp.failure();
	}

	public ArrayList<Tuple<Character>> lower(String inp)
	{
		ArrayList<Tuple<Character>> tuplelist;

		ParserOnIntent<Character> sp = new ParserOnIntent<Character>();

		tuplelist = item(inp);
		if(tuplelist.isEmpty() == false && Character.isLowerCase(tuplelist.get(0).getValue()))
		{
			return sp.pReturn(tuplelist.get(0).getValue(), tuplelist.get(0).getInp());
		}

		return sp.failure();
	}

	public ArrayList<Tuple<Character>> upper(String inp)
	{
		ArrayList<Tuple<Character>> tuplelist;

		ParserOnIntent<Character> sp = new ParserOnIntent<Character>();

		tuplelist = item(inp);
		if(tuplelist.isEmpty() == false && Character.isUpperCase(tuplelist.get(0).getValue()))
		{
			return sp.pReturn(tuplelist.get(0).getValue(), tuplelist.get(0).getInp());
		}

		return sp.failure();
	}

	public ArrayList<Tuple<Character>> letter(String inp)
	{
		ArrayList<Tuple<Character>> tuplelist;

		ParserOnIntent<Character> sp = new ParserOnIntent<Character>();

		tuplelist = item(inp);
		if(tuplelist.isEmpty() == false && Character.isLetter(tuplelist.get(0).getValue()))
		{
			return sp.pReturn(tuplelist.get(0).getValue(), tuplelist.get(0).getInp());
		}

		return sp.failure();
	}

	private static Character dollar = new Character('$');
	
	public ArrayList<Tuple<Character>> alphanum(String inp)
	{
		ArrayList<Tuple<Character>> tuplelist;

		ParserOnIntent<Character> sp = new ParserOnIntent<Character>();

		tuplelist = item(inp);
		if(tuplelist.isEmpty() == false && 
				(Character.isLetterOrDigit(tuplelist.get(0).getValue()) 
						|| dollar.equals(tuplelist.get(0).getValue())))  // Added '$' as a alphabet symbol
		{
			return sp.pReturn(tuplelist.get(0).getValue(), tuplelist.get(0).getInp());
		}

		return sp.failure();
	}

	public ArrayList<Tuple<Character>> pChar(char c, String inp)
	{
		ArrayList<Tuple<Character>> tuplelist;

		ParserOnIntent<Character> sp = new ParserOnIntent<Character>();

		tuplelist = item(inp);
		if(tuplelist.isEmpty() == false && c == tuplelist.get(0).getValue())
		{
			return sp.pReturn(tuplelist.get(0).getValue(), tuplelist.get(0).getInp());
		}

		return sp.failure();
	}

	public ArrayList<Tuple<String>> pString(String x, String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;

		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		if(x.length() == 0)
		{
			return sp.pReturn("", inp);
		}
		else
		{
			cTuplelist = pChar(x.charAt(0), inp);
			if(cTuplelist.isEmpty())
			{
				return sp.failure();
			}

			sTuplelist = pString(x.substring(1, x.length()), cTuplelist.get(0).getInp());
			if(sTuplelist.isEmpty())
			{
				return sp.failure();
			}

			return sp.pReturn(cTuplelist.get(0).getValue() + sTuplelist.get(0).getValue(), sTuplelist.get(0).getInp());
		}
	}

	public ArrayList<Tuple<String>> ident(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;
		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		cTuplelist = lower(inp);
		if(cTuplelist.isEmpty())
		{
			return sp.failure();
		}

		sTuplelist = manyAlphanum(cTuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			//return sp.failure();
			return sp.pReturn(String.valueOf(cTuplelist.get(0).getValue()), cTuplelist.get(0).getInp());
		}
		else
		{
			return sp.pReturn(cTuplelist.get(0).getValue() + sTuplelist.get(0).getValue(), sTuplelist.get(0).getInp());
		}
	}

	public ArrayList<Tuple<String>> manyAlphanum(String inp)
	{
		ArrayList<Tuple<String>> sTupleList;
		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		cTuplelist = alphanum(inp);
		if(cTuplelist.isEmpty())
		{
			return sp.failure();
		}

		sTupleList = manyAlphanum(cTuplelist.get(0).getInp());
		if(sTupleList.isEmpty())
		{
			return sp.pReturn(cTuplelist.get(0).getValue() + "", cTuplelist.get(0).getInp());
		}
		else
		{
			return sp.pReturn(cTuplelist.get(0).getValue() + sTupleList.get(0).getValue(), sTupleList.get(0).getInp());
		}
	}

	public ArrayList<Tuple<Integer>> nat(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;

		ParserOnIntent<Integer> sp = new ParserOnIntent<Integer>();

		sTuplelist = manyDigit(inp);
		if(sTuplelist.isEmpty())
		{
			return sp.failure();
		}
		else
		{
			return sp.pReturn(Integer.valueOf(sTuplelist.get(0).getValue()), sTuplelist.get(0).getInp());
		}


	}

	public ArrayList<Tuple<String>> manyDigit(String inp)
	{
		ArrayList<Tuple<String>> sTupleList;
		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		cTuplelist = digit(inp);
		if(cTuplelist.isEmpty())
		{
			return sp.failure();
		}

		sTupleList = manyDigit(cTuplelist.get(0).getInp());
		if(sTupleList.isEmpty())
		{
			return sp.pReturn(cTuplelist.get(0).getValue() + "", cTuplelist.get(0).getInp());
		}
		else
		{
			return sp.pReturn(cTuplelist.get(0).getValue() + sTupleList.get(0).getValue(), sTupleList.get(0).getInp());
		}
	}


	public ArrayList<Tuple<Integer>> pInt(String inp)
	{
		ArrayList<Tuple<Integer>> iTuplelist;
		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<Integer> sp = new ParserOnIntent<Integer>();

		cTuplelist = pChar('-', inp);
		if(cTuplelist.isEmpty())
		{
			return nat(inp);
		}

		iTuplelist = nat(cTuplelist.get(0).getInp());
		if(iTuplelist.isEmpty())
		{
			return nat(inp);
		}
		else
		{
			return sp.pReturn((-iTuplelist.get(0).getValue()), iTuplelist.get(0).getInp());
		}
	}


	public ArrayList<Tuple<String>> space(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;

		sTuplelist = manyisSpace(inp);

		return sTuplelist;
	}

	public ArrayList<Tuple<String>> manyisSpace(String inp)
	{
		ArrayList<Tuple<String>> sTupleList;
		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		cTuplelist = item(inp);
		if(cTuplelist.isEmpty() || ' ' != cTuplelist.get(0).getValue())
		{
			return sp.pReturn(null, inp);
		}

		sTupleList = manyisSpace(cTuplelist.get(0).getInp());
		return sp.pReturn(null, sTupleList.get(0).getInp());
	}

	public ArrayList<Tuple<String>> identifier(String inp)
	{
		ArrayList<Tuple<String>> tmp1Tuplelist;
		ArrayList<Tuple<String>> tmp2Tuplelist;
		ArrayList<Tuple<String>> sTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		tmp1Tuplelist = sp.space(inp);
		sTuplelist = sp.ident(tmp1Tuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			return sp.failure();
		}
		tmp2Tuplelist = sp.space(sTuplelist.get(0).getInp());

		return sp.pReturn(sTuplelist.get(0).getValue(), tmp2Tuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<Integer>> natural(String inp)
	{
		ArrayList<Tuple<String>> tmp1Tuplelist;
		ArrayList<Tuple<String>> tmp2Tuplelist;
		ArrayList<Tuple<Integer>> iTuplelist;

		ParserOnIntent<Integer> sp = new ParserOnIntent<Integer>();

		tmp1Tuplelist = sp.space(inp);
		iTuplelist = sp.nat(tmp1Tuplelist.get(0).getInp());
		if(iTuplelist.isEmpty())
		{
			return sp.failure();
		}
		tmp2Tuplelist = sp.space(iTuplelist.get(0).getInp());

		return sp.pReturn(iTuplelist.get(0).getValue(), tmp2Tuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<Integer>> integer(String inp)
	{
		ArrayList<Tuple<String>> tmp1Tuplelist;
		ArrayList<Tuple<String>> tmp2Tuplelist;
		ArrayList<Tuple<Integer>> iTuplelist;

		ParserOnIntent<Integer> sp = new ParserOnIntent<Integer>();

		tmp1Tuplelist = sp.space(inp);
		iTuplelist = sp.pInt(tmp1Tuplelist.get(0).getInp());
		if(iTuplelist.isEmpty())
		{
			return sp.failure();
		}
		tmp2Tuplelist = sp.space(iTuplelist.get(0).getInp());

		return sp.pReturn(iTuplelist.get(0).getValue(), tmp2Tuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> symbol(String str, String inp)
	{
		ArrayList<Tuple<String>> tmp1Tuplelist;
		ArrayList<Tuple<String>> tmp2Tuplelist;
		ArrayList<Tuple<String>> sTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		tmp1Tuplelist = sp.space(inp);
		sTuplelist = sp.pString(str, tmp1Tuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			return sp.failure();
		}
		tmp2Tuplelist = sp.space(sTuplelist.get(0).getInp());

		return sp.pReturn(sTuplelist.get(0).getValue(), tmp2Tuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> identAlpha(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;
		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		cTuplelist = sp.alphanum(inp);
		if(cTuplelist.isEmpty())
		{
			return sp.failure();
		}

		sTuplelist = manyAlphanum(cTuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			return sp.pReturn(String.valueOf(cTuplelist.get(0).getValue()), cTuplelist.get(0).getInp());
		}
		else
		{
			return sp.pReturn(cTuplelist.get(0).getValue() + sTuplelist.get(0).getValue(), sTuplelist.get(0).getInp());
		}
	}

	public ArrayList<Tuple<String>> idOrNum(String inp)
	{
		ArrayList<Tuple<String>> tmp1Tuplelist;
		ArrayList<Tuple<String>> tmp2Tuplelist;
		ArrayList<Tuple<String>> sTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		tmp1Tuplelist = sp.space(inp);
		sTuplelist = sp.identAlpha(tmp1Tuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			return sp.failure();
		}
		tmp2Tuplelist = sp.space(sTuplelist.get(0).getInp());

		return sp.pReturn(sTuplelist.get(0).getValue(), tmp2Tuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<Character>> alphanumOrDot(String inp)
	{
		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<Character> sp = new ParserOnIntent<Character>();

		cTuplelist = sp.item(inp);
		if(cTuplelist.isEmpty() == false && 
				(Character.isLetterOrDigit(cTuplelist.get(0).getValue()) == true 
						|| dollar.equals(cTuplelist.get(0).getValue()))) // Added '$' as a alphabet symbol
		{
			return sp.pReturn(cTuplelist.get(0).getValue(), cTuplelist.get(0).getInp());
		}

		//cTuplelist = sp.item(inp);
		if(cTuplelist.isEmpty() == false && '.' == cTuplelist.get(0).getValue())
		{
			return sp.pReturn(cTuplelist.get(0).getValue(), cTuplelist.get(0).getInp());
		}

		if(cTuplelist.isEmpty() == false && '_' == cTuplelist.get(0).getValue())
		{
			return sp.pReturn(cTuplelist.get(0).getValue(), cTuplelist.get(0).getInp());
		}

		return sp.failure();
	}

	public ArrayList<Tuple<String>> identOrDot(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;
		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		cTuplelist = sp.letter(inp);
		if(cTuplelist.isEmpty())
		{
			return sp.failure();
		}

		sTuplelist = manyAlphanumOrDot(cTuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			return sp.pReturn(String.valueOf(cTuplelist.get(0).getValue()), cTuplelist.get(0).getInp());
		}
		else
		{
			return sp.pReturn(cTuplelist.get(0).getValue() + sTuplelist.get(0).getValue(), sTuplelist.get(0).getInp());
		}
	}

	public ArrayList<Tuple<String>> manyAlphanumOrDot(String inp)
	{
		ArrayList<Tuple<String>> sTupleList;
		ArrayList<Tuple<Character>> cTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		cTuplelist = alphanumOrDot(inp);
		if(cTuplelist.isEmpty())
		{
			return sp.failure();
		}

		sTupleList = manyAlphanumOrDot(cTuplelist.get(0).getInp());
		if(sTupleList.isEmpty())
		{
			return sp.pReturn(cTuplelist.get(0).getValue() + "", cTuplelist.get(0).getInp());
		}
		else
		{
			return sp.pReturn(cTuplelist.get(0).getValue() + sTupleList.get(0).getValue(), sTupleList.get(0).getInp());
		}
	}

	public ArrayList<Tuple<String>> idOrDot(String inp)
	{
		ArrayList<Tuple<String>> tmp1Tuplelist;
		ArrayList<Tuple<String>> tmp2Tuplelist;
		ArrayList<Tuple<String>> sTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		tmp1Tuplelist = sp.space(inp);
		sTuplelist = sp.identOrDot(tmp1Tuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			return sp.failure();
		}
		tmp2Tuplelist = sp.space(sTuplelist.get(0).getInp());

		return sp.pReturn(sTuplelist.get(0).getValue(), tmp2Tuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> intent(String inp)
	{
		ArrayList<Tuple<String>> tmpTuplelist;
		ArrayList<Tuple<String>> tmp2Tuplelist;
		ArrayList<Tuple<String>> s1Tuplelist;
		ArrayList<Tuple<String>> s2Tuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		tmpTuplelist = symbol("{", inp);
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Intent. Token : " + errToken(inp), 0);
			//return sp.failure();
		}

		intentData = new IntentData();

		s1Tuplelist = fields(tmpTuplelist.get(0).getInp());
		if(s1Tuplelist.isEmpty())
		{
			return sp.failure();
		}

		tmpTuplelist = symbol("}", s1Tuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw mexp;
			//return sp.failure();
		}

		String intentKey = (intentData.getAction() != null)?intentData.getAction():"";
		if(intentData.getComponent() != null)
		{
			intentKey += intentData.getComponent();
		}

		if(intentDataset.getIntent(intentKey) != null)
		{
			throw new MalformedIntentException("Intent Key is not unique. Key : " + intentKey, 1);
		}

		intentDataset.addIntent(intentData);

		tmp2Tuplelist = symbol("||", tmpTuplelist.get(0).getInp());
		if(tmp2Tuplelist.isEmpty())
		{
			return sp.pReturn("{ " + s1Tuplelist.get(0).getValue() + " }", tmpTuplelist.get(0).getInp());
		}

		s2Tuplelist = intent(tmp2Tuplelist.get(0).getInp());
		if(s1Tuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Intent.", 0);
			//return sp.failure();
		}

		return sp.pReturn("{ " + s1Tuplelist.get(0).getValue() + " } || " + s2Tuplelist.get(0).getValue(), s2Tuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> fields(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;
		ArrayList<Tuple<String>> fTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		sTuplelist = action(inp);
		if(sTuplelist.isEmpty() == false)
		{
			mexp = new MalformedIntentException("couldn't parse after Action field. Token : " + errToken(sTuplelist.get(0).getInp()), 16);
			fTuplelist = fields(sTuplelist.get(0).getInp());
			if(fTuplelist.isEmpty() == false)
			{
				return sp.pReturn(sTuplelist.get(0).getValue() + fTuplelist.get(0).getValue(), fTuplelist.get(0).getInp());
			}
		}

		sTuplelist = category(inp);
		if(sTuplelist.isEmpty() == false)
		{
			mexp = new MalformedIntentException("couldn't parse after Category field. Token : " + errToken(sTuplelist.get(0).getInp()), 18);
			fTuplelist = fields(sTuplelist.get(0).getInp());
			if(fTuplelist.isEmpty() == false)
			{
				return sp.pReturn(sTuplelist.get(0).getValue() + fTuplelist.get(0).getValue(), fTuplelist.get(0).getInp());
			}
		}

		sTuplelist = idata(inp);
		if(sTuplelist.isEmpty() == false)
		{
			mexp = new MalformedIntentException("couldn't parse after Data field. Token : " + errToken(sTuplelist.get(0).getInp()), 17);
			fTuplelist = fields(sTuplelist.get(0).getInp());
			if(fTuplelist.isEmpty() == false)
			{
				return sp.pReturn(sTuplelist.get(0).getValue() + fTuplelist.get(0).getValue(), fTuplelist.get(0).getInp());
			}
		}

		sTuplelist = itype(inp);
		if(sTuplelist.isEmpty() == false)
		{
			mexp = new MalformedIntentException("couldn't parse after Type field. Token : " + errToken(sTuplelist.get(0).getInp()), 19);
			fTuplelist = fields(sTuplelist.get(0).getInp());
			if(fTuplelist.isEmpty() == false)
			{
				return sp.pReturn(sTuplelist.get(0).getValue() + fTuplelist.get(0).getValue(), fTuplelist.get(0).getInp());
			}
		}

		sTuplelist = component(inp);
		if(sTuplelist.isEmpty() == false)
		{
			mexp = new MalformedIntentException("couldn't parse after Component field. Token : " + errToken(sTuplelist.get(0).getInp()), 20);
			fTuplelist = fields(sTuplelist.get(0).getInp());
			if(fTuplelist.isEmpty() == false)
			{
				return sp.pReturn(sTuplelist.get(0).getValue() + fTuplelist.get(0).getValue(), fTuplelist.get(0).getInp());
			}
		}

		sTuplelist = extra(inp);
		if(sTuplelist.isEmpty() == false)
		{
			mexp = new MalformedIntentException("couldn't parse after Extra field. Token : " + errToken(sTuplelist.get(0).getInp()), 21);
			fTuplelist = fields(sTuplelist.get(0).getInp());
			if(fTuplelist.isEmpty() == false)
			{
				return sp.pReturn(sTuplelist.get(0).getValue() + fTuplelist.get(0).getValue(), fTuplelist.get(0).getInp());
			}
		}

		sTuplelist = flag(inp);
		if(sTuplelist.isEmpty() == false)
		{
			mexp = new MalformedIntentException("couldn't parse after Flag field. Token : " + errToken(sTuplelist.get(0).getInp()), 22);
			fTuplelist = fields(sTuplelist.get(0).getInp());

			if(fTuplelist.isEmpty() == false)
			{
				return sp.pReturn(sTuplelist.get(0).getValue() + fTuplelist.get(0).getValue(), fTuplelist.get(0).getInp());
			}
		}

		//e
		return sp.pReturn("", inp);
	}

	public String errToken(String inp)
	{
		StringTokenizer tokenizer = new StringTokenizer(inp, " ");
		if(tokenizer.hasMoreTokens())
		{ 
			return tokenizer.nextToken();
		}

		return "";
	}

	public ArrayList<Tuple<String>> action(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();
		String errmsg;

		sTuplelist = symbol("act", inp);
		if(sTuplelist.isEmpty())
		{
			return sp.failure();
		}
		errmsg = errToken(sTuplelist.get(0).getInp());

		sTuplelist = symbol("=", sTuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Action field. Token : " + errmsg, 9);
			//return sp.failure();
		}
		errmsg = errToken(sTuplelist.get(0).getInp());

		sTuplelist = idOrDot(sTuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Action field. Token : " + errmsg, 9);
			//return sp.failure();
		}

		if(intentData.getAction() != null)
		{
			throw new MalformedIntentException("Duplicated Action field.", 2);
		}
		intentData.setAction(sTuplelist.get(0).getValue());
		return sp.pReturn("act=" + sTuplelist.get(0).getValue() + " ", sTuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> category(String inp)
	{
		ArrayList<Tuple<String>> tmpTuplelist;
		ArrayList<Tuple<String>> s1Tuplelist;
		ArrayList<Tuple<String>> s2Tuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();
		String errmsg;

		tmpTuplelist = symbol("cat", inp);
		if(tmpTuplelist.isEmpty())
		{
			return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		tmpTuplelist = symbol("=", tmpTuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Category field. Token : " + errmsg, 11);
			//return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		tmpTuplelist = symbol("[", tmpTuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Category field. Token : " + errmsg, 11);
			//return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		s1Tuplelist = idOrDot(tmpTuplelist.get(0).getInp());
		if(s1Tuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Category field. Token : " + errmsg, 11);
			//return sp.failure();
		}

		if(intentData.lengthCategory() != 0)
		{
			throw new MalformedIntentException("Duplicated Category field.", 4);
		}
		intentData.addCategory(s1Tuplelist.get(0).getValue());

		s2Tuplelist = categorySub(s1Tuplelist.get(0).getInp());
		if(s2Tuplelist.isEmpty())
		{
			return sp.failure();
		}
		errmsg = errToken(s2Tuplelist.get(0).getInp());

		tmpTuplelist = symbol("]", s2Tuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Category field. Token : " + errmsg, 11);
			//return sp.failure();
		}

		return sp.pReturn("cat=[" + s1Tuplelist.get(0).getValue() + s2Tuplelist.get(0).getValue() + "] ", tmpTuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> categorySub(String inp)
	{
		ArrayList<Tuple<String>> tmpTuplelist;
		ArrayList<Tuple<String>> s1Tuplelist;
		ArrayList<Tuple<String>> s2Tuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		tmpTuplelist = symbol(",", inp);
		if(tmpTuplelist.isEmpty())
		{
			return sp.pReturn("", inp);
		}

		s1Tuplelist = idOrDot(tmpTuplelist.get(0).getInp());
		if(s1Tuplelist.isEmpty())
		{
			return sp.pReturn("", inp);
		}

		intentData.addCategory(s1Tuplelist.get(0).getValue());

		s2Tuplelist = categorySub(s1Tuplelist.get(0).getInp());

		return sp.pReturn(", " + s1Tuplelist.get(0).getValue() + s2Tuplelist.get(0).getValue(), s2Tuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> idata(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();
		String errmsg;

		sTuplelist = symbol("dat", inp);
		if(sTuplelist.isEmpty())
		{
			return sp.failure();
		}
		errmsg = errToken(sTuplelist.get(0).getInp());

		sTuplelist = symbol("=", sTuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Data field. Token : " + errmsg, 10);
			//return sp.failure();
		}
		errmsg = errToken(sTuplelist.get(0).getInp());

		sTuplelist = symbol("non-null", sTuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Data field. Token : " + errmsg, 10);
			//return sp.failure();
		}

		if(intentData.getData() != null)
		{
			throw new MalformedIntentException("Duplicated Data field.", 3);
		}

		intentData.setData(true);

		return sp.pReturn("dat=" + sTuplelist.get(0).getValue() + " ", sTuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> itype(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();
		String errmsg;

		sTuplelist = symbol("typ", inp);
		if(sTuplelist.isEmpty())
		{
			return sp.failure();
		}
		errmsg = errToken(sTuplelist.get(0).getInp());

		sTuplelist = symbol("=", sTuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Type field. Token : " + errmsg, 12);
			//return sp.failure();
		}
		errmsg = errToken(sTuplelist.get(0).getInp());

		sTuplelist = symbol("non-null", sTuplelist.get(0).getInp());
		if(sTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Type field. Token : " + errmsg, 12);
			//return sp.failure();
		}

		if(intentData.getType() != null)
		{
			throw new MalformedIntentException("Duplicated Type field.", 5);
		}
		intentData.setType(true);

		return sp.pReturn("typ=" + sTuplelist.get(0).getValue() + " ", sTuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> component(String inp)
	{
		ArrayList<Tuple<String>> tmpTuplelist;
		ArrayList<Tuple<String>> s1Tuplelist;
		ArrayList<Tuple<String>> s2Tuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();
		String errmsg;

		tmpTuplelist = symbol("cmp", inp);
		if(tmpTuplelist.isEmpty())
		{
			return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		tmpTuplelist = symbol("=", tmpTuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Component field. Token : " + errmsg, 13);
			//return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		s1Tuplelist = idOrDot(tmpTuplelist.get(0).getInp());
		if(s1Tuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Component field. Token : " + errmsg, 13);
			//return sp.failure();
		}
		errmsg = errToken(s1Tuplelist.get(0).getInp());
		
		String value = s1Tuplelist.get(0).getValue();
		for (IntentData.ComponentType ct : IntentData.ComponentType.values()) {
			if(ct.toString().equals(value)) {
				// Set the component type
				intentData.setComponentType(value);
				
				// and start parsing the component name again
				s1Tuplelist = idOrDot(s1Tuplelist.get(0).getInp());
				if(s1Tuplelist.isEmpty())
				{
					throw new MalformedIntentException("couldn't parse Component field. Token : " + errmsg, 13);
					//return sp.failure();
				}
				errmsg = errToken(s1Tuplelist.get(0).getInp());
				
				break;
			}
		}

		tmpTuplelist = symbol("/", s1Tuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Component field. Token : " + errmsg, 13);
			//return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		s2Tuplelist = idOrDot(tmpTuplelist.get(0).getInp());
		if(s2Tuplelist.isEmpty() == false)
		{
			if(intentData.getComponentPkg() != null || intentData.getComponentCls() != null)
			{
				throw new MalformedIntentException("Duplicated Component field.", 6);
			}

			intentData.setComponentPkg(s1Tuplelist.get(0).getValue());
			intentData.setComponentCls(s2Tuplelist.get(0).getValue());
			return sp.pReturn("cmp=" + s1Tuplelist.get(0).getValue() + "/" + s2Tuplelist.get(0).getValue() + " ", s2Tuplelist.get(0).getInp());
		}

		tmpTuplelist = symbol(".", tmpTuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Component field. Token : " + errmsg, 13);
			//return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		s2Tuplelist = idOrDot(tmpTuplelist.get(0).getInp());
		if(s2Tuplelist.isEmpty() == false)
		{

			if(intentData.getComponentPkg() != null || intentData.getComponentCls() != null)
			{
				throw new MalformedIntentException("Duplicated Component field.", 6);
			}

			intentData.setComponentPkg(s1Tuplelist.get(0).getValue());
			intentData.setComponentCls(s1Tuplelist.get(0).getValue() + "." + s2Tuplelist.get(0).getValue());
			return sp.pReturn("cmp=" + s1Tuplelist.get(0).getValue() + "/" + s1Tuplelist.get(0).getValue() + "." + s2Tuplelist.get(0).getValue() + " ", s2Tuplelist.get(0).getInp());
		}

		throw new MalformedIntentException("couldn't parse Component field. Token : " + errmsg, 13);
		//return sp.failure();
	}

	public ArrayList<Tuple<String>> extra(String inp)
	{
		ArrayList<Tuple<String>> tmpTuplelist;
		ArrayList<Tuple<String>> s1Tuplelist;
		ArrayList<Tuple<String>> sTypeTuplelist;
		ArrayList<Tuple<String>> s2Tuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();
		String errmsg;

		tmpTuplelist = symbol("[", inp);
		if(tmpTuplelist.isEmpty())
		{
			return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		s1Tuplelist = idOrDot(tmpTuplelist.get(0).getInp());
		if(s1Tuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Extra field. Token : " + errmsg, 14);
			//return sp.failure();
		}
		errmsg = errToken(s1Tuplelist.get(0).getInp());

		tmpTuplelist = symbol("=", s1Tuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Extra field. Token : " + errmsg, 14);
			//return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		sTypeTuplelist = idOrDot(tmpTuplelist.get(0).getInp());
		if(sTypeTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Extra field. Token : " + errmsg, 14);
			//return sp.failure();
		}

		//tmpTuplelist = symbol("[]", sTypeTuplelist.get(0).getInp());
		tmpTuplelist = arr(sTypeTuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty() == false)
		{
			sTypeTuplelist.get(0).setTuple(sTypeTuplelist.get(0).getValue() + tmpTuplelist.get(0).getValue(), tmpTuplelist.get(0).getInp());
		}

		if(intentData.lengthExtra() != 0)
		{
			throw new MalformedIntentException("Duplicated Extra field.", 7);
		}
		intentData.addExtra(new KeyTypePair(s1Tuplelist.get(0).getValue(), sTypeTuplelist.get(0).getValue()));

		s2Tuplelist = extraSub(sTypeTuplelist.get(0).getInp());
		if(s2Tuplelist.isEmpty())
		{
			return sp.failure();
		}
		errmsg = errToken(s2Tuplelist.get(0).getInp());

		tmpTuplelist = symbol("]", s2Tuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Extra field. Token : " + errmsg, 14);
			//return sp.failure();
		}

		return sp.pReturn("[" + s1Tuplelist.get(0).getValue() + "=" + sTypeTuplelist.get(0).getValue() + s2Tuplelist.get(0).getValue() + "] ", tmpTuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> extraSub(String inp)
	{
		ArrayList<Tuple<String>> tmpTuplelist;
		ArrayList<Tuple<String>> s1Tuplelist;
		ArrayList<Tuple<String>> sTypeTuplelist;
		ArrayList<Tuple<String>> s2Tuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();
		String errmsg;

		tmpTuplelist = symbol(",", inp);
		if(tmpTuplelist.isEmpty())
		{
			return sp.pReturn("", inp);
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		s1Tuplelist = idOrDot(tmpTuplelist.get(0).getInp());
		if(s1Tuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Extra field. Token : " + errmsg, 14);
			//return sp.failure();
		}
		errmsg = errToken(s1Tuplelist.get(0).getInp());

		tmpTuplelist = symbol("=", s1Tuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Extra field. Token : " + errmsg, 14);
			//return sp.failure();
		}
		errmsg = errToken(tmpTuplelist.get(0).getInp());

		sTypeTuplelist = idOrDot(tmpTuplelist.get(0).getInp());
		if(sTypeTuplelist.isEmpty())
		{
			throw new MalformedIntentException("couldn't parse Extra field. Token : " + errmsg, 14);
			//return sp.failure();
		}

		//tmpTuplelist = symbol("[]", sTypeTuplelist.get(0).getInp());
		tmpTuplelist = arr(sTypeTuplelist.get(0).getInp());
		if(tmpTuplelist.isEmpty() == false)
		{
			sTypeTuplelist.get(0).setTuple(sTypeTuplelist.get(0).getValue() + tmpTuplelist.get(0).getValue(), tmpTuplelist.get(0).getInp());
		}

		intentData.addExtra(new KeyTypePair(s1Tuplelist.get(0).getValue(), sTypeTuplelist.get(0).getValue()));

		s2Tuplelist = extraSub(sTypeTuplelist.get(0).getInp());

		return sp.pReturn(", " + s1Tuplelist.get(0).getValue() + s2Tuplelist.get(0).getValue(), s2Tuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> flag(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		sTuplelist = symbol("flg", inp);
		if(sTuplelist.isEmpty())
		{
			return sp.failure();
		}

		if(intentData.getFlag() != null)
		{
			throw new MalformedIntentException("Duplicated Flag field.", 8);
		}
		intentData.setFlag(true);

		return sp.pReturn("flg ", sTuplelist.get(0).getInp());
	}

	public ArrayList<Tuple<String>> arr(String inp)
	{
		ArrayList<Tuple<String>> a1Tuplelist;
		ArrayList<Tuple<String>> a2Tuplelist;

		ParserOnIntent<String> sp = new ParserOnIntent<String>();

		a1Tuplelist = symbol("[]", inp);
		if(a1Tuplelist.isEmpty())
		{
			return sp.pReturn("", inp);
		}

		a2Tuplelist = arr(a1Tuplelist.get(0).getInp());

		return sp.pReturn(a1Tuplelist.get(0).getValue() + a2Tuplelist.get(0).getValue(), a2Tuplelist.get(0).getInp());
	}

	public IntentDataset parse(String inp)
	{
		ArrayList<Tuple<String>> sTuplelist;

		sTuplelist = intent(inp);

		if(sTuplelist.isEmpty())
		{
			return null;
		}

		if(sTuplelist.get(0).getInp().length() != 0)
		{
			return null;
		}

		return intentDataset;
	}

	public String exceptionIntentNumberMsg()
	{
		return "IntentNumber : " + intentDataset.length();
	}
}









