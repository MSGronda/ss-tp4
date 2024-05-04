from ej2.src.animation import animate, animate_parallel
from ej2.src.graph import *
from utils import *

if __name__ == "__main__":

    #times = []
    #energies = []
    #labels = []
    #for filename in get_energy_files():
    #    time, energy = get_energy_data(filename)

    #    times.append(time)
    #    energies.append(energy)
    #    name = filename.removeprefix("../output-files\\energy-deltat-").removesuffix(".csv")
    #    labels.append(f"Delta T = {name} (s)")


   # graph_energy_time(times, energies, labels)

    #time, body_data = get_body_data(get_all_files()[-1])
    #properties = get_static_data(get_all_static_files()[-1])

    #animate("../animations/simulation-video-start.mp4", body_data, properties, 1080, 0, -1)

    times, distances = get_min_distances('../output-files/min-distances.csv')
    graph_min_distance(times, distances)


