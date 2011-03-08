import sys, optparse, os, java, glob


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

classname = "org.mca.agent.AgentDeployer"
classpath = ["%s/lib/"%(mca_home),"%s/cases/%s/conf/"%(mca_home,project_name),"%s/cases/%s/lib/"%(mca_home,project_name)]

agents_dir = "%s/cases/%s/agents"%(mca_home,project_name)
for agent_config in glob.glob(agents_dir +"/*.xml"):
    
    print agent_config
    computation = java.JavaProgram(classname, classpath = classpath, options="", args=agent_config)
    computation.run()







