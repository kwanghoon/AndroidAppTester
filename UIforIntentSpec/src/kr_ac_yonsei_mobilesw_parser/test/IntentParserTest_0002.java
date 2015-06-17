package kr_ac_yonsei_mobilesw_parser.test;

import static org.junit.Assert.*;
import kr_ac_yonsei_mobilesw_parser.IntentDataset;
import kr_ac_yonsei_mobilesw_parser.ParserOnIntent;

import org.junit.Test;

public class IntentParserTest_0002 {

	@Test
	public void test() {
		ParserOnIntent parser = new ParserOnIntent();
		IntentDataset set = parser.parse("{ cmp=Activity com.example.android/com.example.android.MyClass$MainActivity }");
		
		assert(set.length()==1);
		assert(set.getIntent("com.example.android/.MainActivity")
				.getComponentType().equals("Activity"));
	}
}
