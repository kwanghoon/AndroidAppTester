
import Test.QuickCheck
import Test.QuickCheck.Gen
import Test.QuickCheck.Random
import Test.QuickCheck.Instances.Tuple
import Data.List

import System.Random
import System.IO
import System.IO.Unsafe
import System.Directory
import System.Environment

import ActionElements
import CategoryElements
import DataElements
import TypeElements 
import ComponentsElements
import ExtraElements
import FlagElements

--cabal install quickCheck  --> 2.8
--cabal install checkers    --> 0.4.2

import Data.Char

import Control.Monad 

import IntentSpec
import GenAndroidTestCode
  
import Control.Applicative -- Otherwise you can't do the Applicative instance.


infixr 5 +++

--The monad of parsers
--------------------

newtype Parser a              =  P (String -> [(a,String)])

instance Monad Parser where
   return v                   =  P (\inp -> [(v,inp)])
   p >>= f                    =  P (\inp -> case parse p inp of
                                               []        -> []
                                               [(v,out)] -> parse (f v) out)
                                               
--------------------------------adding----

instance Functor Parser where
  fmap = liftM

instance Applicative Parser where
  pure  = return
  (<*>) = ap
                                               


--------------------------------adding----

instance Alternative Parser where
  empty = mzero
  (<|>) = mplus
                                               
------------------------------------------------ 


instance MonadPlus Parser where
   mzero                      =  P (\inp -> [])
   p `mplus` q                =  P (\inp -> case parse p inp of
                                               []        -> parse q inp
                                               [(v,out)] -> [(v,out)])

 
  
  
--Basic parsers
-------------

failure                       :: Parser a
failure                       =  mzero

item                          :: Parser Char
item                          =  P (\inp -> case inp of
                                               []     -> []
                                               (x:xs) -> [(x,xs)])

parse                         :: Parser a -> String -> [(a,String)]
parse (P p) inp               =  p inp

--Choice
------

(+++)                         :: Parser a -> Parser a -> Parser a
p +++ q                       =  p `mplus` q

--Derived primitives
------------------

sat                           :: (Char -> Bool) -> Parser Char
sat p                         =  do x <- item
                                    if p x then return x else failure

digit                         :: Parser Char
digit                         =  sat isDigit

lower                         :: Parser Char
lower                         =  sat isLower

upper                         :: Parser Char
upper                         =  sat isUpper

letter                        :: Parser Char
letter                        =  sat isAlpha

alphanum                      :: Parser Char
alphanum                      =  sat isAlphaNum

char                          :: Char -> Parser Char
char x                        =  sat (== x)

string                        :: String -> Parser String
string []                     =  return []
string (x:xs)                 =  do char x
                                    string xs
                                    return (x:xs)

many                          :: Parser a -> Parser [a]
many p                        =  many1 p +++ return []

many1                         :: Parser a -> Parser [a]
many1 p                       =  do v  <- p
                                    vs <- Main.many p
                                    return (v:vs)

ident                         :: Parser String
ident                         =  do x  <- lower
                                    xs <- Main.many alphanum
                                    return (x:xs)

nat                           :: Parser Int
nat                           =  do xs <- many1 digit
                                    return (read xs)

int                           :: Parser Int
int                           =  do char '-'
                                    n <- nat
                                    return (-n)
                                  +++ nat

space                         :: Parser ()
space                         =  do Main.many (sat isSpace)
                                    return ()

--Ignoring spacing
----------------

token                         :: Parser a -> Parser a
token p                       =  do space
                                    v <- p
                                    space
                                    return v

identifier                    :: Parser String
identifier                    =  token ident

natural                       :: Parser Int
natural                       =  token nat

integer                       :: Parser Int
integer                       =  token int

symbol                        :: String -> Parser String
symbol xs                     =  token (string xs)


--------------------------------------------------------------------

identAlpha :: Parser String
identAlpha = do x  <- alphanum
                xs <- Main.many alphanum
                return (x:xs)
                                         
idOrNum :: Parser String
idOrNum =  token identAlpha


