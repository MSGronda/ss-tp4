import math

import numpy as np
from matplotlib import pyplot as plt
from utils import TIME, POSITION


def graph_positions(data, static_data, start: int = 0, end: int = -1):
    actual_values = []
    times = []
    for d, static in zip(data, static_data):
        if static["type"] == "BEEMAN":
            gamma = static["gamma"]
            mass = static["mass"]
            k = static["springConstant"]
            for time in d[TIME]:
                times.append(time)
                actual_values.append(math.exp(-(gamma / (2 * mass)) * time) * math.cos(math.sqrt(
                    (k / mass) - ((gamma * gamma) / (4 * mass * mass))) * time))

        plt.scatter(d[TIME][start:end], d[POSITION][start:end], marker='o', s=5, label=static["type"])

    plt.scatter(times[start:end], actual_values[start:end], marker="_", s=5, label="Solución Analítica")
    plt.xlabel(f'Tiempo (s)')
    plt.ylabel(f'Posición (m)')
    plt.grid(True)
    plt.legend()
    plt.show()


def graph_error(data, static_data):
    verlet = []
    beeman = []
    gear = []
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
            beeman.append((static["deltaT"], average))
        elif static["type"] == "VERLET":
            verlet.append((static["deltaT"], average))
        else:
            gear.append((static["deltaT"], average))

    beeman.sort(key=lambda x: x[0], reverse=True)
    verlet.sort(key=lambda x: x[0], reverse=True)
    gear.sort(key=lambda x: x[0], reverse=True)

    times = [time for time, _ in beeman]
    beeman_values = [value for _, value in beeman]
    verlet_values = [value for _, value in verlet]
    gear_values = [value for _, value in gear]

    plt.scatter(times, beeman_values, marker='o', s=15, color="green", label="BEEMAN")
    plt.scatter(times, verlet_values, marker='o', s=15, color="blue", label="VERLET")
    plt.scatter(times, gear_values, marker='o', s=15, color="red", label="GEAR PREDICTOR CORRECTOR")

    plt.plot(times, beeman_values, color="green")
    plt.plot(times, verlet_values, color="blue")
    plt.plot(times, gear_values, color="red")

    plt.yscale("log")
    plt.xscale("log")

    plt.xlabel(f'Δt (s)')
    plt.ylabel(f'ECM (m²)')
    plt.grid(True)
    plt.legend()
    plt.show()
