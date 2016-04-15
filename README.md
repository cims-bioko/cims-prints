# OpenANDID

FOSS Biometric Identification Tools for Android; primarily geared towards global health applications.

Copyright (c) 2012-2016, Shawn Sarwar and contributors. All rights reserved.

OpenANDID was created by Biometrac LLC [Shawn Sarwar, Trisha Finnegan, Heath Gross] as proprietary software under the name BiometracCore.
OpenANDID is now available under a GPLv3 License. Any linked libraries may contain their own licenses.

# Requirements:

  Scanning:
    Lumidigm Mercury 301-00 scanner is the only one supported. New scanners can be integrated by subclassing Scanner and providing usb instructions. USB Host mode is required of the Android Device. Android 4.4+ is supported.
  
  Matching:
    OpenANDID Engine is a fork of SourceAFIS, optimized for android. Templates sent via intent must be in ISO format.
    NBIS Bozorth3 is also included in the package in /assets as a binary, but no examples are currently included.


# Current API for calls via Intent:
  
  com.openandid.core.SCAN
  
      call multiple scans with a single intent. Starting with 0 index add a "_n" to the end of each key to group them together into screens of two scans.
    
    InputKeys:
    
      Inputs for the first scan:
      
        prompt_0 : A string scanning prompt for the user
        left_finger_assignment_0 : Which finger you want in the left pane. Valid values are in the format hand_finger, i.e. "left_index" or "right_ring"
        right_finger_assignment_0 : "
        easy_skip_0 : Allows the user to leave the scanning screen without scanning both fingers. Values are "true" or "false"
        
        Convention follows as "prompt_1" for the prompt on the second consecutive scanning screen.
      
      ReturnKeys:
        For the return, you'll get back extras labeled with the values for left and right finger assigments. For example, if you put "left_index" as your "left_finger_assignment_0" then you'll have a "left_index" key in the return bundle. The value of this is the fingerprint template as a hex string.


  com.openandid.core.IDENTIFY

      Try to identify a set of templates within the cached set. Returns the best matches which meet the score threshold.
    
    InputKeys:
      left_thumb : hex string of the template for the left thumb
      right_thumb: "
      left_index: "
      right_index: "
      max_matches : maximum number of matches to return 
    
    ReturnKeys:
      matches_found: integer number of matches found
      match_id_0 : uuid (case_id here) of best match
      match_score_0 : score of best match
      match_id_1: uuid of second best match
      match_score_1: score of second best match
      match_id_...
      match_score_...
      
      Returned scores and ids are limited by max_matches [max 10] or the number of matching records. If there are no matches, matches_found will return 0, but the other fields won't be populated.


  com.openandid.core.PIPE

    Chains together other API calls carrying along intermediate results until the final return. I.E. you can pipe SCAN results of 1 or more scanning screens directly into an IDENTIFY call. Requires all required keys not to be populated by intermediate actions to be passed initially.
  
    InputKeys:
    
      action_0: full qualified address of intent to be called (Limited to valid com.openandid namespace)
      action_1: "
      action_n
      
      Other keys as needed for intents called in action.
      
      Here's a .PIPE example for our current project that scans four fingers and passed them to identify. All outputs and inputs are passed back to the called on return.
      
      Intent i = new Intent("com.openandid.core.PIPE");
      i.putExtra("action_0", "com.openandid.core.SCAN");
      i.putExtra("action_1", "com.openandid.core.IDENTIFY")
      i.putExtra("prompt_0", "Scan the Index Fingers")
      i.putExtra("left_finger_assignment_0", "left_index");
      i.putExtra("right_finger_assignment_0", "right_index");
      i.putExtra("easy_skip_0", "false");
      i.putExtra("prompt_0", "Scan the Thumbs");
      i.putExtra("left_finger_assignment_1", "left_thumb");
      i.putExtra("right_finger_assignment_1", "right_thumb");
      i.putExtra("easy_skip_1", "false");
