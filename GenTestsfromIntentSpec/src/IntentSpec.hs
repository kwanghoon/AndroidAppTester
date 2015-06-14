module IntentSpec where

type IntentSpec = [Intent]
type Intent = [Field]
data Field = Action String | Category [String] | Data String | Type String 
             | Component (Maybe CompType) String String | Extra [(String, ExtraType)] | Flag [String] deriving (Show, Eq)

data CompType = Activity | Service | BroadcastReceiver | ContentProvider deriving (Show, Eq)

data ExtraType = StringType String | BooleanType Bool | IntegerType Int | LongType Integer | FloatType Float
                 | UriType String | ComponentType String String
                 | IntArray [Int] | LongArray [Integer] | FloatArray [Float] deriving (Show, Eq)

-- TODO: Need to move all declarations relevant to IntentSpec here.  

getPkgClz :: String -> String-> (String, String)
getPkgClz androidPkg pkgclz =
    case pkgclz of
        ('.':clz') -> (androidPkg, clz')
        _          -> let ids = pkgnameToIds pkgclz
                      in  (idsToPkgname $ init $ ids, last ids)
                      
getFullClz :: String -> String -> String
getFullClz pkg clz =
    case clz of
        ('.':clz') -> pkg ++ clz
        _          -> clz                      

pkgnameToIds :: String -> [String]
pkgnameToIds []       = []
pkgnameToIds pkg_name = h : pkgnameToIds t  
    where
        h = takeWhile (/= '.') pkg_name
        t = dropWhile (== '.') $ drop (length h) pkg_name
        
idsToPkgname :: [String] -> String
idsToPkgname []     = ""
idsToPkgname [x]    = x
idsToPkgname (x:xs) = x ++ "." ++ idsToPkgname xs

pkgnameToPath :: String -> String        
pkgnameToPath pkg_name = f (pkgnameToIds pkg_name)
    where
        f []     = ""
        f [x]    = x
        f (x:xs) = x ++ "/" ++ f xs 

-- test example
note_intent1 :: IntentSpec
note_intent1 = [ 
    [ Component Nothing "com.example.android" ".Note"
    , Action "android.intent.action.EDIT"
    , Extra [ ( "title", StringType "my title" )
            , ( "content", StringType "my content" ) ]
    , Data "qoFXwARtpfV-LNN" ] ]
                        