alphanumOrDot :: Parser Char
alphanumOrDot = do s <- sat isAlphaNum
                   return s
                 +++ do s <- sat (== '_')               --updated 2014/12/04
                        return s
                 +++ do s <- sat (== '.')
                        return s
                 +++ do s <- sat (== '$')
                        return s

identOrDot :: Parser String
identOrDot = do x  <- letter
                xs <- Main.many alphanumOrDot
                return (x:xs)
                                         
idOrDot :: Parser String
idOrDot =  token identOrDot

alphanumOrOther :: Parser Char                      --for Data or Type
alphanumOrOther = do s <- sat isAlphaNum
                     return s
                   +++ do s <- sat (== '_')
                          return s
                   +++ do s <- sat (== '.')
                          return s
                   +++ do s <- sat (== '/')
                          return s
                   +++ do s <- sat (== ':')
                          return s
                   +++ do s <- sat (== '*')
                          return s
                   +++ do s <- sat (== '?')
                          return s
                   +++ do s <- sat (== '@')
                          return s
                                      
identOrOther :: Parser String
identOrOther = do x  <- letter
                  xs <- Main.many alphanumOrOther
                  return (x:xs)

idOrOther :: Parser String
idOrOther = token identOrOther

---------------------------------------------------------

intent :: Parser IntentSpec
intent = do symbol "{"
            s <- fields
            symbol "}"
            symbol "||"
            i <- intent
            return (s : i)
          +++do symbol "{"
                s <- fields
                symbol "}"
                return [s]

fields :: Parser Intent
fields = do a <- action
            s <- fields
            return (a : s)
          +++ do c <- category
                 s <- fields
                 return (c : s)
          +++ do d <- idata
                 s <- fields
                 return (d : s)
          +++ do t <- itype
                 s <- fields
                 return (t : s)
          +++ do c <- component
                 s <- fields
                 return (c : s)
          +++ do e <- extra
                 s <- fields
                 return (e : s)
          +++ do f <- flag
                 s <- fields
                 return (f : s)
          +++ do f <- internal
                 s <- fields
                 return (f : s)
          +++ return []
        

action :: Parser Field
action = do symbol "act"
            symbol "="
            act <- idOrDot
            return (Action act)


category :: Parser Field
category = do symbol "cat"
              symbol "="
              symbol "["
              cat <- idOrDot
              Category cats <- categorySub
              symbol "]"
              return (Category (cat : cats))

categorySub :: Parser Field
categorySub = do symbol ","
                 cat <- idOrDot
                 Category cats <- categorySub
                 return (Category (cat : cats))
               +++ return (Category [])

idata :: Parser Field
idata = do symbol "dat"
           symbol "="
           --dat <- symbol "non-null"
           dat <- idOrOther
           return (Data dat)
           

itype :: Parser Field
itype = do symbol "typ"
           symbol "="
           typ <- idOrOther
           --typ <- symbol "non-null"
           return (Type typ)
           
component :: Parser Field
component = do symbol "cmp"
               symbol "="
               pname <- idOrDot
               if isComptype pname then
                do { pname' <- idOrDot
                   ; component' (getComptype pname) pname' }
               else
                do component' Nothing pname
                  
component' :: Maybe CompType -> String -> Parser Field
component' comptype pname = do               
               symbol "/"
               do cname <- idOrDot
                  return (Component comptype pname cname)
                +++ do symbol "."
                       cname <- idOrDot
                       return (Component comptype pname ("."  ++ cname))
                       
isComptype :: String -> Bool
isComptype "Activity" = True
isComptype "Service" = True
isComptype "BroadcastReceiver" = True
isComptype "ContentProvider" = True
isComptype _ = False
      
getComptype :: String -> Maybe CompType
getComptype "Activity" = Just Activity
getComptype "Service" = Just Service
getComptype "BroadcastReceiver" = Just BroadcastReceiver
getComptype "ContentProvider" = Just ContentProvider
getComptype _ = Nothing

extra :: Parser Field
extra = do symbol "["
           k <- idOrDot
           symbol "="
           e <- typeAndValue
           Extra es <- extraSub
           symbol "]"
           return (Extra ((k, e) : es)) 
           
