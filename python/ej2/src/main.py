import numpy as np

from ej2.src.animation import animate, animate_parallel
from ej2.src.graph import *
from ej2.src.mission_data import get_distances_to_objective, get_speeds
from utils import *


def system_energy_vs_delta_t():
    times = []
    energies = []
    labels = []
    delta_t = []
    for filename in get_energy_files():
        time, energy = get_energy_data(filename)

        times.append(time)
        energies.append(energy)
        name = filename.removeprefix("../output-files\\energy-deltat-").removesuffix(".csv")
        labels.append(f"Delta T = {name} (s)")
        delta_t.append(float(name))

    graph_energy_time(times, energies, labels)

    mean_errors = np.mean(energies, axis=1)
    std_errors = np.std(energies, axis=1)
    graph_mean_error(delta_t, mean_errors, std_errors)


def generate_animation():
    time, body_data = get_body_data(get_all_files()[-1])
    properties = get_static_data(get_all_static_files()[-1])

    animate("../animations/simulation-video-start.mp4", body_data, properties, 1080)


def generate_starting_day_comparison():
    times, distances = get_min_distances('../output-files/starting-day-comparison.csv')
    graph_variable_vs_time(times, distances, "Dia de salida", "Distancia minima (km)")


def generate_mission_data():
    time, body_data = get_body_data(get_all_files()[-1])
    properties = get_static_data(get_all_static_files()[-1])

    distances = get_distances_to_objective(body_data, properties)
    graph_variable_vs_time(time, distances, "Tiempo (s)", "Distancia entre nave y marte (km)")

    speeds = get_speeds(body_data, properties)
    graph_variable_vs_time(time, speeds, "Tiempo (s)", "Modulo de velocidad (km/s)")


def generate_speed_comparison():
    times, distances = get_min_distances('../output-files/speed-comparison.csv')
    graph_variable_vs_time(times, distances, "Velocidad en modulo (km/s)", "Distancia minima (km)")


if __name__ == "__main__":
    system_energy_vs_delta_t()
    # generate_starting_day_comparison()
    # generate_animation()
    # generate_mission_dat1a()
    # generate_speed_comparison()
