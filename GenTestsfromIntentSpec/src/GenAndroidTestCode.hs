module GenAndroidTestCode where

import Language.Java.Syntax
import Language.Java.Pretty
import Language.Java.Parser
import Language.Java.Lexer

import IntentSpec

genAndroidTestCode :: String -> IntentSpec -> [String] -> IO ()
genAndroidTestCode intentspec intents args = mapM_ (f args) (zip [1..] intents)
    where
        f :: [String] -> (Int, Intent) -> IO ()
        f args (n, intent) = genAndroidTestCode' (show n) args intent intent 
        
genAndroidTestCode' suffix args intent []                                            
    = return () 
genAndroidTestCode' suffix args intent (Component (Just Activity)          pkg clz : xs) 
    = genActivityTestCode pkg' clz' suffix test_pkg_name intent
    where
      (pkg', clz') = getPkgClz pkg clz
      test_pkg_name = args !! 0
genAndroidTestCode' suffix args intent (Component (Just Service)           pkg clz : xs) 
    = genServiceTestCode  pkg' clz' suffix test_pkg_name intent
    where
      (pkg', clz') = getPkgClz pkg clz
      test_pkg_name   = args !! 0
genAndroidTestCode' suffix args intent (Component (Just BroadcastReceiver) pkg clz : xs) 
    = return ()
genAndroidTestCode' suffix args intent (Component Nothing                  pkg clz : xs) 
    =  genActivityTestCode pkg' clz' suffix test_pkg_name intent
    where
      (pkg', clz') = getPkgClz pkg clz
      test_pkg_name = args !! 0
genAndroidTestCode' suffix args intent (_                                      : xs) 
    = genAndroidTestCode' suffix args intent xs 

{--------------------------------------------------------------------------------------- 
A sample test code for Activity

?엯?젰: 
   1) ?뙣?궎吏? ?씠由? : PKG_NAME (e.g., com.example.khchoi.helloandroid) 
   2) ?뀒?뒪?듃 ???긽 Activity ?겢?옒?뒪 ?씠由? : CLASS_NAME  (e.g., MainActivity)
   3) ?뵒?젆?넗由? ?쐞移? : PROJECT_HOME  (e.g., D:\khcohi\Android\AndroidStudioProjects\HelloAndroid)
   4) SUFFIX (e.g., 0001)

異쒕젰:
   1) ?뀒?뒪?듃 ?겢?옒?뒪 ?씠由? : TEST_CLASS_NAME (e.g., ${CLASS_NAME}TEST_${SUFFIX})
   2) ?뙆?씪 : ${PROJECT_HOME}\app\src\androidTest\java\?뙣?궎吏?紐낆뿉 ?빐?떦?븯?뒗 寃쎈줈{com\example\khchoi\helloandroid}\${TEST_CLASS_NAME}.java
   3) ?뙆?씪 ?궡?슜

// MainActivityTest_00001.java
   
package ${PKG_NAME};

import android.content.Intent;
import android.test.ActivityUnitTestCase;
... 異붽? ?븘?슂 ...

public class ${TEST_CLASS_NAME} extends ActivityUnitTestCase<${CLASS_NAME}> {
    public ${TEST_CLASS_NAME}() {
        super(${CLASS_NAME}.class);
    }
    
    // ?씠 肄붾뱶瑜? ?깮?꽦?븷 ?븣 ?궗?슜?븳 ?씤?뀗?듃 ?뒪?럺?쓣 二쇱꽍?쑝濡? ?몴?떆

    public void test_NNNNN() {
        Intent activityIntent =  new Intent();
        activityIntent.setClassName("${PKG_NAME}",
                "${PKG_NAME}.${CLASS_NAME}");

        activityIntent.setAction("my action");
        ... 二쇱뼱吏? intent?뿉 ?뵲?씪 ?쟻?젅?븳 肄붾뱶 ?깮?꽦 ...
        startActivity(activityIntent, null, null);
    }
    
    // ... ?깮?꽦?븷 ?씤?뀗?듃 媛쒖닔留뚰겮 硫붿냼?뱶瑜? ?굹?뿴
}
   

---------------------------------------------------------------------------------------}

