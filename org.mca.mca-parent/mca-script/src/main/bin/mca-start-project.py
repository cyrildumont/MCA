import sys, optparse, os, java


def getMCAHOME():
    pwd = os.getcwd()
    index = pwd.rfind("/")
    return pwd[0:index]

parser = optparse.OptionParser(usage="%prog [OPTIONS] COMMAND...")
(options, args) = parser.parse_args()

if len(args) == 0 :
    parser.print_help()
    sys.exit(1)

mca_home = getMCAHOME()
project_name = args[0]

arguments = "%s start"%(project_name)
classname = "org.mca.computation.ComputationCase"
classpath = ["%s/lib/"%(mca_home),"%s/conf/"%(mca_home)]
computation = java.JavaProgram(classname, classpath = classpath, options="-Dmca.home=%s"%(mca_home), args=arguments)
computation.run()








