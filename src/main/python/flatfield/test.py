import subprocess
import os
os.chdir(os.path.dirname(os.path.abspath(__file__)))
subprocess.run(["python", "process_tile.py", 
                "-p", "/data/sternsonlab/Zhenggang/2acq/outputs/M28C_LHA_S1/stitching/c0-n5-tile0.json", 
                "-v"])