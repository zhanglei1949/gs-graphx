import networkx.algorithms.tests.test_clique
import pytest

from graphscope.nx.utils.compat import import_as_graphscope_nx

import_as_graphscope_nx(networkx.algorithms.tests.test_clique,
                        decorators=pytest.mark.usefixtures("graphscope_session"))
