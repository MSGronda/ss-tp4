import math

from matplotlib import pyplot as plt
from utils import TIME, POSITION, NAME


def graph_positions(data, start: int = 0, end: int = -1):
    for d in data:
        plt.scatter(d[TIME][start:end], d[POSITION][start:end], marker='o', s=5, label=d[NAME])
    plt.xlabel(f'Tiempo (s)')
    plt.ylabel(f'Posicion (m)')
    plt.grid(True)
    plt.legend()
    plt.show()


def graph_error(data):
    actual_value = []
    for time in data[0][TIME]:
        # TODO:  cambiar a leer de un static data
        actual_value.append(math.exp(-(100/(2*70)) * time) * math.cos(math.sqrt((10000/70) - ((100 * 100) / (4 * 70*70))) * time))

    for d in data:
        error = []
        for aprox, actual in zip(d[POSITION], actual_value):
            error.append(math.pow(aprox - actual, 2))

        plt.scatter(d[TIME], error, marker='o', s=5, label=d[NAME])

    plt.xlabel(f'Tiempo (s)')
    plt.ylabel(f'Error cuadratico medio')
    plt.grid(True)
    plt.legend()
    plt.show()
