# adapted from https://github.com/peng-lab/BaSiCPy.git

import numpy as np
import json
import os
import zarr
from basicpy import BaSiC

def process_tiles(input_channel_json_path):
    # json_path: path to json config for all tiles for each channel
    # estimate flatfield, darkfield, and baseline for input  
    with open(input_channel_json_path, 'r') as file:
        input_channel_json = json.load(file)
    stitching_dir = os.path.dirname(input_channel_json_path)
    flatfield_dir = os.path.join(stitching_dir, "per_tile_flatfields")
    channel_name = os.path.basename(os.path.dirname(input_channel_json[0]["file"]))
    channel_dir = os.path.join(flatfield_dir, channel_name)
    os.makedirs(channel_dir, exist_ok=True)

    all_baselines = []

    for tile_info_dict in input_channel_json:

        tile_input_path = tile_info_dict.get("file", None)
        if tile_input_path is None or not os.path.exists(tile_input_path):
            print(f"File not found: {tile_input_path}\nSkipping...")
            continue

        tile_num = int(tile_info_dict.get("index", -1))
        tile_dir = os.path.join(channel_dir, f"tile{tile_num}")
        os.makedirs(tile_dir, exist_ok=True)

        tile_input_arr = np.asarray(zarr.open(store=zarr.N5Store(tile_input_path), mode='r')[...])
        basic_model = BaSiC(get_darkfield=True, smoothness_flatfield=1)
        basic_model.fit(tile_input_arr)

        os.chdir(tile_dir)
        np.save("flatfield.npy", basic_model.flatfield)
        np.save("darkfield.npy", basic_model.darkfield)
        np.save("baseline.npy", basic_model.baseline)
        all_baselines.append(basic_model.baseline.copy())

        del tile_input_arr, basic_model
    
    avg_baseline = np.mean(all_baselines, axis=0)
    os.chdir(channel_dir)
    np.save("avg_baseline.npy", avg_baseline)
    # save flatfield, darkfield, and baseline as it is
    
