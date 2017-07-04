# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# #
# #  OpenBEAT
# #  Arni Hermann Reynisson     arnir06@ru.is
# #  Eirikur Ari Petursson      eirikurp06@ru.is
# #  Gudleifur Kristjansson     gudleifur05@ru.is
# #  Hannes Hogni Vilhjalmsson  hannes@ru.is
# #
# #  Copyright(c) 2009 Center for Analysis and Design of Intelligent Agents
# #                    Reykjavik University
# #                    All rights reserved
# #
# #                    http://cadia.ru.is/
# #
# #  Based on BEAT, Copyright(c) 2000-2001 by MIT Media Lab,
# #  developed by Hannes Vilhjalmsson, Timothy Bickmore, Yang Gao and Justine Cassell
# #
# #  Based on CADIA Panda BML Realizer, Copyright(c) 2008 Center for Analysis and Design of Intelligent Agents
# #                                                       Reykjavik University,
# #  developed by Bjarni Thor Arnason and Aegir Thorsteinsson
# #
# #  Redistribution and use in source and binary forms, with or without
# #  modification, is permitted provided that the following conditions
# #  are met:
# #
# #  - Redistributions of source code must retain the above copyright notice,
# #    this list of conditions and the following disclaimer.
# #
# #  - Redistributions in binary form must reproduce the above copyright
# #    notice, this list of conditions and the following disclaimer in the
# #    documentation and#or other materials provided with the distribution.
# #
# #  - Neither the name of its copyright holders nor the names of its
# #    contributors may be used to endorse or promote products derived from
# #    this software without specific prior written permission.
# #
# #  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# #  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# #  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
# #  PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
# #  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# #  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# #  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
# #  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# #  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# #  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# #  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
# #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

#Panda3D imports
from pandac.PandaModules import *
from direct.task import Task

#Python base imports
import sys

#Global variables
PORT_DEFAULT = 15000

class OpenBEATServer():
    """Handles all incoming connections from OpenBEAT"""
    
    def __init__(self, dict = None):
        self.dict = dict
        self.nodePath = None
        self.dataList = []
        self.__Manager = None
        self.__Listener = None
        self.__Reader = None
        self.__Connection = None
        self.__Connected = False
        self.__HostName = ""

        self.__Manager = QueuedConnectionManager()
        self.__Listener = QueuedConnectionListener(self.__Manager, 0)

        self.openBEAT_Listener()
        
        taskMgr.add(self.checkData, "OpenBEAT_check_data")

    def isConnected(self):
        """Returns true or false/if there is a connection"""
        return self.__Connected

    def openBEAT_Listener(self):
        """Sets up TCP listener for OpenBEAT connection"""
        
        try:
            self.__Reader = QueuedConnectionReader(self.__Manager, 0)
            self.__Reader.setRawMode(True)

            self.__Connection = self.__Manager.openTCPServerRendezvous(PORT_DEFAULT, 5)

            self.__Listener.addConnection(self.__Connection)

            print ("Started OpenBEAT listener on port: " + str(PORT_DEFAULT))
        except:
            print ("Error initializing OpenBEAT listener on port: " + str(port))
            sys.exit()

    def checkData(self, task):
        """Polls the TCP listener to see if there is any data to be received,
            and sends the data to the appropriate nodepath if there is any"""

        if self.__Reader.dataAvailable():
            datagram = NetDatagram()
        
            if self.__Reader.getData(datagram):
                print "Data recieved: " + datagram.getMessage()
                self.dataList.append(datagram.getMessage())

                #Split the datagram and send it to appropriate place
                for line in self.dataList:
                    self.dataList = line.split('|BEAT|')

                    #Check if character is in the dictionary
                    if self.dict.has_key(self.dataList[0]):
                        self.nodePath = self.dict[self.dataList[0]]
                        self.nodePath.BML(self.dataList[1])
                        
                    if not self.dict.has_key(self.dataList[0]):
                        print 'The character ' + self.dataList[0] + ' is not available!'

                #CleanUp
                self.nodePath = None
                self.dataList = []
                        
                    

        if self.__Listener.newConnectionAvailable():
            newConnection = PointerToConnection()

            if self.__Listener.getNewConnection(newConnection):
                self.__Reader.addConnection(newConnection.p())
                self.__HostName = newConnection.p().getAddress()
                self.__Connection = newConnection
                self.__IsConnected = True

                if self.__IsConnected:
                    print "OpenBEAT connected!"

        return Task.cont
            