extraSub :: Parser Field
extraSub = do symbol ","
              k <- idOrDot
              symbol "="
              e <- typeAndValue
              Extra es <- extraSub
              return (Extra ((k, e) : es))
            +++ return (Extra [])

typeAndValue :: Parser ExtraType
typeAndValue =  do symbol "String"
                   v <- stringValue
                   return (StringType v)
                 +++ do symbol "int[]"
                        v <- intArrayValue
                        return (IntArray v)
                 +++ do symbol "long[]"
                        v <- longArrayValue
                        return (LongArray v)
                 +++ do symbol "float[]"
                        v <- floatArrayValue
                        return (FloatArray v)
                 +++ do symbol "boolean"
                        v <- booleanValue
                        return (BooleanType v)
                 +++ do symbol "int"
                        v <- intValue
                        return (IntegerType v)
                 +++ do symbol "long"
                        v <- longValue
                        return (LongType v)
                 +++ do symbol "float"
                        v <- floatValue
                        return (FloatType v)
                 +++ do symbol "uri"
                        v <- uriValue
                        return (UriType v)
                 +++ do symbol "component"
                        v1 <- componentValue
                        do symbol "."
                           v2 <- componentValue
                           return (ComponentType v1 ('.':v2))
                         +++ do v2 <- componentValue
                                return (ComponentType v1 v2)
                        


--String
notDoubleQuotes :: Parser Char
notDoubleQuotes =  sat (/= '"')

strWithSp :: Parser String
strWithSp = do x <- notDoubleQuotes
               xs <- Main.many notDoubleQuotes
               return (x:xs)

stringWithSpace :: Parser String
stringWithSpace =  do char '"'  
                      sv <- strWithSp
                      char '"'
                      return sv

stringValue :: Parser String
stringValue = token stringWithSpace


--Bool
boolean :: Parser Bool
boolean =  do bv <- symbol "True"
              return (read bv)
            +++ do bv <- symbol "False"
                   return (read bv)

booleanValue = token boolean


--Int
intV :: Parser Int
intV = do i <- integer
          return i

intValue :: Parser Int
intValue = token intV


--Int[]
intArrayValue :: Parser [Int]
intArrayValue = do i <- intValue
                   is <- intArrayValueSub
                   return (i : is)

intArrayValueSub :: Parser [Int]
intArrayValueSub = do symbol ","
                      i <- intValue
                      is <- intArrayValueSub
                      return (i : is)
                    +++ return []

--Long
natLong :: Parser Integer
natLong =  do xs <- many1 digit
              return (read xs)

intLong :: Parser Integer
intLong =  do char '-'
              n <- natLong
              return (-n)
            +++ natLong

long :: Parser Integer
long = do i <- intLong
          return i
               
longValue :: Parser Integer
longValue = token long


--Long[]
longArrayValue :: Parser [Integer]
longArrayValue = do l <- longValue
                    ls <- longArrayValueSub
                    return (l : ls)

longArrayValueSub :: Parser [Integer]
longArrayValueSub = do symbol ","
                       l <- longValue
                       ls <- longArrayValueSub
                       return (l : ls)
                     +++ return []


--Float(????)
digitOrDot :: Parser Char
digitOrDot = do s <- sat isDigit
                return s
            +++ do s <- sat (== '.')
                   return s

flo :: Parser Float
flo =  do xs <- many1 digitOrDot
          return (read xs)

float :: Parser Float
float = do char '-'
           f <- flo
           return (-f)
        +++ do f <- flo
               return f
               
floatValue :: Parser Float
floatValue = token float


--Float[]
floatArrayValue :: Parser [Float]
floatArrayValue = do f <- floatValue
                     fs <- floatArrayValueSub
                     return (f : fs)

floatArrayValueSub :: Parser [Float]
floatArrayValueSub = do symbol ","
                        f <- floatValue
                        fs <- floatArrayValueSub
                        return (f : fs)
                      +++ return []


--Uri
uri :: Parser String
uri = do uri <- idOrOther
         return uri
        
uriValue :: Parser String
uriValue = token uri


