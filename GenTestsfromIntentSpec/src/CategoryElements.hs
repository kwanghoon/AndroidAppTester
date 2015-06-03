module CategoryElements where

import Test.QuickCheck

categoryElements = elements ["android.intent.category.APP_CALENDAR",
                             "android.app.category.LAUNCHER",
                             "android.intent.category.DEFAULT"]

{-
listOf : Generates a list of random length
listOf1 : Generates a non-empty list of random length
-}

categoryElementsList = listOf1 $ categoryElements


categoryArbitrary = listOf1 $ listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['.', '_'])