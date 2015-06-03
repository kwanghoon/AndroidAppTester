module TypeElements where

import Test.QuickCheck

typeElements = elements ["video/*",
                         "image/*",
                         "text/plain"]

                         
typArbitrary = listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['.', '_', '*', '/'])