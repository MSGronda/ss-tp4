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