--Component
componentV :: Parser String
componentV = do cls <- idOrDot
                return cls

componentValue :: Parser String
componentValue = token componentV



flag :: Parser Field
flag = do symbol "flg"
          symbol "["
          fl <- idOrNum
          Flag fls <- flagSub
          symbol "]"
          return (Flag (fl : fls))

flagSub :: Parser Field
flagSub = do symbol ","
             fl <- idOrDot
             Flag fls <- categorySub
             return (Flag (fl : fls))
           +++ return (Flag [])

internal :: Parser Field
internal = do symbol "internal"
              symbol "="
              bool <- boolean
              return (Internal bool)

arr :: Parser String
arr = do a1 <- symbol "[]"
         a2 <- arr
         return (a1 ++ a2)
       +++ return ""

eval :: String -> IntentSpec
eval xs = case parse intent xs of
               [(n, [])] -> n
               [(_, out)] -> error ("unused input" ++ out)
               [] -> error "invalid input"


instance Arbitrary Field where
  arbitrary = do n <- choose (1,14) :: Gen Int
                 case n of
                      1 -> do act <- actionElements
                              return (Action act)
                              
                      2 -> do cat <- categoryElementsList
                              return (Category cat)
                              
                      3 -> do dat <- dataElements
                              return (Data dat)
                              
                      4 -> do typ <- typeElements
                              return (Type typ)
                              
                      5 -> do pkg <- packageElements
                              cls <- classElements
                              comptype <- comptypeElements
                              return (Component comptype pkg cls) -- Is it OK?
                              
                      6 -> do exts <- extraTupleList
                              return (Extra exts)

                      7 -> do flg <- flagElementsList 
                              return (Flag flg)
                      
                      8 -> do act <- actionArbitrary
                              return (Action act)
                              
                      9 -> do cat <- categoryArbitrary
                              return (Category cat)
                              
                      10 -> do dat <- dataArbitrary
                               return (Data dat)
                              
                      11 -> do typ <- typArbitrary
                               return (Type typ)

                      12 -> do pkg <- componentsArbitrary
                               cls <- componentsArbitrary
                               comptype <- comptypeElements
                               return (Component comptype pkg cls) -- Is it OK?

                      13 -> do exts <- extraArbitrary
                               return (Extra exts)

                      14 -> do flg <- flagArbitrary 
                               return (Flag flg)


extraTupleList :: Gen [(String, ExtraType)]
extraTupleList = listOf1 $ ((>*<) extraKeyElements (arbitrary))

extraArbitrary :: Gen [(String, ExtraType)]
extraArbitrary = listOf1 $ ((>*<) keyArbitrary (arbitrary))

instance Arbitrary ExtraType where
  arbitrary = oneof[ liftM StringType stringTypeElements,
                     liftM BooleanType booleanTypeElements,
                     liftM IntegerType intTypeElements,
                     liftM LongType longTypeElements,
                     liftM FloatType floatTypeElements,
                     liftM UriType uriTypeElements,
                     liftM IntArray intArrayElements,
                     liftM LongArray longArrayElements,
                     liftM FloatArray floatArrayElements,
                     liftM StringType stringTypeArbitrary,
                     liftM BooleanType arbitrary,
                     liftM IntegerType arbitrary,
                     liftM LongType arbitrary,
                     liftM FloatType arbitrary,
                     liftM UriType uriTypeArbitrary,
                     liftM IntArray intArrayElements,
                     liftM LongArray longArrayElements,
                     liftM FloatArray floatArrayElements]
{-
                     liftM2 ComponentType packageElements classElements,

                     liftM2 ComponentType componentsArbitrary componentsArbitrary,
                     liftM IntArray arbitrary,
                     liftM LongArray arbitrary,
                     liftM FloatArray arbitrary]
-}

genField seed = unGen arbitrary (mkQCGen (unsafePerformIO (getStdRandom (randomR (-9223372036854775807, 9223372036854775806))))) 15 :: [Field]


makeTestCaseOfIntentSpec :: Int -> IntentSpec
makeTestCaseOfIntentSpec count = take count [x | x <- map (removeDuplicateConstructor . genField) [1..count] ]


