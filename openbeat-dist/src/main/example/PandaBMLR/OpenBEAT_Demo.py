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

# Load a config file before anything else
from pandac.PandaModules import loadPrcFile
loadPrcFile("PandaBMLR.config")

# Panda3D imports
from pandac.PandaModules import *
import direct.directbase.DirectStart
from direct.showbase.DirectObject import DirectObject

# BMLR imports
from PandaBMLR.PandaBMLR import PandaBMLR
from PandaBMLR.Pawn import Pawn
from PandaBMLR.CharacterPawn import CharacterPawn
from PandaBMLR.Camera import BMLR_Camera

#OpenBEAT imports
from OpenBEATServer import OpenBEATServer

class Init(DirectObject):
	""" This demo shows the bare minimum required code to get BMLR working with OpenBEAT"""
	
	def __init__(self):

		#############
		# Load the environment
		#############
		#self.InitEnvironment()
		
		#############
		# Set up PandaBMLR
		#############
		self.BMLR = PandaBMLR()

		self.cam = BMLR_Camera(self.BMLR)
		
		base.disableMouse()
		base.camera.setPosHpr(400, 0, 160, 90, -8, 0)
		
		#############
		# Pawns
		#############		
		# Pawn that is stuck to the camera to allow characters to gaze at camera
		self.CameraPawn = Pawn(self.BMLR, "Camera", geom=None, geomType = 0)
		self.CameraPawn.reparentTo(base.camera)
		
		#############
		# Characters
		#############		
		self.Rob = CharacterPawn(self.BMLR, "Rob", "CADIA.Rob")
		self.Rob.setPosHpr(-30, -50, 102, -90, 0, 0)
		self.Rob.BML('<posture stance="Standing" />')
		
		self.SuperHumanoid = CharacterPawn(self.BMLR, "SuperHumanoid", "CADIA.MayaHumanoid.Red")
		self.SuperHumanoid.setPosHpr(-30, 50, 102, -90, 0, 0)
		self.SuperHumanoid.BML('<posture stance="Standing" />')
		
		##############
		# OpenBEAT
		##############
		#Put each character that you are going to use in the dictionary and let it return the nodepath.
		self.characters = {'Rob' : self.Rob, 'SuperHumanoid' : self.SuperHumanoid}
		self.Server = OpenBEATServer(self.characters)
		
		#############
		# Lighting
		#############	
		#self.InitLightBasic()

	def InitEnvironment(self):
		""" Loads the millenium falcon model """
		
		# Turn antialiasing on
		render.setAntialias(AntialiasAttrib.MMultisample,1)
		
		# load the falcon model
		falcon = loader.loadModel("Content/falcon/falcon.bam")
		falcon.setScale(30)
		falcon.setPos(0, 0, 28.5)
		falcon.reparentTo(render)	
		
	def InitLightBasic(self):
		""" Default lighting if video card does not support shaders """
		
		aLight = AmbientLight("AmbientLight")
		aLight.setColor(Vec4(0.3, 0.3, 0.3, 1))
		render.setLight(render.attachNewNode(aLight))
	
		dLight1 = DirectionalLight("DirectionalLight1")
		dLight1.setColor(Vec4(0.65, 0.6, 0.6, 1))		
		dLight1NP = render.attachNewNode(dLight1)
		dLight1NP.setHpr(100, -40, 0)
		render.setLight(dLight1NP)
	
		dLight2 = DirectionalLight("DirectionalLight2")
		dLight2.setColor(Vec4(0.35, 0.35, 0.3, 1))
		dLight2NP = render.attachNewNode(dLight2)
		dLight2NP.setHpr(150, -60, 0)
		render.setLight(dLight2NP)
		
Init()
run()