@echo off
FOR  %%f IN (*.po) DO "C:\Program Files (x86)\Poedit\GettextTools\bin\msgcat" --properties-output %%f -o %%fPROP
ren *.poPROP *.properties
PAUSE