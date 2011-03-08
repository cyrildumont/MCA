import sys, subprocess, optparse

class NodeError (Exception) :
    def __init__ (self, message, node,
                  output=None, retcode=None, signal=None) :
        Exception.__init__(self, message)
        self.node = node
        self.output = output
        self.retcode = retcode
        self.signal = signal

class NodeOSError (NodeError) :
    pass

class ClusterError (Exception) :
    def __init__ (self, message, status) :
        Exception.__init__(self, message)
        self.error = {}
        self.success = {}
        for node, state in status.items() :
            if type(state) is str :
                self.success[node] = state
            else :
                self.error[node] = state

class Nodes :
    def __init__ (self, nodes="/etc/nodes") :
        self._name = {}
        self._mac = {}
        self._ip = {}
        self._pxe = {}
        for name, mac, ip, pxe in (line.strip().split()
                                   for line in open(nodes)
                                   if not line.startswith("#")) :
            self._mac[name.lower()] = mac.upper()
            self._ip[name.lower()]  = ip
            self._pxe[name.lower()] = pxe.upper()
            self._name[mac.lower()] = name
            self._name[ip]  = name
            self._name[pxe.lower()] = name
            self._name[name.lower()] = name
            self._name[int(ip.split(".")[-1])] = name
            self._name[ip.split(".")[-1]] = name
            self._name[pxe[-2:].lower()] = name
    def getname (self, node) :
        try :
            return self._name[str(node).lower()]
        except NodeError :
            raise
        except :
            raise NodeError("unknown node", node)
    def getmac (self, node) :
        try :
            return self._mac[self.getname(node).lower()]
        except NodeError :
            raise
        except :
            raise NodeError("unknown node", node)
    def getip (self, node) :
        try :
            return self._ip[self.getname(node).lower()]
        except NodeError :
            raise
        except :
            raise NodeError("unknown node", node)
    def getpxe (self, node) :
        try :
            return self._pxe[self.getname(node).lower()]
        except NodeError :
            raise
        except :
            raise NodeError("unknown node", node)
    def getnum (self, node) :
        try :
            return int(self.getip(node).split(".")[-1])
        except NodeError :
            raise
        except :
            raise NodeError("unknown node", node)
    def printf (self, node, format) :
        try :
            return format % { "ip" : self.getip(node),
                              "mac" : self.getmac(node),
                              "pxe" : self.getpxe(node),
                              "name" : self.getname(node),
                              "num" : str(self.getnum(node)) }
        except :
            raise TypeError, "invalid format: " + repr(format)
    def __iter__ (self) :
        for num in sorted(self.getnum(node) for node in self._mac.keys()) :
            yield self.getname(num)
    def ssh (self, node, command, user=None) :
        if user is None :
            ssh = "ssh %%(name)s %s" % command
        else :
            ssh = "ssh %s@%%(name)s %s" % (user, command)
        child = subprocess.Popen(self.printf(node, ssh),
                                 stdin=None,
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.STDOUT,
                                 shell=True)
        retcode = child.wait()
        if retcode > 0 :
            raise NodeOSError("process error", node,
                              output=child.stdout.read(),
                              retcode=retcode)
        elif retcode < 0 :
            raise NodeOSError("process killed", node,
                              output=child.stdout.read(),
                              signal=abs(retcode))
        else :
            return child.stdout.read()
    def ssh_all (self, command, user=None) :
        if user is None :
            ssh = "ssh %%(name)s %s" % command
        else :
            ssh = "ssh %s@%%(name)s %s" % (user, command)
        children = {}
        for node in self :
            children[node] = subprocess.Popen(self.printf(node, ssh),
                                              stdin=None,
                                              stdout=subprocess.PIPE,
                                              stderr=subprocess.STDOUT,
                                              shell=True)
        errors = 0
        for node, child in children.items() :
            retcode = child.wait()
            if retcode > 0 :
                children[node] = NodeOSError("child error", node,
                                             output=child.stdout.read(),
                                             retcode=retcode)
                errors += 1
            elif retcode < 0 :
                children[node] = NodeOSError("child killed", node,
                                             output=child.stdout.read(),
                                             signal=abs(retcode))
                errors += 1
            else :
                children[node] = child.stdout.read()
        if errors == 1 :
            raise ClusterError("1 process failed", children)
        elif errors > 1 :
            raise ClusterError("%s processes failed" % str(errors), children)
        else :
            return children
