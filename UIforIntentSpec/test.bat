@echo off
for %%i in (%1\*) do java -cp "bin;lib/*;tool/*" kr_ac_yonsei_mobilesw_UI.MainByCommand -mode 0 -count 20 -device 1 %%i
echo on