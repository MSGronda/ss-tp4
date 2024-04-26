from matplotlib import pyplot as plt
from utils import TIME, POSITION, NAME


def graph_positions(data):
    for d in data:
        plt.scatter(d[TIME], d[POSITION], marker='o', s=5, label=d[NAME])
    plt.xlabel(f'Tiempo (s)')
    plt.ylabel(f'Posicion (m)')
    plt.grid(True)
    plt.legend()
    plt.show()