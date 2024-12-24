import numpy as np

# this script should be run after all tiles are processed with `process_tile`

def convert(parent_path):
    # parent_path: the path to all the flatfield/darkfield/baselines 

    # read all baselines and calculate the average 

    # for each tile, calculate and save the final S/T file:
    # image_corrected = (image - darkfield) / flatfield  - baseline + baseline_avg
	#   		      = image * (1/flatfield) + (baseline_avg - darkfield / flatfield - baseline) 
    # S = 1 / flatfield
    # T = baseline_avg - darkfield / flatfield - baseline

    # save the new S/T 

    return