module FlagElements where

import Test.QuickCheck

flagElements = elements ["FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET",
                         "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS",
                         "FLAG_ACTIVITY_FORWARD_RESULT"]

flagElementsList = listOf1 $ flagElements


flagArbitrary = listOf1 $ listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['.', '_'])