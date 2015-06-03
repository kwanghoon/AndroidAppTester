package kr_ac_yonsei_mobilesw_UI;

public enum AnalyzeResult {
	Normal, Exit, ErrorExit, 
	IntentSpecCatchAndNormal, IntentSpecCatchAndExit, IntentSpecCatchAndErrorExit,
	IntentSpecPassAndNormal, IntentSpecPassAndExit, IntentSpecPassAndErrorExit,
	CantAnalyze;
}