genActivityTestCode :: String -> String -> String -> String -> Intent -> IO ()
genActivityTestCode pkg_name class_name suffix test_pkg_name intent = do
    compUnit <- genActivityTestCode' pkg_name class_name test_pkg_name test_class_name intent
    -- writeFile outputPath (prettyPrint compUnit)
    putStrLn $ "#" ++ outputPath
    putStrLn $ prettyPrint $ compUnit
    putStrLn " "
    where path            = pkgnameToPath test_pkg_name ++ "/"
          test_class_name = class_name ++ "Test" ++ _suffix
          outputPath      = path ++ test_class_name ++ ".java"  
          _suffix         = if suffix == "" then suffix else "_" ++ suffix
          
genActivityTestCode' pkg_name class_name test_pkg_name test_class_name  intent = do
    return compUnit
    where
        pkg_name_ids      = map Ident (pkgnameToIds pkg_name)
        test_pkg_name_ids = map Ident (pkgnameToIds test_pkg_name)
        compUnit = CompilationUnit (Just $ PackageDecl $ Name test_pkg_name_ids)
                    [ ImportDecl False (Name [Ident "android", Ident "content", Ident "Intent"]) False
                    , ImportDecl False (Name [Ident "android", Ident "content", Ident "ComponentName"]) False
                    , ImportDecl False (Name [Ident "android", Ident "test", Ident "ActivityUnitTestCase"]) False
                    , ImportDecl False (Name [Ident "android", Ident "net", Ident "Uri"]) False
                    , ImportDecl False (Name [Ident "java", Ident "net", Ident "URI"]) False
                    , ImportDecl False (Name $ pkg_name_ids ++ [Ident class_name]) False
                    ]
                    [ClassTypeDecl (ClassDecl [Public] (Ident test_class_name) [] 
                        (Just (ClassRefType (ClassType [(Ident "ActivityUnitTestCase",
                                    [ActualType (ClassRefType (ClassType [(Ident class_name, [])]))])]))) [] 
                        (ClassBody (constr : genActivityTestMethodCode pkg_name test_class_name class_name intent)))]
        constr = MemberDecl (ConstructorDecl [Public] [] (Ident test_class_name) [] []
                        (ConstructorBody (Just (SuperInvoke [] 
                            [ClassLit (Just (RefType (ClassRefType (ClassType [(Ident class_name, [])]))))] )) []))
                            
genActivityTestMethodCode pkg_name test_class_name class_name intent = 
    map (\pair -> testMethod pair startactivity) $ zip [intent] [1..]
    where
        startactivity = BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident "startActivity"]) 
                            [ExpName (Name [Ident locVarintent]),Lit Null,Lit Null]))) 
        
locVarintent = "intent"
        
testMethod :: ([Field], Int) -> BlockStmt -> Decl        
testMethod (fields, num) activation =
    MemberDecl 
        (MethodDecl [Public] [] Nothing (Ident ("testcase" ++ show num)) [] [] 
            (MethodBody (Just (Block $ 
                [
                    LocalVars [] (RefType (ClassRefType (ClassType [(Ident "Intent",[])]))) 
                        [VarDecl (VarId (Ident locVarintent)) 
                        (Just (InitExp (InstanceCreation [] (ClassType [(Ident "Intent",[])]) [] Nothing)))]
                ]  ++  concat (map testStmt fields)
                   ++  [ activation ] 
                ))) )

                            
mkTrycatch :: [BlockStmt] -> [BlockStmt]
mkTrycatch blockstmts = 
    [ BlockStmt (Try (Block blockstmts) 
        [Catch (FormalParam [] (RefType (ClassRefType (ClassType [(Ident "Throwable",[])]))) False (VarId (Ident "t")))
            (Block [])] Nothing) ]                             

testStmt :: Field -> [BlockStmt]
testStmt (Action action) = 
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "setAction"]) 
        [Lit (String action)]))) ]
testStmt (Category categories) = 
    map (\category -> 
        BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "addCategory"]) 
            [Lit (String category)])))) categories
testStmt (Data uri) = mkTrycatch
     [BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "setData"]) 
        [MethodInv (MethodCall (Name [Ident "Uri",Ident "parse"]) [Lit (String uri)])])))]
testStmt (Type t) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "setType"]) 
        [Lit (String t)]))) ]
testStmt (Component comptype pkg clz) = 
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "setClassName"]) 
        [Lit (String pkg), Lit (String $ getFullClz pkg clz)]))) ]
testStmt (Extra extras) = concat $ map testStmtExtra extras
testStmt (Flag flags) = [ ]
--    map (\flag -> 
--        BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "setFlags"]) 
--            [Lit (String flag)])))) flags
testStmt (Internal _) = [ ]


