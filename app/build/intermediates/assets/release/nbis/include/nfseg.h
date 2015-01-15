/*******************************************************************************

License: 
This software was developed at the National Institute of Standards and 
Technology (NIST) by employees of the Federal Government in the course 
of their official duties. Pursuant to title 17 Section 105 of the 
United States Code, this software is not subject to copyright protection 
and is in the public domain. It has been determined that the export control 
restriction did not apply to the NFSEG and BOZORTH3 software, due to both
being outside the scope of EAR(see Part 734.3 of the EAR for exact details); 
they are freely distributed and considered public domain. NIST assumes no 
responsibility  whatsoever for its use by other parties, and makes no 
guarantees, expressed or implied, about its quality, reliability, or any 
other characteristic. 

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

#ifndef _NFSEG_H
#define _NFSEG_H

#ifndef _JPEGL_H
#include <jpegl.h>
#endif
#ifndef _WSQ_H
#include <wsq.h>
#endif
#ifndef _DEFS_H
#include <defs.h>
#endif
#ifndef _MEMALLOC_H
#include <memalloc.h>
#endif
#ifndef _UTIL_H
#include <util.h>
#endif
#ifndef _DILATE_H
#include <dilate.h>
#endif
#ifndef _IMGSNIP_H
#include <imgsnip.h>
#endif
#ifndef _IMGAVG_H
#include <imgavg.h>
#endif
#ifndef _THRESH_H
#include <thresh.h>
#endif

#define BLK_DW          10
#define WHT_DW          4
#define ZERO_DW         24
#define OFF_DW1         160
#define OFF_DW2         45
#define OFF_STP         5
#define FIN_DW          6
#define EDGE_DW         5
#define FING_SPACE_MIN  25
#define FING_SPACE_MAX  60
#define FING_WIDTH_MIN  25
#define FING_HEIGHT_MIN 32
#define INBCNT          5000
#define Z_FAC           8.0
#define THR_PER         0.4
#define W_STP           5
#define TOP_LI          125

typedef struct {
   int tx, ty, bx, by;
} line_coords;

typedef struct {
   int tlx, tly;
   int trx, try;
   int blx, bly;
   int brx, bry;
   int sx, sy, sw, sh, nrsw, nrsh;
   float theta;
   int dty, dby, dlx, drx;
   int err;
} seg_rec_coords;

extern int segment_fingers(unsigned char *, const int, const int,
               seg_rec_coords **, const int, const int, const int, const int);
extern int dynamic_threshold(unsigned char *, const int, const int, const int,
               const int, const int);
extern void remove_lines(unsigned char *, const int, const int);
extern int accum_blk_wht(unsigned char *, const int, const int, int **, int **,
               int *, const int, const int, const int);
extern int find_digits(int *, int *, const int, int *, int *, const int,
               float *, int *, int *, const int);
extern void find_digit_edges(int *, const int, int *, int *, const int, int *,
               float *);
extern int get_edge_coords(const int, const int, int *, const int,
               line_coords *, const int);
extern int get_fing_boxes(const int, const int, const float, line_coords *,
               const int, seg_rec_coords *, const int);
extern void get_fing_seg_pars(const float, seg_rec_coords *, const int);
extern int get_segfing_bounds(unsigned char *, const int, const int,
               seg_rec_coords *, const int);
extern int accum_top_row(unsigned char *, const int, const int, int **, int **,
               int **, int *);
extern int accum_top_col_blk(unsigned char *, const int, const int, int **,
               int **, int **, int *);
extern int accum_top_col_wht(unsigned char *, const int, const int, int **,
               int **, int **, int *);
extern int get_top_score(int *, const int, const int, int *, int *, int *,
               const int, int *, int *, int *, const int, int *, int *, int *,
               const int, int *);
extern int adjust_top_up(int *, unsigned char *, const int, const int,
               const int, const int);
extern void find_segfing_bottom(seg_rec_coords *, const int, unsigned char *,
               const int, const int, const int, const int, const float);
extern void find_segfing_sides(seg_rec_coords *, const int, unsigned char *,
               const int, const int, const int);
extern void adjust_fing_seg_pars(seg_rec_coords *, const int);
extern void err_check_finger(int *, seg_rec_coords *, const int);
extern void scale_seg_fingers(seg_rec_coords *, const int, const int,
               const int, const int);
extern int parse_segfing(unsigned char ***, unsigned char *, const int,
               const int, seg_rec_coords *, const int, const int);
extern int write_parsefing(char *, const int, const int, const int, const int,
	       const int, unsigned char **, seg_rec_coords *, const int, 
	       const int);
extern int insert_parsefing(ANSI_NIST *const ansi_nist, const int imgrecord_i,
	       const int fgp, const seg_rec_coords *const fing_boxes,
	       const int nf, const int rot_search);

#endif /* !_NFSEG_H */
