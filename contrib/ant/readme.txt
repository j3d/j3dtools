              README for the Ant build scripts

Ant is an alternate build tool to Make for building the code. It is a project
of the Apache Foundation and part of the Jakarta project. You can find more
information and download it at:

http://jakarta.apache.org/ant/

To use this build.xml file, just add it to the $PROJECT_ROOT/code directory
and create a empty manifest.mf file in the same dir. Then type

ant compile-all 

in the code directory and it should build the binaries. Just typing "ant"
on the command line will give a complete list of options.

This code is contributed to the repository by:
Ståle Pedersen
staalep@ifi.uio.no
