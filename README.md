# Tokenizer


## Quick Install
*Clone repository*
```bash
git clone 
cd campus-one-backend
```

## Project layout
src/main/java
- `Main.java` main program entry
- database/ — contains the `.txt` files of tokens
- gui/ — interface and components
- model — objects for each token
- recognizers/ — logic for recognizing token
- tokenizer/ — integration of program

test/
- Location of files used for testing

bin/
- Contains the `.class` files


## Compile & Run Main File (via terminal)
NOTE: Test files and `Main.java` can be run using the `Run Java` button.

*Step 1: Open new Powershell terminal*
(Optional) Empty `bin` folder first to remove old `.class` files:

```powershell
Remove-Item -Recurse -Force bin\* -ErrorAction SilentlyContinue
```
  
*Step 2: Compile all Java files into bin and run Main.java*

```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName } 
javac -d bin $files 
java -cp bin main.java.Main
```

  

  