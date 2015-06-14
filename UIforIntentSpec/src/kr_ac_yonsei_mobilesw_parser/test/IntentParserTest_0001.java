package kr_ac_yonsei_mobilesw_parser.test;

import static org.junit.Assert.*;
import kr_ac_yonsei_mobilesw_parser.IntentDataset;
import kr_ac_yonsei_mobilesw_parser.MalformedIntentException;
import kr_ac_yonsei_mobilesw_parser.ParserOnIntent;

import org.junit.Test;


public class IntentParserTest_0001 {

	/*
	 * [test_0001 ~ test_0012]
	 * 
	 * Extension of the syntax of Intent specification language
	 * 
	 *  1. Add the type of a component to the syntax of cmp field 
	 * 
	 *   cmp=com.example.android/.MainActivity
	 *   
	 *   =>
	 *   
	 *   cmp=Activity com.example.android/.MainActivity
	 *   
	 *   cmp=com.example.android/.MainActivity
	 *   
	 *   
	 *  2. The possible types of Android components are 
	 *  
	 *    Activity
	 *    Service
	 *    BroadcastReceiver
	 *    ContentProvider   (for later uses)
	 *   
	 */
	@Test
	public void test_0001() {
		ParserOnIntent parser = new ParserOnIntent();
		IntentDataset set = parser.parse("{ cmp=Activity com.example.android/.MainActivity }");
		
		assert(set.length()==1);
		assert(set.getIntent("com.example.android/.MainActivity")
				.getComponentType().equals("Activity"));
	}
	
	@Test
	public void test_0002() {
		ParserOnIntent parser = new ParserOnIntent();
		IntentDataset set = parser.parse("{ cmp=com.example.android/.MainActivity }");
		
		assert(set.length()==1);
		assert(set.getIntent("com.example.android/.MainActivity")
				.getComponentType().equals("Activity"));
	}
	
	@Test
	public void test_0003() {
		try {
			ParserOnIntent parser = new ParserOnIntent();
			IntentDataset set = parser.parse("{ cmp=Activity }");
		}
		catch(MalformedIntentException exn) {
			assert(true);
		}
	}
	@Test
	public void test_0004() {
		ParserOnIntent parser = new ParserOnIntent();
		IntentDataset set = parser.parse("{ cmp=Service com.example.android/.MainService }");
		
		assert(set.length()==1);
		assert(set.getIntent("com.example.android/.MainService")
				.getComponentType().equals("Service"));
	}
	
	@Test
	public void test_0005() {
		ParserOnIntent parser = new ParserOnIntent();
		IntentDataset set = parser.parse("{ cmp=com.example.android/.MainService }");
		
		assert(set.length()==1);
		assert(set.getIntent("com.example.android/.MainService")
				.getComponentType().equals("Service"));
	}
	
	@Test
	public void test_0006() {
		try {
			ParserOnIntent parser = new ParserOnIntent();
			IntentDataset set = parser.parse("{ cmp=Service }");
		}
		catch(MalformedIntentException exn) {
			assert(true);
		}
	}
	@Test
	public void test_0007() {
		ParserOnIntent parser = new ParserOnIntent();
		IntentDataset set = parser.parse("{ cmp=BroadcastReceiver com.example.android/.MyReceiver }");
		
		assert(set.length()==1);
		assert(set.getIntent("com.example.android/.MyReceiver")
				.getComponentType().equals("BroadcastReceiver"));
	}
	
	@Test
	public void test_0008() {
		ParserOnIntent parser = new ParserOnIntent();
		IntentDataset set = parser.parse("{ cmp=com.example.android/.MyReceiver }");
		
		assert(set.length()==1);
		assert(set.getIntent("com.example.android/.MyReceiver")
				.getComponentType().equals("BroadcastReceiver"));
	}
	
	@Test
	public void test_0009() {
		try {
			ParserOnIntent parser = new ParserOnIntent();
			IntentDataset set = parser.parse("{ cmp=BroadcastReceiver }");
		}
		catch(MalformedIntentException exn) {
			assert(true);
		}
	}
	@Test
	public void test_0010() {
		ParserOnIntent parser = new ParserOnIntent();
		IntentDataset set = parser.parse("{ cmp=ContentProvider com.example.android/.MyProvider }");
		
		assert(set.length()==1);
		assert(set.getIntent("com.example.android/.MyProvider")
				.getComponentType().equals("ContentProvider"));
	}
	
	@Test
	public void test_0011() {
		ParserOnIntent parser = new ParserOnIntent();
		IntentDataset set = parser.parse("{ cmp=com.example.android/.MyReceiver }");
		
		assert(set.length()==1);
		assert(set.getIntent("com.example.android/.MyProvider")
				.getComponentType().equals("ContentProvider"));
	}
	
	@Test
	public void test_0012() {
		try {
			ParserOnIntent parser = new ParserOnIntent();
			IntentDataset set = parser.parse("{ cmp=ContentProvider }");
		}
		catch(MalformedIntentException exn) {
			assert(true);
		}
	}

}
