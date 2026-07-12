$files = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
javac -d build/classes -sourcepath src $files
if($?) { 
    java -Dfile.encoding=UTF-8 -cp build/classes views.Main
}
