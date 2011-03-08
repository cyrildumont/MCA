import subprocess, os, glob
class JavaProgram :
    def __init__(self, classname, classpath=["."], options="", args=[]):
        self.classname = classname
        self.classpath = self.generateClasspath(classpath)
        self.options = options
        self.args = args
        
        
    def generateClasspath(self, dirs):    
        classpath = ""
        for dir in dirs:
            if os.path.isdir(dir):
                print "%s is a directory" %(dir)
                classpath += "%s:"%(dir)
                for jar in glob.glob(dir + "*.jar"):
                    classpath += "%s:"%jar
            else :
                print "%s is a file : %s"%(dir, os.path.isfile(dir))
                classpath += "%s:"%(dir)
            
        return classpath
                    
    def run(self):
        print "CLASSPATH = %s"%(self.classpath)
        print "CLASSNAME = %s"%(self.classname)
        print "OPTIONS = %s"%(self.options)
        print "ARGS = %s"%(self.args)
        
        command = "java %s -cp %s %s %s"%(self.options, self.classpath, self.classname, self.args)
        child = subprocess.Popen(command, stdin=None, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True)
        retcode = child.wait()
        print child.stdout.read()
        return retcode
    

        