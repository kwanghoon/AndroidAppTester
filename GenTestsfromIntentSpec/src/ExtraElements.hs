module ExtraElements where

import Test.QuickCheck
import Test.QuickCheck.Gen

extraKeyElements = elements ["key1",
                             "key2",
                             "key3"]

keyArbitrary = listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['0'..'9'])

stringTypeElements = elements ["it is string value1",
                               "it is string value2",
                               "it is string value3",
                               "it is string value4",
                               "it is string value5"]
                               
stringTypeArbitrary = listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['0'..'9'] ++ [' ', '.', '_', ':', '/', '?', '@'])

booleanTypeElements = elements [True, False]

intTypeElements :: Gen Int
intTypeElements = elements [-500..500]

longTypeElements :: Gen Integer
longTypeElements = elements [-1000..1000]

floatTypeElements :: Gen Float
floatTypeElements = elements [-1000.0..1000.0]

uriTypeElements = elements ["//username:password@host:8080/directory/file?query#fragment",
                            "username:password@host:8080",
                            "username:password",
                            "host",
                            "8080",
                            "/directory/file",
                            "query",
                            "fragment"]

uriTypeArbitrary = listOf1 $ elements (['a'..'z'] ++ ['A'..'Z'] ++ ['0'..'9'] ++ ['.', '_', ':', '/', '?', '@'])


--component is used ComponentsElements.hs

intArrayElements = listOf1 $ intTypeElements

longArrayElements = listOf1 $ longTypeElements

floatArrayElements = listOf1 $ floatTypeElements












