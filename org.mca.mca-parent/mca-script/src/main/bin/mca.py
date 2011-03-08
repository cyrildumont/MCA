import sys, optparse, os, java
from cluster import *
from time import sleep

def getMCAHOME():
    pwd = os.getcwd()
    index = pwd.rfind("/")
    return pwd[0:index]

def _cmp (a, b) :
    try :
        return int(a[4:]) - int(b[4:])
    except :
        return cmp(a, b)
    
def ssh(nodes, command):
    try :
        success, error = (nodes.ssh_all(command), {})
    except ClusterError :
        e = sys.exc_info()[1]
        success, error = (e.success, e.error)
    
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
        
mca_home = getMCAHOME()

parser = optparse.OptionParser(usage="%prog [OPTIONS]")
parser.add_option("-c", "--nodes-core-file", dest="core",
                  action="store", type="string", default="%s/conf/core"%(mca_home),
                  help="loads nodes from NODES (default '%s/conf/core')"%(mca_home))
parser.add_option("-m", "--nodes-master-file", dest="master",
                  action="store", type="string", default="%s/conf/master"%(mca_home),
                  help="loads nodes from NODES (default '%s/conf/master')"%(mca_home))
parser.add_option("-w", "--nodes-workers-file", dest="workers",
                  action="store", type="string", default="%s/conf/workers"%(mca_home),
                  help="loads nodes from NODES (default '%s/conf/workers')"%(mca_home))
(options, args) = parser.parse_args()

if len(args) < 1 :
    parser.print_help()
    sys.exit(1)
    
action = args[0]

print "Core %s..."%(action)
core = Nodes(options.core)  
command = "%s/bin/mcacore.sh %s"%(mca_home, action)
ssh(core, command)
sleep(5)
print "Masters %s..."%(action)
masters = Nodes(options.master)
command = "%s/bin/mcamaster.sh %s"%(mca_home, action)
ssh(masters, command)
print "Workers %s..."%(action)
workers = Nodes(options.workers)
command = "%s/bin/mcaworker.sh %s"%(mca_home, action)
ssh(workers, command)







