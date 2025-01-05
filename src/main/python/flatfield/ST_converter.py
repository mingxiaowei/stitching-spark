import os
import argparse
import time
import numpy as np

parser = argparse.ArgumentParser(description="Convert existing flatfield, darkfield, and baseline to S/T terms for the stitching pipeline")
parser.add_argument("-p", "--path", required=True, type=str, help="Path to json config for all tiles for a channel, e.g. /path/to/stitching/c0-n5.json")
parser.add_argument("-v", "--verbose", action='store_true', default=False, help="Enable verbose output")

def convert_to_ST(input_channel_json_path, verbose=True):
    base_path = os.path.dirname(input_channel_json_path)