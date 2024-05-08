import os
from multiprocessing import Pool, Manager, Process

import cv2
import numpy as np
from utils import X,Y, calc_plane_dimensions


def generate_center(body, plane_scale, offset):
    return int((body[X] + offset) * plane_scale), int((body[Y] + offset) * plane_scale)


def planet_data(properties: {}, plane_scale) -> []:
    resp = [0] * len(properties)

    # Earth
    earth_r = max(10, int(properties["EARTH"]["r"] * plane_scale))
    earth_idx = properties["EARTH"]["index"]
    earth_color = (255, 0, 0)

    resp[earth_idx] = (earth_r, earth_color)

    # Mars
    mars_r = max(6, int(properties["MARS"]["r"] * plane_scale))
    mars_idx = properties["MARS"]["index"]
    mars_color = (0, 0, 255)

    resp[mars_idx] = (mars_r, mars_color)

    # Spaceship
    spaceship_r = 4
    spaceship_idx = properties["SPACESHIP"]["index"]
    spaceship_color = (0, 255, 0)

    resp[spaceship_idx] = (spaceship_r, spaceship_color)

    # Jupiter (if present)
    if "JUPITER" in properties:
        jupiter_r = max(20, int(properties["JUPITER"]["r"] * plane_scale))
        jupiter_idx = properties["JUPITER"]["index"]
        jupiter_color = (80, 127, 255)

        resp[jupiter_idx] = (jupiter_r, jupiter_color)

    return resp


def animate(filename: str, times: [], body_data: [], properties: {}, video_width: int, starting: int = 0, ending: int = -1):
    fps = 30

    width, height = calc_plane_dimensions(body_data)
    offset = width

    plane_scale = (video_width - 15) / (2 * width)

    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(filename, fourcc, fps, (video_width, video_width))

    animation_properties = planet_data(properties, plane_scale)

    sun_pos = int(width * plane_scale)
    sun_r = max(35, int(properties["SUN"]["r"] * plane_scale))  # TODO: change
    sun_color = (0, 255, 255)

    for time, bodies in zip(times,body_data[starting:ending]):
        frame = np.ones((video_width, video_width, 3), dtype=np.uint8) * 255

        cv2.circle(frame, (sun_pos, sun_pos), radius=sun_r, color=sun_color, thickness=-1)

        for i, body in enumerate(bodies):
            body_property = animation_properties[i]
            cv2.circle(frame, generate_center(body, plane_scale, offset), radius=body_property[0],color=body_property[1], thickness=-1)

        text = str(time)
        cv2.putText(frame, text, (int(video_width / 2) - len(text) * 10, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 0),2, cv2.LINE_AA)

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



