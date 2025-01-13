import os 
import json

def get_per_tile_base_path(input_channel_json_path, input_channel_json=None):
    if input_channel_json is None:
        with open(input_channel_json_path, 'r') as file:
            input_channel_json = json.load(file)
    stitching_dir = os.path.dirname(input_channel_json_path)
    flatfield_dir = os.path.join(stitching_dir, "per_tile_flatfields")
    channel_name = os.path.basename(os.path.dirname(input_channel_json[0]["file"]))
    channel_dir = os.path.join(flatfield_dir, channel_name)
    return channel_dir

def get_per_tile_folder_path(input_channel_json_path, tile_index, input_channel_json=None):
    if input_channel_json is None:
        with open(input_channel_json_path, 'r') as file:
            input_channel_json = json.load(file)
    total_tile_cnt = len(input_channel_json)
    assert 0 <= tile_index < total_tile_cnt, f"Tile index {tile_index} is out of range for {total_tile_cnt} tiles"
    channel_dir = get_per_tile_base_path(input_channel_json_path, input_channel_json)
    tile_dir = os.path.join(channel_dir, f"tile{tile_index}")
    return tile_dir