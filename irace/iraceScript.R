valueArg <- commandArgs(TRUE)[1]
valueArg <- paste("--exec-dir=",valueArg)
library(irace)
irace.cmdline(valueArg)
