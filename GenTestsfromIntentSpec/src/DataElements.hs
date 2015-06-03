module DataElements where

import Test.QuickCheck

dataElements = elements ["content://com.google.provider.NotePad/notes",
                         "content://contacts/people/1",
                         "tel:123"]


dataArbitrary = listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['0'..'9'] ++ ['.', '_', ':', '/', '?', '@'])