testStmtExtra :: (String, ExtraType) -> [BlockStmt]
testStmtExtra (key, StringType string) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "putExtra"]) 
        [Lit (String key), Lit (String string)]))) ]
testStmtExtra (key, BooleanType bool) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "putExtra"]) 
        [Lit (String key), Lit (Boolean bool)]))) ]
testStmtExtra (key, IntegerType int) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "putExtra"]) 
        [Lit (String key), Lit (Int (read $ show $ int))]))) ]
testStmtExtra (key, LongType long) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "putExtra"]) 
        [Lit (String key), Lit (Int long)]))) ]
testStmtExtra (key, FloatType float) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "putExtra"]) 
        [Lit (String key), Lit (Float (read $ show $ float))]))) ]
testStmtExtra (key, UriType uri) = mkTrycatch
    [BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident "intent",Ident "putExtra"]) 
        [Lit (String key),
         MethodInv (MethodCall (Name [Ident "URI",Ident "create"]) [Lit (String uri)])])))]
testStmtExtra (key, ComponentType pkg pkgclz) =
    [BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "putExtra"]) 
        [Lit (String "key"),
         InstanceCreation [] (ClassType [(Ident "ComponentName",[])])
            [Lit (String pkg),Lit (String $ pkg ++ pkgclz)] Nothing])))] 
testStmtExtra (key, IntArray ints) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "putExtra"])
        [Lit (String key), 
            ArrayCreateInit (PrimType IntT) 1 
                ( ArrayInit $  map (\int -> InitExp $ Lit $ Int (read $ show $ int)) ints )])))]
testStmtExtra (key, LongArray integers) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "putExtra"])
        [Lit (String key), 
            ArrayCreateInit (PrimType IntT) 1 
                ( ArrayInit $  map (\integer -> InitExp $ Lit $ Int integer) integers )])))]
testStmtExtra (key, FloatArray floats) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "putExtra"])
        [Lit (String key), 
            ArrayCreateInit (PrimType FloatT) 1 
                ( ArrayInit $  map (\float -> InitExp $ Lit $ Float (read $ show $ float)) floats )])))]

---------------------------------------------------------------------------------------
        
genBroadcastReceiverTestCode :: [String] -> IO ()
genBroadcastReceiverTestCode args = putStrLn "*** Not support for generating Broadcast Receiver test code"

{---------------------------------------------------------------------------------------  
?엯?젰: 
   1) ?뙣?궎吏? ?씠由? : PKG_NAME (e.g., com.example.khchoi.helloandroid) 
   2) ?뀒?뒪?듃 ???긽 Service ?겢?옒?뒪 ?씠由? : CLASS_NAME  (e.g., MyService)
   3) ?뵒?젆?넗由? ?쐞移? : PROJECT_HOME  (e.g., D:\khcohi\Android\AndroidStudioProjects\HelloAndroid)
   4) SUFFIX (e.g., 0001)

異쒕젰:
   1) ?뀒?뒪?듃 ?겢?옒?뒪 ?씠由? : TEST_CLASS_NAME (e.g., ${CLASS_NAME}TEST_${SUFFIX})
   2) ?뙆?씪 : ${PROJECT_HOME}\app\src\androidTest\java\?뙣?궎吏?紐낆뿉 ?빐?떦?븯?뒗 寃쎈줈{com\example\khchoi\helloandroid}\${TEST_CLASS_NAME}.java
   3) ?뙆?씪 ?궡?슜

// MyServiceTest_00001.java

package ${PKG_NAME};

import android.content.Intent;
import android.test.ServiceTestCase;
... 異붽? ?븘?슂 ...

public class ${TEST_CLASS_NAME} extends ServiceTestCase<${CLASS_NAME}> {
    public ${TEST_CLASS_NAME}() {
        super(${CLASS_NAME}.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupService();
    }

    public void test_NNNNN() throws Exception {
        Intent serviceIntent = new Intent();
        serviceIntent.setClassName("${PKG_NAME}","${PKG_NAME}.${CLASS_NAME}");
        serviceIntent.setAction("my action");
        ... 二쇱뼱吏? intent?뿉 ?뵲?씪 ?쟻?젅?븳 肄붾뱶 ?깮?꽦 ... 
        startService(serviceIntent);
    }

    @Override
    public void tearDown() throws Exception {
        shutdownService(); 
        super.tearDown();
    }
}


---------------------------------------------------------------------------------------}

