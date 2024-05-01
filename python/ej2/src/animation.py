from multiprocessing import Pool

import cv2
import numpy as np
from utils import X,Y, calc_plane_dimensions


def generate_center(body, plane_scale, offset):
    return int((body[X] + offset) * plane_scale), int((body[Y] + offset) * plane_scale)


def animate(body_data: [], properties: {}, video_width: int):
    fps = 120

    width, height = calc_plane_dimensions(body_data)
    offset = width

    plane_scale = video_width / (2 * width)

    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter('../animations/simulation_video.mp4', fourcc, fps, (video_width, video_width))

    # Sun
    sun_r = max(10, int(100 * plane_scale))  # TODO: change
    sun_color = (0, 255, 255)
    sun_pos = int(width * plane_scale)

    # Earth
    earth_r = max(3, int(25 * plane_scale))
    earth_idx = properties["EARTH"]["index"]
    earth_color = (255, 0, 0)

    # Mars
    mars_r = max(2, int(10 * plane_scale))
    mars_idx = properties["MARS"]["index"]
    mars_color = (0, 0, 255)

    # Spaceship
    spaceship_r = max(2, int(5 * plane_scale))
    spaceship_idx = properties["SPACESHIP"]["index"]
    spaceship_color = (0, 255, 0)

    for bodies in body_data:
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
