import math

from utils import X, Y, VX, VY


def calc_distance(body1, body2):
    rx = (body1[X] - body2[X])
    ry = (body1[Y] - body2[Y])

    return math.sqrt(rx**2 + ry**2)


def calc_speed(body):
    return math.sqrt(body[VX]**2 + body[VY]**2)


def get_distances_to_objective(body_data: [], properties: {}) -> []:
    mars_idx = properties["MARS"]["index"]
    spaceship_idx = properties["SPACESHIP"]["index"]

    distances = []

    for bodies in body_data:
        distances.append(calc_distance(bodies[mars_idx], bodies[spaceship_idx]))

    return distances


def get_speeds(body_data: [], properties: {}) -> []:
    spaceship_idx = properties["SPACESHIP"]["index"]

    speeds = []

    for bodies in body_data:
        speeds.append(calc_speed(bodies[spaceship_idx]))

    return speeds
