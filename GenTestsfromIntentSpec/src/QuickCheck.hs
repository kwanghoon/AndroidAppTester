module QuickCheck where

import Test.QuickCheck
import Test.QuickCheck.Arbitrary
import Test.QuickCheck.Gen
import System.Random
import Data.List
import Control.Monad

qsort :: Ord a => [a] -> [a]
qsort [] = []
qsort (x:xs) = qsort lhs ++ [x] ++ qsort rhs
               where lhs = filter (< x) xs
                     rhs = filter (>= x) xs
                     
test1 = unGen arbitrary (mkStdGen 42) 10 :: [Bool]

prop_idempotent xs = qsort (qsort xs) == qsort xs
test2 = quickCheck (prop_idempotent :: [Integer] -> Bool)


prop_minimum xs = head (qsort xs) == minimum xs
test3 = quickCheck (prop_minimum :: [Integer] -> Bool)

prop_minimum' xs = not (null xs) ==> head (qsort xs) == minimum xs
test4 = quickCheck (prop_minimum' :: [Integer] -> Property)

prop_ordered xs = ordered (qsort xs)
                  where ordered [] = True
                        ordered [x] = True
                        ordered (x:y:xs) = x <= y && ordered (y:xs)
                        
prop_permutation xs = permutation xs (qsort xs)
                      where permutation xs ys = null (xs \\ ys) && null (ys \\ xs)
                      
prop_maximum xs = not (null xs) ==> last (qsort xs) == maximum xs

prop_append xs ys = not (null xs) ==> not (null ys) ==> head (qsort (xs ++ ys)) == min (minimum xs) (minimum ys)

prop_sort_model xs = sort xs == qsort xs

data Doc = Empty | Char Char | Text String | Line | Concat Doc Doc | Union Doc Doc deriving (Show, Eq)

data Ternary = Yes | No | Unknown deriving (Show, Eq)

{-
instance Arbitrary Ternary where
  arbitrary = elements [Yes, No, Unknown]
-}


instance Arbitrary Ternary where
  arbitrary = do n <- choose (0, 2) :: Gen Int
                 return $ case n of
                               0 -> Yes
                               1 -> No
                               _ -> Unknown

{-
instance (Arbitrary a, Arbitrary b) => Arbitrary (a, b) where
  arbitrary = do x <- arbitrary
                 y <- arbitrary
                 return (x, y)
-}

{-
--문서에는 정의 안되어 있다고 하지만, 실제로는 정의되어 있음..
instance Arbitrary Char where
  arbitrary = elements (['A'..'Z'] ++ ['a'..'z'] ++ "~!@#$%^&*()")
-}

{-
instance Arbitrary Doc where
  arbitrary = do n <- choose (1,6) :: Gen Int
                 case n of
                      1 -> return Empty
                      2 -> do x <- arbitrary
                              return (Char x)
                      3 -> do x <- arbitrary
                              return (Text x)
                      4 -> return Line
                      5 -> do x <- arbitrary
                              y <- arbitrary
                              return (Concat x y)
                      6 -> do x <- arbitrary
                              y <- arbitrary
                              return (Union x y)
-}

instance Arbitrary Doc where
  arbitrary = oneof [ return Empty, 
                      liftM Char arbitrary,
                      liftM Text arbitrary,
                      return Line,
                      liftM2 Concat arbitrary arbitrary,
                      liftM2 Union arbitrary arbitrary ]
                  
prop_empty_id x = [] ++ x == x && x ++ [] == x
test5 = quickCheck (prop_empty_id :: [Doc] -> Bool)



