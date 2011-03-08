import optparse
from cluster import *

parser = optparse.OptionParser(usage="%prog [OPTIONS]")
parser.add_option("-f", "--nodes-file", dest="nodes",
                  action="store", type="string", default="/etc/nodes",
                  help="loads nodes from NODES (default '/etc/nodes')")
(options, args) = parser.parse_args()

if len(args) > 0 :
    parser.print_help()
    sys.exit(1)

for node in Nodes(options.nodes) :
    print node

