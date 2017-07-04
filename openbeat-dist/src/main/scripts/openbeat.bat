setlocal EnableDelayedExpansion
set JarsDir=openbeat
if defined cp (set cp=%cp%;.) else (set cp=.)
for %%i in ("%JarsDir%\*.jar") do set cp=!cp!;%%i
endlocal & set cp=%cp%

setlocal EnableDelayedExpansion
set JarsDir=lib
if defined cp (set cp=%cp%;.) else (set cp=.)
for %%i in ("%JarsDir%\*.jar") do set cp=!cp!;%%i
endlocal & set cp=%cp%

java -Xmx512m -splash:openbeat/OpenBEATsplash.png -cp %cp% is.ru.openbeat.Launcher