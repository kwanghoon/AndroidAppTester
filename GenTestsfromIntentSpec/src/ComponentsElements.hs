module ComponentsElements where

import Test.QuickCheck

packageElements = elements ["com.example.android.notepad",
                            "com.enterpriseandroid.androidSecurity",
                            "com.example.cafe"]

classElements = elements ["com.example.android.notepad.NoteEditor",
                          ".NoteEditor",
                          ".MainActivity",
                          ".CafeActivity"]

componentsArbitrary = listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['.', '_'])