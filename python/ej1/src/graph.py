import math

import numpy as np
from matplotlib import pyplot as plt
from utils import TIME, POSITION


def graph_positions(data, static_data, start: int = 0, end: int = -1):
    for d, static in zip(data, static_data):
        plt.scatter(d[TIME][start:end], d[POSITION][start:end], marker='o', s=5, label=static["type"])
    plt.xlabel(f'Tiempo (s)')
    plt.ylabel(f'Posicion (m)')
    plt.grid(True)
    plt.legend()
    plt.show()


def graph_error(data, static_data):
    for d, static in zip(data, static_data):
        error = []
        gamma = static["gamma"]
        mass = static["mass"]
        k = static["springConstant"]

        for aprox, time in zip(d[POSITION], d[TIME]):
            actual_value = math.exp(-(gamma / (2 * mass)) * time) * math.cos(math.sqrt(
                (k / mass) - ((gamma * gamma) / (4 * mass * mass))) * time)
            error.append(math.pow(aprox - actual_value, 2))

        average = np.mean(error)

        if static["type"] == "BEEMAN":
            color = "green"
        elif static["type"] == "VERLET":
            color = "blue"
        else:
            color = "red"

        plt.scatter(static["deltaT"], average, marker='o', s=5, color=color)

    plt.yscale("log")
    plt.xscale("log")

    plt.xlabel(f'Delta T (s)')
    plt.ylabel(f'Error cuadratico medio')
    plt.grid(True)
    # plt.legend()
    plt.show()
