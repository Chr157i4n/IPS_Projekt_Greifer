#!/usr/bin/env python

# RS485 device:  /dev/ttyTool

import sys

from SimpleXMLRPCServer import SimpleXMLRPCServer
from SocketServer import ThreadingMixIn
import serial
import time


def log(message):
	#sys.stdout.write(message+"\n")
	#sys.stderr.write(message+"\n")
	print(message)
	#print(message+"\n")


title = ""
port = "/dev/ttyTool"
KEEPALIVE = "M113"

counter = 0

while(True):
	try:
		ser = serial.Serial (port=port, baudrate=115200, parity=serial.PARITY_NONE, bytesize=8, stopbits=1, timeout=2.0, write_timeout=2.0,)
		break
	except:
		log("could not open port: "+port)
		#ser = None

def init():
	pass


def send_message(message):
	if str(message):
		log("command: \""+str(message)+"\"")
		if(ser==None):
			value = "A" + message
		else:
			ser.reset_input_buffer()
			ser.write((message + "\n").encode())
			value = read_answer()
		log("return: \""+str(value)+"\"")
		return value
	else:
		return "Error: No message set."

def read_answer():
	buffer = KEEPALIVE
	buffer_stripped = KEEPALIVE
	while(buffer_stripped == KEEPALIVE):
		buffer = ser.readline()
		buffer_stripped = buffer.rstrip("\n")
		log(":"+str(buffer_stripped))
	if(len(buffer) > 0 and buffer[-1] != "\n"):
		log("INCOMPLETE ANSWER")
		return "-1"
	elif(buffer_stripped == ""):
		log("NO ANSWER")
		return "-2"
	elif(buffer_stripped[0] == "E"):
		log("ERROR")
		return buffer_stripped
	else:
		return buffer_stripped

def test_connection():
	global counter
	log("Test connection")
	counter += 1
	return str(counter)


log("MyDaemon daemon started")


class MultithreadedSimpleXMLRPCServer(ThreadingMixIn, SimpleXMLRPCServer):
	pass

server = MultithreadedSimpleXMLRPCServer(("127.0.0.1", 40405))
server.RequestHandlerClass.protocol_version = "HTTP/1.1"
server.register_function(test_connection, "test_connection")
server.register_function(send_message, "send_message")

server.serve_forever()
