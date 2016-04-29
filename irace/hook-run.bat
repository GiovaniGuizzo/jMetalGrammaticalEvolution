@ECHO OFF

IF NOT EXIST arena mkdir arena
cd arena

set arguments=%*
set arguments=%arguments:\=%

java -cp ../../dist/* org.uma.jmetal.irace.HookRun %arguments% 1> %2_STDOUT.txt 2> %2_STDERR.txt

exit %errorlevel%
