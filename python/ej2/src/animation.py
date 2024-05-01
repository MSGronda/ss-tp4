from multiprocessing import Pool

import cv2
import numpy as np
from utils import X,Y, calc_plane_dimensions


def generate_center(body, plane_scale, offset):
    a = (body[X] + offset)
    b = (body[Y] + offset)
    return int(a * plane_scale), int((body[Y] + offset) * plane_scale)


def animate(body_data: [], properties: {}, video_width: int):
    fps = 60

    width, height = calc_plane_dimensions(body_data)
    offset = width

    plane_scale = video_width / (2 * width)

    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    video_width += 100
    out = cv2.VideoWriter('../animations/simulation_video.mp4', fourcc, fps, (video_width, video_width))

    # Sun
    sun_r = 200   # TODO: change
    sun_color = (0, 255, 255)
    sun_pos = int(width * plane_scale)

    # Earth
    earth_r = 50
    earth_idx = properties["EARTH"]["index"]
    earth_color = (255, 0, 0)

    # Mars
    mars_r = 25
    mars_idx = properties["MARS"]["index"]
    mars_color = (0, 0, 255)

    # Spaceship
    spaceship_r = 10
    spaceship_idx = properties["SPACESHIP"]["index"]
    spaceship_color = (0, 255, 0)

    for bodies in body_data:
        frame = np.ones((video_width, video_width, 3), dtype=np.uint8) * 255

        # Sun
        cv2.circle(frame, (sun_pos, sun_pos), radius=sun_r, color=sun_color, thickness=-1)

        # Earth
        a = generate_center(bodies[earth_idx], plane_scale, offset)
        cv2.circle(frame, a, radius=earth_r, color=earth_color, thickness=-1)

        # Mars
        a = generate_center(bodies[mars_idx], plane_scale, offset)
        cv2.circle(frame, generate_center(bodies[mars_idx], plane_scale, offset), radius=mars_r, color=mars_color, thickness=-1)

        # Spaceship
        cv2.circle(frame, generate_center(bodies[spaceship_idx], plane_scale, offset), radius=spaceship_r, color=spaceship_color, thickness=-1)

        out.write(frame)

    out.release()