genServiceTestCode :: String -> String -> String -> String -> Intent -> IO ()
genServiceTestCode pkg_name class_name suffix test_pkg_name intent = do
    compUnit <- genServiceTestCode' pkg_name class_name test_pkg_name test_class_name intent
    -- writeFile outputPath (prettyPrint compUnit)
    putStrLn $ "#" ++ outputPath
    putStrLn $ prettyPrint $ compUnit
    -- mapM_ (putStrLn . show) is
    where path            = pkgnameToPath test_pkg_name ++ "/"
          test_class_name = class_name ++ "Test" ++ _suffix
          outputPath      = path ++ test_class_name ++ ".java"         
          _suffix         = if suffix == "" then suffix else "_" ++ suffix

genServiceTestCode' pkg_name class_name test_pkg_name test_class_name intent = do
    return compUnit
    where
        pkg_name_ids      = map Ident (pkgnameToIds pkg_name)
        test_pkg_name_ids = map Ident (pkgnameToIds test_pkg_name)
        
        compUnit = CompilationUnit (Just $ PackageDecl $ Name test_pkg_name_ids)
                    [ ImportDecl False (Name [Ident "android", Ident "content", Ident "Intent"]) False
                    , ImportDecl False (Name [Ident "android", Ident "content", Ident "ComponentName"]) False
                    , ImportDecl False (Name [Ident "android", Ident "test", Ident "ServiceTestCase"]) False
                    , ImportDecl False (Name [Ident "android", Ident "net", Ident "Uri"]) False
                    , ImportDecl False (Name [Ident "java", Ident "net", Ident "URI"]) False
                    , ImportDecl False (Name $ pkg_name_ids ++ [Ident class_name]) False
                    ]
                    [ClassTypeDecl (ClassDecl [Public] (Ident test_class_name) [] 
                        (Just (ClassRefType (ClassType [(Ident "ServiceTestCase",
                                    [ActualType (ClassRefType (ClassType [(Ident class_name, [])]))])]))) [] 
                        (ClassBody (constr : setup : teardown 
                                        : genServiceTestMethodCode pkg_name test_class_name class_name intent)))]
        constr = MemberDecl (ConstructorDecl [Public] [] (Ident test_class_name) [] []
                        (ConstructorBody (Just (SuperInvoke [] 
                            [ClassLit (Just (RefType (ClassRefType (ClassType [(Ident class_name, [])]))))] )) []))
        setup = MemberDecl (MethodDecl [Public] [] Nothing (Ident "setUp") [] 
                        [ClassRefType (ClassType [(Ident "Exception",[])])] 
                        (MethodBody (Just (Block 
                            [BlockStmt (ExpStmt (MethodInv (SuperMethodCall [] (Ident "setUp") []))),
                             BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident "setupService"]) [])))]))))
        teardown = MemberDecl (MethodDecl [Public] [] Nothing (Ident "tearDown") [] 
                        [ClassRefType (ClassType [(Ident "Exception",[])])] 
                        (MethodBody (Just (Block 
                            [BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident "shutdownService"]) []))),
                             BlockStmt (ExpStmt (MethodInv (SuperMethodCall [] (Ident "tearDown") []))) ] ))))
          
genServiceTestMethodCode pkg_name test_class_name class_name intent = 
    map (\pair -> testMethod pair startservice) $ zip [intent] [1..]
    where
        startservice = BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident "startService"]) 
                            [ExpName (Name [Ident locVarintent])]))) 
                            
-- Utility Functions

--pkgnameToIds :: String -> [String]
--pkgnameToIds []       = []
--pkgnameToIds pkg_name = h : pkgnameToIds t  
--    where
--        h = takeWhile (/= '.') pkg_name
--        t = dropWhile (== '.') $ drop (length h) pkg_name
--        
--pkgnameToPath pkg_name = f (pkgnameToIds pkg_name)
--    where
--        f []     = ""
--        f [x]    = x
--        f (x:xs) = x ++ "/" ++ f xs        
        
---
test = case parser compilationUnit "import java.util.*; public class MyClass extends Object { public MyClass() { super(MainActivity.class); } }" of
        Left err -> putStrLn (show err)
        Right cu -> putStrLn $ (show cu)
test1 = case parser compilationUnit "public class MyClass { public void setUp() throws Exception { super.setUp(); setupService(); } }" of
        Left err -> putStrLn (show err)
        Right cu -> putStrLn $ (show cu)