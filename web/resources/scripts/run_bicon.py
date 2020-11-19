#!/usr/bin/env python3

from bicon import data_preprocessing
from bicon import BiCoN
from bicon import results_analysis

import sys

path_expr = sys.argv[1]
path_net = sys.argv[2]

GE, G, labels, _ = data_preprocessing(path_expr, path_net)
L_g_min = 10
L_g_max = 15

model = BiCoN(GE, G, L_g_min, L_g_max)
solution, scores = model.run_search()

results = results_analysis(solution, labels)
print(results.genes1)