removeDuplicateConstructor :: Intent -> Intent
removeDuplicateConstructor [] = []
removeDuplicateConstructor (Action x : xs) = Action x : removeDuplicateConstructor (xs \\ [Action y | Action y <- xs])
removeDuplicateConstructor (Category x : xs) = removeDuplicateCategoryList (Category x) : removeDuplicateConstructor (xs \\ [Category y | Category y <- xs])
removeDuplicateConstructor (Data x : xs) = Data x : removeDuplicateConstructor (xs \\ [Data y | Data y <- xs])
removeDuplicateConstructor (Type x : xs) = Type x : removeDuplicateConstructor (xs \\ [Type y | Type y <- xs])
removeDuplicateConstructor (Component maybe x1 x2 : xs) = Component maybe x1 x2 : removeDuplicateConstructor (xs \\ [Component maybe' y1 y2 | Component maybe' y1 y2 <- xs])
removeDuplicateConstructor (Extra x : xs) = removeDuplicateExtraKeys (Extra x) : removeDuplicateConstructor (xs \\ [Extra y | Extra y <- xs])
removeDuplicateConstructor (Flag x : xs) = Flag x : removeDuplicateConstructor (xs \\ [Flag y | Flag y <- xs])
removeDuplicateConstructor (Internal x : xs) = Internal x : removeDuplicateConstructor (xs \\ [Internal y | Internal y <- xs])

rmdups :: Eq a => [a] -> [a]
rmdups [] = []
rmdups (x : xs) = x : rmdups (filter (/=x) xs)

removeDuplicateCategoryList :: Field -> Field
removeDuplicateCategoryList (Category (xs)) = Category (rmdups xs)

rmdupsExtra :: Eq a => [(a,b)] -> [(a,b)]
rmdupsExtra [] = []
rmdupsExtra ((k,t) : xs) = (k,t) : rmdupsExtra (filter ((/=k).fst) xs)

--fstExtra :: (a,b,c) -> a
--fstExtra (x,_,_) = x 

removeDuplicateExtraKeys :: Field -> Field
removeDuplicateExtraKeys (Extra (xs)) = Extra (rmdupsExtra xs)

replaceComponent :: Intent -> IntentSpec -> IntentSpec
replaceComponent _ [] = []
replaceComponent s (d:ds) = addAndRemoveComponentFix [Component maybe x1 x2 | Component maybe x1 x2 <- s] d : replaceComponent s ds

addAndRemoveComponentFix :: Intent -> Intent -> Intent
addAndRemoveComponentFix (Component maybe x1 x2 : ss) ds = Component maybe x1 x2 : (ds \\ [Component maybe' y1 y2 | Component maybe' y1 y2 <- ds])

makeAdbCommand :: IntentSpec -> String
makeAdbCommand [] = []
makeAdbCommand (i:is) = if [f | f@(Internal True) <- i] == [] 
  then "adb shell am " ++ makeIntentCompType i ++ makeIntentCommand i ++ "\n" ++ makeAdbCommand is
  else makeAdbCommand is


makeIntentCompType :: Intent -> String
makeIntentCompType [] = "start"   -- Activity by default
makeIntentCompType (Component (Just Activity) _ _ : xs) = "start"
makeIntentCompType (Component (Just Service) _ _ : xs) = "startservice"
makeIntentCompType (Component (Just BroadcastReceiver) _ _ : xs) = "broadcast"
makeIntentCompType (Component _ _ _ : xs) = makeIntentCompType xs
makeIntentCompType (_ : xs) = makeIntentCompType xs 

makeIntentCommand :: Intent -> String
makeIntentCommand [] = []
makeIntentCommand (Action x : xs) = " -a " ++ x ++ makeIntentCommand xs
makeIntentCommand (Category x : xs) = makeCagegory x ++ makeIntentCommand xs
makeIntentCommand (Data x : xs) = " -d " ++ x ++ makeIntentCommand xs
makeIntentCommand (Type x : xs) = " -t " ++ x ++ makeIntentCommand xs
makeIntentCommand (Component maybe x1 x2 : xs) = " -n " ++ x1 ++ "/" ++ x2 ++ makeIntentCommand xs
makeIntentCommand (Extra x : xs)  = makeExtra x ++ makeIntentCommand xs
makeIntentCommand (Flag x : xs) = makeIntentCommand xs
makeIntentCommand (Internal x : xs) = makeIntentCommand xs

