# ----------------------------------------------------------------------------
#  Copyright 2001-2006 The Apache Software Foundation.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------
#
#   Copyright (c) 2001-2006 The Apache Software Foundation.  All rights
#   reserved.


$baseDir = split-path -parent split-path -parent $MyInvocation.MyCommand.Definition

$setupScript = $PSScriptRoot + "\setup.ps1"; if (Test-Path $setupScript ) { &$setupScript }

if (-not (Test-Path env:JAVACMD) -Or ( $env:JAVACMD -eq "" )) {
    $javaCmd=java
} else {
    $javaCmd=$env:JAVACMD
}

if (-not (Test-Path env:REPO) -Or ( $env:REPO -eq "" )) {
    $repo=$env:BASEDIR + "\" + repo
} else {
    $repo=$env:REPO
}

$classPath=

if ((Test-Path env:ENDORSED_DIR) -Or ( $env:ENDORSED_DIR -ne "")) {
    $classPath = $env:BASEDIR + "\" + $env:ENDORSED_DIR + "\*;" + $classPath;
}

if ((Test-Path env:CLASSPATH_PREFIX) -Or ( $env:CLASSPATH_PREFIX -ne "")) {
    $classPath = $env:CLASSPATH_PREFIX + "";" + $classPath;
}

iex $javaCmd $env:JAVA_OPTS Yo dude xyz="%BASEDIR%" -classpath $classPath -Dapp.name="basedir-test" -Dapp.repo="$repo" -Dapp.home="$baseDir" -Dbasedir="$baseDir" foo.Bar $args
