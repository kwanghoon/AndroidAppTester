module IntentParser where

import Data.Char
import Control.Monad

infixr 5 +++

--The monad of parsers
--------------------

newtype Parser a              =  P (String -> [(a,String)])

instance Monad Parser where
   return v                   =  P (\inp -> [(v,inp)])
   p >>= f                    =  P (\inp -> case parse p inp of
                                               []        -> []
                                               [(v,out)] -> parse (f v) out)

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
                                    vs <- many p
                                    return (v:vs)

ident                         :: Parser String
ident                         =  do x  <- lower
                                    xs <- many alphanum
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
space                         =  do many (sat isSpace)
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
                xs <- many alphanum
                return (x:xs)
                                         
idOrNum :: Parser String
idOrNum =  token identAlpha


alphanumOrDot :: Parser Char
alphanumOrDot = do s <- sat isAlphaNum
                   return s
                 +++ do s <- sat (== '.')
                        return s
                 +++ do s <- sat (== '_')               --updated 2014/12/04
                        return s

identOrDot :: Parser String
identOrDot = do x  <- letter
                xs <- many alphanumOrDot
                return (x:xs)
                                         
idOrDot :: Parser String
idOrDot =  token identOrDot

type IntentSpec = [Intent]
type Intent = [Field]
data Field = Action String | Category [String] | Data String | Type String 
             | Component String String | Extra [(String, String)] | Flag deriving Show

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
           dat <- symbol "non-null"
           return (Data dat)
           

itype :: Parser Field
itype = do symbol "typ"
           symbol "="
           typ <- symbol "non-null"
           return (Type typ)
           
component :: Parser Field
component = do symbol "cmp"
               symbol "="
               pname <- idOrDot
               symbol "/"
               do cname <- idOrDot
                  return (Component pname cname)
                +++ do symbol "."
                       cname <- idOrDot
                       return (Component pname ("."  ++ cname))

extra :: Parser Field
extra = do symbol "["
           i <- idOrNum
           symbol "="
           v <- idOrNum
           do a <- symbol "[]"
              Extra e <- extraSub
              symbol "]"
              return (Extra ((i, (v ++ a)) : e))
            +++ do Extra e <- extraSub
                   symbol "]"
                   return (Extra ((i, v) : e)) 
           
extraSub :: Parser Field
extraSub = do symbol ","
              i <- idOrNum
              symbol "="
              v <- idOrNum
              do a <- arr
                 Extra is <- extraSub
                 return (Extra ((i, (v ++ a)) : is))
               +++ do Extra is <- extraSub
                      return (Extra ((i, v) : is))
            +++ return (Extra [])
           
flag :: Parser Field
flag = do symbol "flg"
          return Flag
          
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
