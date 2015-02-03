/*******************************************************************************

License: 
This software was developed at the National Institute of Standards and 
Technology (NIST) by employees of the Federal Government in the course 
of their official duties. Pursuant to title 17 Section 105 of the 
United States Code, this software is not subject to copyright protection 
and is in the public domain. NIST assumes no responsibility  whatsoever for 
its use by other parties, and makes no guarantees, expressed or implied, 
about its quality, reliability, or any other characteristic. 

This software has been determined to be outside the scope of the EAR
(see Part 734.3 of the EAR for exact details) as it has been created solely
by employees of the U.S. Government; it is freely distributed with no
licensing requirements; and it is considered public domain.  Therefore,
it is permissible to distribute this software as a free download from the
internet.

Disclaimer: 
This software was developed to promote biometric standards and biometric
technology testing for the Federal Government in accordance with the USA
PATRIOT Act and the Enhanced Border Security and Visa Entry Reform Act.
Specific hardware and software products identified in this software were used
in order to perform the software development.  In no case does such
identification imply recommendation or endorsement by the National Institute
of Standards and Technology, nor does it imply that the products and equipment
identified are necessarily the best available for the purpose.  

*******************************************************************************/


#ifndef _USAGEMCS_H
#define _USAGEMCS_H

/* Include line added by MDG on 05/09/2005 */
#include <little.h>

/* Two macros that make it easier to produce main program source codes
that write usage messages in case of incorrect numbers of arguments.
CAUTION: For these macros to work correctly, the "argument count" and
"argument vector" parameters of main() must be called argc and argv
(their usual names). */


/*******************************************************************/

/* A macro for use with a command that has a fixed number of
arguments.  Example of how to use it: if correct usage of the command
foobar is "foobar foo bar", then let the first line of the main
program source code be a call of the Usage macro with a string
showing the usage, but omitting the comand name:
  Usage("foo bar");
If the arg count is not one more than the number of words in the
provided string, then the result is the writing of a usage message to
stderr (for this example, "ERROR: Usage: foobar foo bar") and an
exit(1). */

#define Usage(str) Usage_func(argc, argv[0], str)

/*******************************************************************/

/* A macro for use with a command that has an unfixed number of
arguments.  To use it, check whether argc indicates that the number
of args cannot be right, and if so call the usage macro with a
string showing the usage, but omitting the command name.  For example,
if the correct usage of the command barfoo is
"barfoo bar[bar..] foo[foo..]", then begin the main program source
code with:
  if(!(argc >= 3 && (argc & 1)))
    usage("bar[bar]... foo[foo]...");
If argc indicates incorrect usage, the usage macro will be called and
the result will be the writing of a usage message to stderr (for this
example, "ERROR: Usage: barfoo bar[bar]... foo[foo]...") and an
exit(1). */

#define usage(str) usage_func(argv[0], str)

/*******************************************************************/

#endif /* !_USAGEMCS_H */