makeCagegory :: [String] -> String
makeCagegory [] = []
makeCagegory (x:xs) = " -c " ++ x ++ makeCagegory xs

makeExtra :: [(String, ExtraType)] -> String
makeExtra [] = []
makeExtra ((k, StringType v) : xs)              = " --es " ++ k ++ " \"" ++ v ++ "\"" ++ makeExtra xs
makeExtra ((k, BooleanType v) : xs)             = " --ez " ++ k ++ " " ++ (show v) ++ makeExtra xs
makeExtra ((k, IntegerType v) : xs)             = " --ei " ++ k ++ " " ++ (show v) ++ makeExtra xs
makeExtra ((k, LongType v) : xs)                = " --el " ++ k ++ " " ++ (show v) ++ makeExtra xs
makeExtra ((k, FloatType v) : xs)               = " --ef " ++ k ++ " " ++ (show v) ++ makeExtra xs
makeExtra ((k, UriType v) : xs)                 = " --eu " ++ k ++ " " ++ v ++ makeExtra xs
makeExtra ((k, ComponentType v1 v2) : xs)       = " --ecn " ++ k ++ " " ++ v1 ++ "/" ++ v2 ++ makeExtra xs
makeExtra ((k, IntArray vs) : xs)               = " --eia " ++ k ++ " " ++ (makeArrayValue vs) ++ makeExtra xs
makeExtra ((k, LongArray vs) : xs)              = " --ela " ++ k ++ " " ++ (makeArrayValue vs) ++ makeExtra xs
makeExtra ((k, FloatArray vs) : xs)             = " --efa " ++ k ++ " " ++ (makeArrayValue vs) ++ makeExtra xs

makeArrayValue :: Show a => [a] -> String
makeArrayValue [] = ""
makeArrayValue (x:[]) = (show x) ++ " "
makeArrayValue (x:xs) = (show x) ++ "," ++ makeArrayValue xs

addIntentSpecUsingInputIntent :: Intent -> IntentSpec -> IntentSpec
addIntentSpecUsingInputIntent _ [] = []
addIntentSpecUsingInputIntent s (d:ds) = (d ++ s) : addIntentSpecUsingInputIntent s ds

removeDuplicateConstructorIntentSpec :: IntentSpec -> IntentSpec
removeDuplicateConstructorIntentSpec xs = map removeDuplicateConstructor xs

addRandomUsingInputIntent :: Intent -> IntentSpec -> IntentSpec
addRandomUsingInputIntent _ [] = []
addRandomUsingInputIntent s (d:ds) = (s ++ d) : addRandomUsingInputIntent s ds

addInternalUsingInputIntent _ [] = []
addInternalUsingInputIntent s (d:ds) = ([ f | f@(Internal _) <- s] ++ d) : addInternalUsingInputIntent s ds

passOnly :: Int -> IntentSpec -> IntentSpec
passOnly _ [] = []
passOnly count (s:ss) = addRandomUsingInputIntent s (replaceComponent s (makeTestCaseOfIntentSpec count)) ++ passOnly count ss

randomUsingSpec :: Int -> IntentSpec -> IntentSpec
randomUsingSpec _ [] = []
randomUsingSpec count (s:ss) = addIntentSpecUsingInputIntent s (replaceComponent s (makeTestCaseOfIntentSpec count)) ++ randomUsingSpec count ss

randomOnly :: Int -> IntentSpec -> IntentSpec
randomOnly _ [] = []
randomOnly count (s:ss) = addInternalUsingInputIntent s (replaceComponent s (makeTestCaseOfIntentSpec count)) ++ randomOnly count ss

