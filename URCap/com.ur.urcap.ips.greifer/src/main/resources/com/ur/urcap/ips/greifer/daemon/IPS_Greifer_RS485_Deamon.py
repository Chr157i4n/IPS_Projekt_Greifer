#!/usr/bin/env 

# RS485 device:  /dev/ttyTool

import sys

from SimpleXMLRPCServer import SimpleXMLRPCServer
from SocketServer import ThreadingMixIn
import serial

title = ""
port = "/dev/ttyTool"
ser = serial.Serial (port=port, baudrate=19200, parity=serial.PARITY_NONE, bytesize=8, stopbits=1, timeout=1.0, write_timeout=2.0,)

def init():
	

def set_title(new_title):
	global title
	title = new_title
	return title

def get_title():
	tmp = ""
	if str(title):
		tmp = title
	else:
		tmp = "No title set"
	return tmp + " (Python)"

def get_message(name):
	if str(name):
		return "Moin " + str(name) + ", willkommen zu PolyScope!"
	else:
		return "No name set"

def get_measurement_value_test():
	return 123

def get_measurement_value():
	serial.write("m123")
	value = sio.readline()
	return value[1:]


sys.stdout.write("MyDaemon daemon started")
sys.stderr.write("MyDaemon daemon started")

class MultithreadedSimpleXMLRPCServer(ThreadingMixIn, SimpleXMLRPCServer):
	pass

server = MultithreadedSimpleXMLRPCServer(("127.0.0.1", 40405))
server.RequestHandlerClass.protocol_version = "HTTP/1.1"
server.register_function(set_title, "set_title")
server.register_function(get_title, "get_title")
server.register_function(get_message, "get_message")
server.register_function(get_measurement_value, "get_measurement_value")
server.serve_forever()
