module GenAndroidTestCode where

import Language.Java.Syntax
import Language.Java.Pretty
import Language.Java.Parser
import Language.Java.Lexer

import IntentSpec

genAndroidTestCode :: Int -> String -> IntentSpec -> [String] -> IO ()
genAndroidTestCode 0 intentspec intents args = genActivityTestCode intents args
genAndroidTestCode 1 intentspec intents args = genBroadcastReceiverTestCode intents args
genAndroidTestCode 2 intentspec intents args = genServiceTestCode intents args

{--------------------------------------------------------------------------------------- 
A sample test code for Activity

입력: 
   1) 패키지 이름 : PKG_NAME (e.g., com.example.khchoi.helloandroid) 
   2) 테스트 대상 Activity 클래스 이름 : CLASS_NAME  (e.g., MainActivity)
   3) 디렉토리 위치 : PROJECT_HOME  (e.g., D:\khcohi\Android\AndroidStudioProjects\HelloAndroid)

출력:
   1) 테스트 클래스 이름 : TEST_CLASS_NAME (e.g., ${CLASS_NAME}TEST)
   2) 파일 : ${PROJECT_HOME}\app\src\androidTest\java\패키지명에 해당하는 경로{com\example\khchoi\helloandroid}\${TEST_CLASS_NAME}.java
   3) 파일 내용

// MainActivityTest_00001.java
   
package ${PKG_NAME};

import android.content.Intent;
import android.test.ActivityUnitTestCase;
... 추가 필요 ...

public class ${TEST_CLASS_NAME} extends ActivityUnitTestCase<${CLASS_NAME}> {
    public ${TEST_CLASS_NAME}() {
        super(${CLASS_NAME}.class);
    }
    
    // 이 코드를 생성할 때 사용한 인텐트 스펙을 주석으로 표시

    public void test_NNNNN() {
        Intent activityIntent =  new Intent();
        activityIntent.setClassName("${PKG_NAME}",
                "${PKG_NAME}.${CLASS_NAME}");

        activityIntent.setAction("my action");
        ... 주어진 intent에 따라 적절한 코드 생성 ...
        startActivity(activityIntent, null, null);
    }
    
    // ... 생성할 인텐트 개수만큼 메소드를 나열
}
   

---------------------------------------------------------------------------------------}

genActivityTestCode :: IntentSpec -> [String] -> IO ()
genActivityTestCode is args =
    genActivityTestCode' pkg_name test_class_name class_name prj_home is
    where pkg_name   = args !! 0
          class_name = args !! 1
          prj_home   = args !! 2
          suffix     = args !! 3
          _suffix    = if suffix == "" then suffix else "_" ++ suffix
          test_class_name = class_name ++ "Test" ++ _suffix
          
          
genActivityTestCode' pkg_name test_class_name class_name prj_home is = do
    putStrLn $ prettyPrint $ compUnit
    mapM_ (putStrLn . show) is
    where
        pkg_name_ids = map Ident (pkgnameToIds pkg_name)
        compUnit = CompilationUnit (Just $ PackageDecl $ Name pkg_name_ids)
                    [ ImportDecl False (Name [Ident "android", Ident "content", Ident "Intent"]) False
                    , ImportDecl False (Name [Ident "android", Ident "content", Ident "ComponentName"]) False
                    , ImportDecl False (Name [Ident "android", Ident "test", Ident "ActivityUnitTestCase"]) False
                    , ImportDecl False (Name [Ident "android", Ident "net", Ident "Uri"]) False
                    , ImportDecl False (Name [Ident "java", Ident "net", Ident "URI"]) False
                    ]
                    [ClassTypeDecl (ClassDecl [Public] (Ident test_class_name) [] 
                        (Just (ClassRefType (ClassType [(Ident "ActivityUnitTestCase",
                                    [ActualType (ClassRefType (ClassType [(Ident class_name, [])]))])]))) [] 
                        (ClassBody (constr : genActivityTestMethodCode pkg_name test_class_name class_name is)))]
        constr = MemberDecl (ConstructorDecl [Public] [] (Ident test_class_name) [] []
                        (ConstructorBody (Just (SuperInvoke [] 
                            [ClassLit (Just (RefType (ClassRefType (ClassType [(Ident class_name, [])]))))] )) []))
                            
genActivityTestMethodCode pkg_name test_class_name class_name is = map testMethod $ zip is [1..]
        
locVarintent = "intent"
        
testMethod (fields, num) =
    MemberDecl 
        (MethodDecl [Public] [] Nothing (Ident ("testcase" ++ show num)) [] [] 
            (MethodBody (Just (Block $ mkTrycatch $
                [
                    LocalVars [] (RefType (ClassRefType (ClassType [(Ident "Intent",[])]))) 
                        [VarDecl (VarId (Ident locVarintent)) 
                        (Just (InitExp (InstanceCreation [] (ClassType [(Ident "Intent",[])]) [] Nothing)))]
                ]  ++  concat (map testStmt fields)
                   ++  [ startactivity ] 
                ))) )
                
    where
        startactivity = BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident "startActivity"]) 
                            [ExpName (Name [Ident locVarintent]),Lit Null,Lit Null]))) 
                            
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
testStmt (Data uri) = 
     [BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "setData"]) 
        [MethodInv (MethodCall (Name [Ident "Uri",Ident "parse"]) [Lit (String uri)])])))]
testStmt (Type t) =
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "setType"]) 
        [Lit (String t)]))) ]
testStmt (Component pkg pkgclz) = 
    [ BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "setClassName"]) 
        [Lit (String pkg), Lit (String $ pkg ++ pkgclz)]))) ]
testStmt (Extra extras) = concat $ map testStmtExtra extras
testStmt (Flag flags) = [ ]
--    map (\flag -> 
--        BlockStmt (ExpStmt (MethodInv (MethodCall (Name [Ident locVarintent,Ident "setFlags"]) 
--            [Lit (String flag)])))) flags


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
testStmtExtra (key, UriType uri) =
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

        
genBroadcastReceiverTestCode :: IntentSpec -> [String] -> IO ()
genBroadcastReceiverTestCode is args = putStrLn "*** Not support for generating Broadcast Receiver test code"

{- A sample test code for Service

// MyServiceTest_00001.java

package com.example.khchoi.helloandroid;

import android.content.Intent;
import android.test.ServiceTestCase;

public class MyServiceTest extends ServiceTestCase<MyService> {
    public MyServiceTest() {
        super(MyService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupService();
    }

    public void testActivatingMyService() throws Exception {
        Intent serviceIntent = new Intent(getContext(), MyService.class);

        serviceIntent.setAction("my action");
        startService(serviceIntent);
    }

    @Override
    public void tearDown() throws Exception {
        shutdownService(); 
        super.tearDown();
    }
}


-}

genServiceTestCode :: IntentSpec -> [String] -> IO ()
genServiceTestCode is args = putStrLn ""

-- Utility Functions

pkgnameToIds :: String -> [String]
pkgnameToIds []       = []
pkgnameToIds pkg_name = h : pkgnameToIds t  
    where
        h = takeWhile (/= '.') pkg_name
        t = dropWhile (== '.') $ drop (length h) pkg_name
        
---
test = case parser compilationUnit "import java.util.*; public class MyClass extends Object { public MyClass() { super(MainActivity.class); } }" of
        Left err -> putStrLn (show err)
        Right cu -> putStrLn $ (show cu)
test1 = case parser compilationUnit "public class MyClass { public void test1() { try {  } catch(Throwable t) { } } }" of
        Left err -> putStrLn (show err)
        Right cu -> putStrLn $ (show cu)