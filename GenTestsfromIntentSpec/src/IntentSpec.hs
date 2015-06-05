module IntentSpec where

type IntentSpec = [Intent]
type Intent = [Field]
data Field = Action String | Category [String] | Data String | Type String 
             | Component String String | Extra [(String, ExtraType)] | Flag [String] deriving (Show, Eq)

data ExtraType = StringType String | BooleanType Bool | IntegerType Int | LongType Integer | FloatType Float
                 | UriType String | ComponentType String String
                 | IntArray [Int] | LongArray [Integer] | FloatArray [Float] deriving (Show, Eq)

-- TODO: Need to move all declarations relevant to IntentSpec here.                 