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
    actual_value = []
    static = static_data[0]
    for time in data[0][TIME]:
        actual_value.append(math.exp(-(static["gamma"]/(2*static["mass"])) * time) * math.cos(math.sqrt(
            (static["springConstant"]/static["mass"]) - ((static["gamma"] * static["gamma"]) / (4 * static["mass"]*static["mass"]))
        ) * time))

    for d, static in zip(data, static_data):
        error = []
        for aprox, actual in zip(d[POSITION], actual_value):
            error.append(math.pow(aprox - actual, 2))

        average = np.mean(error) / len(error)

        plt.scatter(static["deltaT"], average, marker='o', s=5, label=static["type"])

    plt.xlabel(f'Delta T (s)')
    plt.ylabel(f'Error cuadratico medio')
    plt.grid(True)
    plt.legend()
    plt.show()
