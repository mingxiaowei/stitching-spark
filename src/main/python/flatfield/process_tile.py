# adapted from https://github.com/peng-lab/BaSiCPy.git

import numpy as np
from basicpy import BaSiC
from scipy.ndimage import zoom
from matplotlib import pyplot as plt
from cv2 import resize

def process_tile(input, save_path):
    # estimate flatfield, darkfield, and baseline for input 
    input = np.asarray(input)
    basic_model = BaSiC(get_darkfield=True, smoothness_flatfield=1)
    basic_model.fit(input)

    # save flatfield, darkfield, and baseline as it is
    
