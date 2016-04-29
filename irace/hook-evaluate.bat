@ECHO OFF

java -cp ../../dist/* org.uma.jmetal.irace.HookEvaluate %2 > HV_%2.txt

set /p cost=<HV_%2.txt

echo -%cost%

del FUN_%2.txt
del VAR_%2.txt
del TIME_%2.txt
del HV_%2.txt
del %2_STDERR.txt
del %2_PARAMS.txt
del %2_STDOUT.txt

exit %errorlevel%
