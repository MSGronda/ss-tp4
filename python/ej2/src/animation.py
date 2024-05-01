import os
from multiprocessing import Pool, Manager, Process

import cv2
import numpy as np
from utils import X,Y, calc_plane_dimensions


def generate_center(body, plane_scale, offset):
    return int((body[X] + offset) * plane_scale), int((body[Y] + offset) * plane_scale)


def animate(filename: str, body_data: [], properties: {}, video_width: int, starting: int = 0, ending: int = -1):
    fps = 30

    width, height = calc_plane_dimensions(body_data)
    offset = width

    plane_scale = video_width / (2 * width)

    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(filename, fourcc, fps, (video_width, video_width))

    # Sun
    sun_r = max(20, int(properties["SUN"]["r"] * plane_scale))  # TODO: change
    sun_color = (0, 255, 255)
    sun_pos = int(width * plane_scale)

    # Earth
    earth_r = max(6, int(properties["EARTH"]["r"] * plane_scale))
    earth_idx = properties["EARTH"]["index"]
    earth_color = (255, 0, 0)

    # Mars
    mars_r = max(4, int(properties["MARS"]["r"] * plane_scale))
    mars_idx = properties["MARS"]["index"]
    mars_color = (0, 0, 255)

    # Spaceship
    spaceship_r = 2
    spaceship_idx = properties["SPACESHIP"]["index"]
    spaceship_color = (0, 255, 0)

    for bodies in body_data[starting:ending]:
        frame = np.ones((video_width, video_width, 3), dtype=np.uint8) * 255

        # Sun
        cv2.circle(frame, (sun_pos, sun_pos), radius=sun_r, color=sun_color, thickness=-1)

        # Earth
        cv2.circle(frame, generate_center(bodies[earth_idx], plane_scale, offset), radius=earth_r, color=earth_color, thickness=-1)

        # Mars
        cv2.circle(frame, generate_center(bodies[mars_idx], plane_scale, offset), radius=mars_r, color=mars_color, thickness=-1)

        # Spaceship
        cv2.circle(frame, generate_center(bodies[spaceship_idx], plane_scale, offset), radius=spaceship_r, color=spaceship_color, thickness=-1)

        out.write(frame)

    out.release()


def animate_for_parallel(args):
    animate(*args)


def combine_videos(input_files, output_file, screen_size: int):
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    output_video = cv2.VideoWriter(output_file, fourcc, 30.0, (screen_size, screen_size))

    for file in input_files:
        input_video = cv2.VideoCapture(file)

        while True:
            ret, frame = input_video.read()
            if not ret:
                break
            output_video.write(frame)

        input_video.release()

    output_video.release()


def animate_parallel(body_data: [], properties: {}, video_width: int, num_process: int):
    frames_per_process = int(len(body_data) / num_process)

    args = []
    for i in range(num_process):
        args.append((f"../animations/simulation-video-{i}.mp4", body_data, properties, video_width, i * frames_per_process, (i+1) * frames_per_process))

    with Pool() as pool:
        pool.map(animate_for_parallel, args)
        pool.close()
        pool.join()

    combination_files = [arg[0] for arg in args]

    combine_videos(combination_files, "../animations/simulation-video.mp4", video_width)

    for arg in args:
        os.remove(arg[0])