makeTestArtifact :: String -> IntentSpecStr -> IntentSpec -> [String] -> String -> IO ()
makeTestArtifact "AdbCommand" intentSpecS intents args thefile = do
    writeFile (reverse(drop 3 (reverse thefile)) ++ ".randomis") (show intents)
    putStr (makeAdbCommand intents)
            
makeTestArtifact "AndroidTestCode" intentSpecS intents args thefile = 
    genAndroidTestCode intentSpecS intents args

--    
type RandomType = Int
type IntentSpecStr = String
type Count = Int
type ArtifactType = String
type ComponentType = Int


make :: ArtifactType -> RandomType -> Count -> IntentSpecStr -> [String] -> String -> IO ()
    
make artifactType randomType count intentSpecS args thefile
    | randomType==0 = 
        let intents = removeDuplicateConstructorIntentSpec 
                        (passOnly count (eval intentSpecS)) 
        in  makeTestArtifact artifactType intentSpecS intents args thefile
    | randomType==1 = 
        let intents = removeDuplicateConstructorIntentSpec 
                        (randomUsingSpec count (eval intentSpecS)) 
        in  makeTestArtifact artifactType intentSpecS intents args thefile
    | randomType==2 = 
        let intents = removeDuplicateConstructorIntentSpec 
                        (randomOnly count (eval intentSpecS)) 
        in  makeTestArtifact artifactType intentSpecS intents args thefile
    | randomType==3 = 
        let intents = removeDuplicateConstructorIntentSpec(passOnly count (eval intentSpecS)) ++ removeDuplicateConstructorIntentSpec(randomUsingSpec count (eval intentSpecS)) ++ removeDuplicateConstructorIntentSpec(randomOnly count (eval intentSpecS))
        in  makeTestArtifact artifactType intentSpecS intents args thefile
        

{-
  Arguments:

   1) AdbCommand 0 3 "{cmp=com.example.android/.Main}"
   2) AdbCommand 0 3 -f myspec.is
   3) AdbCommand 0 3 -ftmp myspec.is
        (The file is deleted after it is processed.)

   4) AndroidTestCode 0 3 "{cmp=com.example.android/.Main}" com.example.android.test
   5) AndroidTestCode 0 3 -f myspec.is com.example.android.test
   6) AndroidTestCode 0 3 -ftmp myspec.is  com.example.android.test
        (The file is deleted after it is processed.)

-}

main :: IO ()
main = do hSetEncoding stdout utf8
          args_0 <- getArgs
          let artifact_type = args_0 !! 0
          let random_type = castInt 1  args_0
          let count = castInt 2 args_0
          (spec, args, keep, thefile) <- readIntentSpec (drop 3 args_0)
          -- artifact type, random type, count, intent spec,
          -- putStrLn spec
          -- mapM_ putStrLn args
          if artifact_type == "AndroidTestCodeFromRandomis" 
                then  let intents = read spec::IntentSpec 
                          in  makeTestArtifact "AndroidTestCode" spec intents args thefile
                else  make  artifact_type random_type count spec args thefile
              
          if keep then return ()  
          else do fileExists <- doesFileExist thefile
                  when fileExists (removeFile thefile)
                                    


castInt :: Int -> [String] -> Int
castInt 0 (arg:args) = read arg :: Int
castInt n (arg:args) = castInt (n-1) args

readIntentSpec :: [String] -> IO (String, [String], Bool, String ) -- True : -f, False : -ftmp
readIntentSpec args = 
  do if length args >= 2 && (args !! 0 == "-f" || args !! 0 == "-ftmp")
       then do txt <- readFile (args !! 1)
               return (txt, drop 2 args, args !! 0 == "-f", args !! 1)
       else return (head args, tail args, True, "")



-- >ghc --make Main.hs

-- :set args AndroidTestCode 0 5 "{ cmp=Activity com.example.khchoi.helloandroid/.MainActivity act=android.intent.action.MAIN }" com.example.khchoi.helloandroid.test  
-- :set args AndroidTestCode 0 5 "{ cmp=Service com.example.khchoi.helloandroid/.MyService act=android.intent.action.MAIN }" com.example.khchoi.helloandroid.test


