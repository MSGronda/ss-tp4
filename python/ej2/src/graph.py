import itertools
from matplotlib import pyplot as plt


def graph_energy_time(times, energies, labels):
    marker = itertools.cycle(('x', '+', '.', 'o', '*'))
    plt.figure(figsize=(10, 7))

    for (time, energy), label in zip(zip(times, energies), labels):
        plt.scatter(time, energy, marker=next(marker), s=5, label=label)

    plt.xlabel("Tiempo (s)")
    plt.ylabel("Error porcentual de energia del sistema")
    plt.yscale("log")

    plt.legend()
    plt.show()


def graph_mean_error(delta_t, mean_erros, std_errors):
    plt.errorbar(delta_t, mean_erros, std_errors, linestyle='None', marker='o', color='blue')
    plt.xlabel("$\Delta$ T (s)")
    plt.ylabel("Error porcentual medio de energia del sistema")

    plt.yscale("log")
    plt.xscale("log")

    plt.show()


def graph_variable_vs_time(times, variable, x_label, y_label):
    plt.scatter(times, variable, marker='o', s=5)

    plt.xlabel(x_label)
    plt.ylabel(y_label)

    plt.show()


