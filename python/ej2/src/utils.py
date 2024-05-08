import csv
import glob
import datetime

X = 0
Y = 1
VX = 2
VY = 3


def get_all_files() -> [str]:
    return glob.glob("../output-files/bodies-*.csv")


def get_all_static_files() -> [str]:
    return glob.glob("../output-files/properties-*.csv")


def get_energy_files() -> [str]:
    return glob.glob("../output-files/energy-deltat-*.csv")


def get_energy_data(filename:str):
    time = []
    energy = []

    with open(filename, 'r') as file:
        reader = csv.reader(file)

        for row in reader:
            time.append(float(row[0]))
            energy.append(float(row[1]))

    return time, energy


def get_min_distances(filename:str):
    time = []
    distances = []

    with open(filename, 'r') as file:
        reader = csv.reader(file)

        for row in reader:
            time.append(float(row[0]))
            distances.append(float(row[1]))

    return time, distances


def get_dates(times: [], starting_date: int):
    return [datetime.datetime.fromtimestamp(t + starting_date) for t in times]


def get_body_data(filename: str):
    time = []
    positions = []

    positions_for_timeframe = None

    with open(filename, 'r') as file:
        reader = csv.reader(file)

        for row in reader:
            if len(row) == 1:
                time.append(float(row[0]))

                if positions_for_timeframe is not None:
                    positions.append(positions_for_timeframe)

                positions_for_timeframe = []

            elif len(row) == 4:
                positions_for_timeframe.append((float(row[X]), float(row[Y]), float(row[VX]), float(row[VY])))

    # El ultimo valor
    if positions_for_timeframe is not None:
        positions.append(positions_for_timeframe)

    return time, positions


def get_static_data(filename: str):
    resp = {}
    with open(filename, 'r') as file:
        reader = csv.reader(file)

        for row in reader:

            if len(row) == 2:       # Datos de simulacion
                resp[row[0]] = float(row[1])

            elif len(row) == 4:     # Datos de planeta
                resp[row[0]] = {"index": int(row[1]), "m": float(row[2]), "r": float(row[3])}

    return resp


def calc_plane_dimensions(body_data: []):

    max_distance = 0

    # TODO: optimize using numpy
    for bodies in body_data:
        for body in bodies:
            current_max_distance = max(abs(body[X]), abs(body[Y]))

            if current_max_distance > max_distance:
                max_distance = current_max_distance

    return max_distance, max_distance


