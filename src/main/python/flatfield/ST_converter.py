import os
import argparse
import json
import numpy as np
from tifffile import imwrite 
from utils import *

parser = argparse.ArgumentParser(description="Convert existing flatfield, darkfield, and baseline to S/T terms for the stitching pipeline")
parser.add_argument("-p", "--path", required=True, type=str, help="Path to json config for all tiles for a channel, e.g. /path/to/stitching/c0-n5.json")
parser.add_argument("-v", "--verbose", action='store_true', default=False, help="Enable verbose output")

all_fields = ['baseline', 'flatfield', 'darkfield']

def stack_2d_to_3d(arr_2d, z):
    return np.repeat(arr_2d[:, :, np.newaxis], z, axis=2)

def stack_1d_to_3d(arr_1d, x, y):
    return np.broadcast_to(arr_1d[np.newaxis, np.newaxis, :],  (x, y, len(arr_1d))) 

def convert_to_ST(input_channel_json_path, verbose=True):
    # for each tile, calculate and save the final S/T file:
    # image_corrected = (image - darkfield) / flatfield  - baseline + baseline_avg
    #   		      = image * (1 / flatfield) + (baseline_avg - darkfield / flatfield - baseline) 
    # S = 1 / flatfield
    # T = baseline_avg - darkfield / flatfield - baseline

    with open(input_channel_json_path, 'r') as file:
        input_channel_json = json.load(file)

    tiles_cnt = len(input_channel_json)
    x, y, z = input_channel_json[0]['size']
    if verbose:
        print(f"Tile dimensions: x = {x}, y = {y}, z = {z}")
        
    per_tile_base_path = get_per_tile_base_path(input_channel_json_path)
    avg_baseline = np.load(os.path.join(per_tile_base_path, 'avg_baseline.npy'))
    avg_baseline_3d = stack_1d_to_3d(avg_baseline, x, y)

    for tile_index in range(tiles_cnt):
        if verbose:
            print(f"Processing tile {tile_index} of {tiles_cnt}")
        tile_dir = get_per_tile_folder_path(input_channel_json_path, tile_index)
        os.chdir(tile_dir)

        tile_fields = {field: np.load(f"{field}.npy") for field in all_fields}
        baseline_3d = stack_1d_to_3d(tile_fields['baseline'], x, y)
        df_over_ff_3d = stack_2d_to_3d(tile_fields['darkfield'] / tile_fields['flatfield'], z)
        S_1d = 1 / tile_fields['flatfield']
        T_3d = baseline_3d - avg_baseline_3d - df_over_ff_3d

        imwrite("S.tif", S_1d)
        imwrite("T.tif", T_3d)
        if verbose:
            print(f"S/T converted and saved for {tile_dir}")
        del tile_fields, baseline_3d, df_over_ff_3d, S_1d, T_3d

if __name__ == "__main__":
    args = parser.parse_args()
    convert_to_ST(args.path, args.verbose)
