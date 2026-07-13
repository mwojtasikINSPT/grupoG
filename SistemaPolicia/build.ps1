$files = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
javac -d build/classes -sourcepath src $files
if($?) { 
    java -cp build/classes views.Main
}
