import sys, optparse
from cluster import *

parser = optparse.OptionParser(usage="%prog [OPTIONS] COMMAND...")
parser.add_option("-f", "--nodes-file", dest="nodes",
                  action="store", type="string", default="/etc/nodes",
                  help="loads nodes from NODES (default '/etc/nodes')")
parser.add_option("-q", "--quiet", dest="quiet",
                  action="store_true", default=False,
                  help="hide output on success")
(options, args) = parser.parse_args()

if len(args) == 0 :
    parser.print_help()
    sys.exit(1)

nodes = Nodes(options.nodes)

try :
    success, error = (nodes.ssh_all(" ".join(args)), {})
except ClusterError :
    e = sys.exc_info()[1]
    success, error = (e.success, e.error)

def _cmp (a, b) :
    try :
        return int(a[4:]) - int(b[4:])
    except :
        return cmp(a, b)

if not options.quiet :
    for node in sorted(success.keys(), cmp=_cmp) :
        lines = success[node].rstrip().split("\n")
        print "%s:" % nodes.getname(node), lines.pop(0)
        indent = "...".ljust(len("%s:" % nodes.getname(node)))
        for line in lines :
            print indent, line

for node in sorted(error.keys(), cmp=_cmp) :
    if error[node].signal is not None :
        print "%s: received signal %s" % (nodes.getname(node),
                                          error[node].signal)
    elif error[node].retcode is not None :
        print "%s: process returned %s" % (nodes.getname(node),
                                           error[node].retcode)
    for line in error[node].output.rstrip().split("\n") :
        print "***", line
