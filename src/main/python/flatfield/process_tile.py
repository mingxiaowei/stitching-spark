# adapted from https://github.com/peng-lab/BaSiCPy.git
import json
import os
import zarr
import argparse
import time
import numpy as np
from basicpy import BaSiC
from PIL import Image

parser = argparse.ArgumentParser(description="Process tiles for flatfield, darkfield, and baseline estimation")
parser.add_argument("-p", "--path", required=True, type=str, help="Path to json config for all tiles for a channel")
parser.add_argument("-v", "--verbose", action='store_true', default=False, help="Enable verbose output")

def process_tiles(input_channel_json_path, verbose=True):
    # json_path: path to json config for all tiles for each channel
    # estimate flatfield, darkfield, and baseline for input  
    with open(input_channel_json_path, 'r') as file:
        input_channel_json = json.load(file)
    stitching_dir = os.path.dirname(input_channel_json_path)
    flatfield_dir = os.path.join(stitching_dir, "per_tile_flatfields")
    channel_name = os.path.basename(os.path.dirname(input_channel_json[0]["file"]))
    channel_dir = os.path.join(flatfield_dir, channel_name)
    os.makedirs(channel_dir, exist_ok=True)
    if verbose:
        print(f'Channnel folder created: {channel_dir}')

    all_baselines = []
    all_tile_dirs = []
    base_n5_path = input_channel_json[0]["file"].split('.n5')[0] + '.n5'
    if verbose:
        print(f'base_n5_path: {base_n5_path}')
    zarr_data = zarr.convenience.open(store=zarr.N5Store(base_n5_path), mode='r')

    for tile_info_dict in input_channel_json:

        tile_input_path = tile_info_dict.get("file", None)
        if tile_input_path is None or not os.path.exists(tile_input_path):
            if verbose:
                print(f"File not found: {tile_input_path}\nSkipping...")
            continue

        tile_num = int(tile_info_dict.get("index", -1))
        tile_dir = os.path.join(channel_dir, f"tile{tile_num}")
        os.makedirs(tile_dir, exist_ok=True)
        all_tile_dirs.append(tile_dir)
        if verbose:
            print(f'Tile folder created: {tile_dir}')
            print(f'tile_input_path: {tile_input_path}')
        # zarr_data = zarr.open(store=zarr.N5Store(tile_input_path), mode='r')
        n5_subpath = tile_input_path.split('.n5')[-1]
        t0 = time.time()
        tile_input = zarr_data[n5_subpath][...]
        tile_input_arr = np.asarray(tile_input)
        t1 = time.time()
        if verbose:
            print(f"Time to load tile {tile_num}: {t1-t0}")
        basic_model = BaSiC(get_darkfield=True, smoothness_flatfield=1)
        basic_model.fit(tile_input_arr)
        t2 = time.time()
        if verbose:
            print(f"Time to fit a BaSiC model to tile {tile_num}: {t2-t1}")

        os.chdir(tile_dir)
        np.save("flatfield.npy", basic_model.flatfield)
        np.save("darkfield.npy", basic_model.darkfield)
        np.save("baseline.npy", basic_model.baseline)
        all_baselines.append(basic_model.baseline.copy())
        if verbose:
            print(f"Tile {tile_num} processed")

        del tile_input_arr, basic_model
    
    avg_baseline = np.mean(all_baselines, axis=0)
    os.chdir(channel_dir)
    np.save("avg_baseline.npy", avg_baseline)
    
    # for each tile, calculate and save the final S/T file:
    # image_corrected = (image - darkfield) / flatfield  - baseline + baseline_avg
    #   		      = image * (1 / flatfield) + (baseline_avg - darkfield / flatfield - baseline) 
    # S = 1 / flatfield
    # T = baseline_avg - darkfield / flatfield - baseline
    for tile_dir in all_tile_dirs:
        os.chdir(tile_dir)
        baseline = np.load("baseline.npy")
        darkfield = np.load("darkfield.npy")
        flatfield = np.load("flatfield.npy")
        S = 1 / flatfield
        T = avg_baseline - darkfield / flatfield - baseline
        Image.fromarray(S).save("S.tif")
        Image.fromarray(T).save("T.tif")
        if verbose:
            print(f"S/T converted and saved for {tile_dir}")

if __name__ == "__main__":
    args = parser.parse_args()
    process_tiles(args.path, args.verbose)
