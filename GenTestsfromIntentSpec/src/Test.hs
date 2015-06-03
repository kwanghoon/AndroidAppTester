module Test where

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

--OK
failure                       :: Parser a
failure                       =  mzero

--OK
item                          :: Parser Char
item                          =  P (\inp -> case inp of
                                               []     -> []
                                               (x:xs) -> [(x,xs)])
                                               
--현재 못함 나중에 확인 필요
parse                         :: Parser a -> String -> [(a,String)]
parse (P p) inp               =  p inp

--Choice
------

--만들필요가 없음
(+++)                         :: Parser a -> Parser a -> Parser a
p +++ q                       =  p `mplus` q

--Derived primitives
------------------

--만들필요가 없는듯
sat                           :: (Char -> Bool) -> Parser Char
sat p                         =  do x <- item
                                    if p x then return x else failure
--OK
digit                         :: Parser Char
digit                         =  sat isDigit

--OK
lower                         :: Parser Char
lower                         =  sat isLower

--OK
upper                         :: Parser Char
upper                         =  sat isUpper

--OK
letter                        :: Parser Char
letter                        =  sat isAlpha

--OK
alphanum                      :: Parser Char
alphanum                      =  sat isAlphaNum

--OK
char                          :: Char -> Parser Char
char x                        =  sat (== x)

--OK
string                        :: String -> Parser String
string []                     =  return []
string (x:xs)                 =  do char x
                                    string xs
                                    return (x:xs)


--만들필요가 없는듯
many                          :: Parser a -> Parser [a]
many p                        =  many1 p +++ return []

many1                         :: Parser a -> Parser [a]
many1 p                       =  do v  <- p
                                    vs <- many p
                                    return (v:vs)

--OK
ident                         :: Parser String
ident                         =  do x  <- lower
                                    xs <- many alphanum
                                    return (x:xs)

--OK
nat                           :: Parser Int
nat                           =  do xs <- many1 digit
                                    return (read xs)

--OK
int                           :: Parser Int
int                           =  do char '-'
                                    n <- nat
                                    return (-n)
                                  +++ nat

--OK
space                         :: Parser ()
space                         =  do many (sat isSpace)
                                    return ()

--Ignoring spacing
----------------

--만들필요가 없는듯
token                         :: Parser a -> Parser a
token p                       =  do space
                                    v <- p
                                    space
                                    return v

--OK
identifier                    :: Parser String
identifier                    =  token ident

--OK
natural                       :: Parser Int
natural                       =  token nat

--OK
integer                       :: Parser Int
integer                       =  token int

--OK
symbol                        :: String -> Parser String
symbol xs                     =  token (string xs)





--------------------------------------------------------------------
--OK
identAlpha :: Parser String
identAlpha = do x  <- alphanum
                xs <- many alphanum
                return (x:xs)
--OK                                         
idOrNum :: Parser String
idOrNum =  token identAlpha

--OK
alphanumOrDot :: Parser Char
alphanumOrDot = do s <- sat isAlphaNum
                   return s
                 +++ do s <- sat (== '.')
                        return s

--OK
identOrDot :: Parser String
identOrDot = do x  <- letter
                xs <- many alphanumOrDot
                return (x:xs)

--OK
idOrDot :: Parser String
idOrDot =  token identOrDot

{-
alphanumOrDotOrSpace :: Parser Char
alphanumOrDotOrSpace = do s <- sat isAlphaNum
                          return s
                        +++ do s <- sat (== '.')
                               return s
                        +++ do s <- sat (== ' ')
                               return s

identOrDotOrSpace :: Parser String
identOrDotOrSpace = do xs <- many alphanumOrDotOrSpace
                       return xs

idOrDotOrSpace :: Parser String
idOrDotOrSpace =  token identOrDotOrSpace
-}


--OK
intent :: Parser String
intent = do symbol "{"
            s <- fields
            symbol "}"
            symbol "||"
            i <- intent
            return ("{" ++ s ++ "} || " ++ i)
          +++do symbol "{"
                s <- fields
                symbol "}"
                return ("{" ++ s ++ "}")

--OK
fields :: Parser String
fields = do a <- action
            s <- fields
            return (a ++ s)
          +++ do c <- category
                 s <- fields
                 return (c ++ s)
          +++ do d <- idata
                 s <- fields
                 return (d ++ s)
          +++ do t <- itype
                 s <- fields
                 return (t ++ s)
          +++ do c <- component
                 s <- fields
                 return (c ++ s)
          +++ do e <- extra
                 s <- fields
                 return (e ++ s)
          +++ do f <- flag
                 s <- fields
                 return (f ++ s)
          +++ return ""
        
--OK
action :: Parser String
action = do symbol "act"
            symbol "="
            act <- idOrDot
            return ("act=" ++ act ++ " ")


--OK
category :: Parser String
category = do symbol "cat"
              symbol "="
              symbol "["
              cat <- idOrDot
              cats <- categorySub
              symbol "]"
              return ("cat=[" ++ cat ++ cats ++ "] ")

--OK
categorySub :: Parser String
categorySub = do symbol ","
                 cat <- idOrDot
                 cats <- categorySub
                 return ("," ++ cat ++ cats)
               +++ return ""

--OK
idata :: Parser String
idata = do symbol "dat"
           symbol "="
           dat <- symbol "non-null"
           return ("dat=" ++ dat ++ " ")
           
--OK
itype :: Parser String
itype = do symbol "typ"
           symbol "="
           typ <- symbol "non-null"
           return ("typ=" ++ typ ++ " ")

--OK
component :: Parser String
component = do symbol "cmp"
               symbol "="
               pname <- idOrDot
               symbol "/"
               do cname <- idOrDot
                  return ("cmp=" ++ pname ++ "/" ++ cname ++ " ")
                +++ do symbol "."
                       cname <- idOrDot
                       return ("cmp=" ++ pname ++ "/." ++ cname ++ " ")

--OK
extra :: Parser String
extra = do symbol "["
           i <- idOrNum
           e <- extraSub
           symbol "]"
           return (" [" ++ i ++ e ++ "] ")

--OK           
extraSub :: Parser String
extraSub = do symbol ","
              i <- idOrNum
              is <- extraSub
              return (", " ++ i ++ is)
            +++ return ""
               
--OK
flag :: Parser String
flag = do symbol "flg"
          return "flg "


eval :: String -> String
eval xs = case parse intent xs of
               [(n, [])] -> n
               [(_, out)] -> error ("unused input" ++ out)
               [] -> error "invalid input"

















