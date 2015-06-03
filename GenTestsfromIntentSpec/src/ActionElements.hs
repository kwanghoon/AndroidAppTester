module ActionElements where

import Test.QuickCheck

actionElements = elements ["com.android.action.Edit",
                           "com.android.action.Add",
                           "com.android.action.Delete"]
                           
actionArbitrary = listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['.', '_'])