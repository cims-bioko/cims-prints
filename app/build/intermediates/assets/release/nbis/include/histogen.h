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

/******************************************************************************
      PACKAGE: ANSI/NIST 2007 Standard Reference Implementation

      FILE:    HISTOGEN.H

      AUTHORS: Bruce Bandini
      DATE:    05/18/2010

*******************************************************************************/
#ifndef _HISTOGEN_H
#define _HISTOGEN_H

#define str_eq(s1,s2)  (!strcmp ((s1),(s2)))

/* If filemask = *, then getopt adds files in current dir to non-options list.
   For a correctly formed command line, argc is always <= 4. */
#define MAX_ARGC    4
#define NUM_OPTIONS 4

#define CMD_LEN 512
#define FILESYS_PATH_LEN 256
#define READ_LINE_BUFFER 256
#define MAX_FIELD_NUM_CHARS 12
#define MAX_FIELD_NUMS 30
#define ALLOC_BLOCK_SIZE 10
#define HISTOGEN_LOG_FNAME "histogen.log"

typedef struct histo HISTO;
struct histo {
  char  field_num[12];
  int   count;
  HISTO *next;
};

HISTO *histo_head;

enum {
  INCLUDE_INVALID_FILES=5,
  INCLUDE_FIELD_SEPARATORS,
  INCLUDE_NEWLINE_CHARS,
  INCLUDE_SPACE_CHARS
};

enum {
  FALSE=0,
  TRUE=1
};

/******************************************************************************/
/* histogen.c */
extern int process_file(const char *);
extern int initialize_linked_list();
extern int output_linked_list(FILE *);

#endif /* !_HISTOGEN_H */

