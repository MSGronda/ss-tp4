import csv
import glob


X = 0
Y = 0


def get_all_files() -> [str]:
    return glob.glob("../output-files/bodies-*.csv")


def get_all_static_files() -> [str]:
    return glob.glob("../output-files/properties-*.csv")


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

            elif len(row) == 2:
                positions_for_timeframe.append((float(row[0]), float(row[1])))

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


def calc_plane_dimensions(body_data: [], properties: {}):

    # Marte siempre va a ser el mas lejano
    mars_idx = properties["MARS"]["index"]
    spaceship_idx = properties["SPACESHIP"]["index"]

    max_distance = 0

    # TODO: optimize using numpy
    for bodies in body_data:
        current_max_distance = max(bodies[mars_idx][X], bodies[mars_idx][Y], bodies[spaceship_idx][X], bodies[spaceship_idx][Y])

        if current_max_distance > max_distance:
            max_distance = current_max_distance

    return max_distance, max_distance


