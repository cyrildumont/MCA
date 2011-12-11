import sys, optparse, os, java


def getMCAHOME():
    pwd = os.getcwd()
    index = pwd.rfind("/")
    return pwd[0:index]

parser = optparse.OptionParser(conflict_handler="resolve",usage="%prog className")
(options, args) = parser.parse_args()

if len(args) == 0 :
    parser.print_help()
    sys.exit(1)

mca_home = getMCAHOME()
classname = args[0]
classpath = ["%s/lib/"%(mca_home),"%s/conf/"%(mca_home)]

javaOptions = "-Dmca.home=%s "%(mca_home) 
javaOptions += "-Djava.security.policy=%s/conf/security/user.policy "%(mca_home)
javaOptions += "-Djava.util.logging.config.file=%s/conf/logging.properties "%(mca_home)
javaOptions += "-Djava.security.auth.login.config=%s/conf/security/user.login "%(mca_home)
javaOptions += "-Djavax.net.ssl.trustStore=%s/conf/security/keystore.user"%(mca_home)

computation = java.JavaProgram(classname, classpath=classpath, options=javaOptions)
computation.run()








