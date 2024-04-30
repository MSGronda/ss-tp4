import csv
import glob


def get_all_files() -> [str]:
    return glob.glob("../output-files/particle-movement-*.csv")


def get_all_static_files() -> [str]:
    return glob.glob("../output-files/static-data-*.csv")


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

            elif len(row) == 3:
                positions_for_timeframe.append((float(row[0]), float(row[1])))

    return time, positions
