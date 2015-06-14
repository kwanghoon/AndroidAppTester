module ComponentsElements where

import Test.QuickCheck
import IntentSpec

packageElements = elements ["com.example.android.notepad",
                            "com.enterpriseandroid.androidSecurity",
                            "com.example.cafe"]

classElements = elements ["com.example.android.notepad.NoteEditor",
                          ".NoteEditor",
                          ".MainActivity",
                          ".CafeActivity"]
                          
comptypeElements = 
    elements 
        ([Just x | x<- [Activity, Service, BroadcastReceiver, ContentProvider]]
            ++ [ Nothing ] )

componentsArbitrary = listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['.', '_'])