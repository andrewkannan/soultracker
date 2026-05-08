$files = Get-ChildItem -Path .\src\main\java -Recurse -Filter *.java
foreach ($file in $files) {
    $content = Get-Content $file.FullName
    $modified = $content -replace 'findByEmail', 'findFirstByEmail'
    Set-Content $file.FullName $modified
}
