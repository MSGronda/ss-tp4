from multiprocessing import Pool

import cv2
import numpy as np
from utils import X,Y, calc_plane_dimensions


def generate_center(body, plane_multiplier, offset):
    return int((body[X] + offset) * plane_multiplier), int((body[Y] + offset) * plane_multiplier)


def animate(body_data: [], properties: {}, plane_scale: float):
    fps = 60

    width, height = calc_plane_dimensions(body_data, properties)
    offset = width / 2
    width, height = int(width * plane_scale), int(height * plane_scale)

    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter('../animations/simulation_video.mp4', fourcc, fps, (width, height))

    # Sun
    sun_r = 300   # TODO: change
    sun_color = (0, 255, 255)
    sun_pos = int((width + offset) * plane_scale)

    # Earth
    earth_r = 50
    earth_idx = properties["EARTH"]["index"]
    earth_color = (0, 0, 255)

    # Mars
    mars_r = 25
    mars_idx = properties["MARS"]["index"]
    mars_color = (255, 0, 0)

    # Spaceship
    spaceship_r = 10
    spaceship_idx = properties["SPACESHIP"]["index"]
    spaceship_color = (0, 255, 0)

    for bodies in body_data:
        frame = np.ones((height, width, 3), dtype=np.uint8) * 255

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