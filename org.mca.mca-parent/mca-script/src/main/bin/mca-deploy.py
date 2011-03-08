import sys, optparse, os, java


def getMCAHOME():
    pwd = os.getcwd()
    index = pwd.rfind("/")
    return pwd[0:index]
    
parser = optparse.OptionParser(usage="%prog [options] projectName")
parser.add_option("-s","--space",dest="host",
                  help="MCASpace HOST where you want to deploy the project",
                  action="store", default="localhost")

(options, args) = parser.parse_args()

print options.host

if len(args) == 0 :
    parser.print_help()
    sys.exit(1)

mca_home = getMCAHOME()
project_name = args[0]

arguments = "%s deploy %s"%(project_name, options.host)
classname = "org.mca.computation.ComputationCase"
classpath = ["%s/lib/"%(mca_home),"%s/cases/%s/conf/"%(mca_home,project_name),"%s/cases/%s/lib/"%(mca_home,project_name)]
computation = java.JavaProgram(classname, classpath = classpath, options="-Dmca.home=%s"%(mca_home), args=arguments)
computation.run()




