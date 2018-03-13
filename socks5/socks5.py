#! usr/bin/python 
#coding=utf-8 

import socket
import threading
import select
import time
from oredis import ORedis
import hashlib

scan_host = 'Host: 192.168.5.128'
IsNeedAuth=False
Username='admin'
Password='123456'
Port=8081
ors = ORedis('192.168.5.131', 6379)

def prxoy(sock, address): 
	cs = sock  
	DspPort=0
	DspAddr=''
	try:
		recv= cs.recv(512)
		VER=recv[0:1]
		#MethodNum=ord(recv[1:2])
		#Methods=[]
		#for i in range(0,MethodNum):
		   # Methods.append(ord(recv[2+i:3+i]))
		if(IsNeedAuth):		  #Need AUTHENICATION
			cs.send(b'\x05\x02')	 #Reply
			recv= cs.recv(1024)
			Ver=recv[0:1]
			UserLen=ord(recv[1:2])
			User=recv[2:2+UserLen]
			PassLen=ord(recv[2+UserLen:3+UserLen])
			Pass=recv[3+UserLen:3+UserLen+PassLen]
			if (User==Username and Pass==Password):
				cs.send(Ver+'\x00')
			else:
				cs.send(Ver+'\xff')
				cs.close()
				return 
		else:
			cs.send(VER+'\x00')  #  NO AUTHENICATION REQUEST
		try :
			recv= cs.recv(1024)
		except Exception,ex:
			 print 'Client is Closed'
			 return
		CMD=ord(recv[1:2])
		ATYP=ord(recv[3:4])
		if(CMD ==0x01):			 # CONNECT CMD
			if (ATYP==03):					  # DOMAINNAME
				AddrLen=ord(recv[4:5])
				DspPort=256*ord(recv[5+AddrLen:5+AddrLen+1])+ord(recv[1+5+AddrLen:5+AddrLen+2])
				DspAddr=socket.gethostbyname(recv[5:5+AddrLen])
			elif (ATYP==01):					 #IPV4
				if (recv.count('.')==4):	# Asiic  format  split by  '.'
					AddrLen=ord(recv[4:5])
					DspAddr=recv[5:5+AddrLen]
					DspPort=256*ord(recv[5+AddrLen:5+AddrLen+1])+ord(recv[5+AddrLen+1:5+AddrLen+2])
				else:
					#four hex number format
					DspAddr=recv[4:8]
					DspAddrr=''
					for i in DspAddr:
						DspAddrr +=str(ord(i))+'.'
					DspAddr=DspAddrr[:-1]
					DspPort=256*ord(recv[4+4:4+4+1])+ord(recv[4+4+1:4+4+2])
			else:
				print "IPV6 is not support"
				return
			cs.send(VER+'\x00\x00\x01\x00\x00\x00\x00\x00\x00')   # REPLY
			forward(cs,DspAddr,DspPort)
		else :
			print "Don't suport  this Cmd",CMD
	except Exception,e:
		print e

m2 = hashlib.md5()
def forward(cs,DspAddr,DspPort):
	try:
		#print DspAddr +'\n'
		ss = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
		
		ss.connect((DspAddr, DspPort))
	except Exception,e:
				print "Connect to ",DspAddr,"Fail"
				return
	socks=[]
	socks.append(cs)
	socks.append(ss)
	while(True):
	   try:
		r, w, e = select.select(socks, [], [])
		for s in r:
			if s is cs:
				recv=cs.recv(2048)
				caddr,cport= cs.getpeername()
				if (len(recv) >0):
					saddr,sport= ss.getpeername()
					if sport == 80:
						if scan_host in recv:
							m2.update(recv)
							ors.set(m2.hexdigest(), recv.replace('\r\n', '\\r\\n'))
							#TODO 入库保存http request
							print recv
					print caddr,':',cport,'<',len(recv),'>',saddr,':',sport
					ss.send(recv)
					
				else:
					for sock in socks:
						sock.close()
					return
			elif s is ss:		   
				recv=ss.recv(2048)
				saddr,sport= ss.getpeername()
				if (len(recv) >0):
					caddr,cport= cs.getpeername()
					print saddr,':',sport,'<',len(recv),'>',caddr,':',cport
					cs.send(recv)
				else:
					for sock in socks:
						sock.close()
					return
	   except Exception,e:
			print "Translate data error"
			break			

if __name__ == "__main__":
	ls = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	ls.bind(('0.0.0.0',Port))
	ls.listen(500)
	while (True):
		clientSock, address = ls.accept()
		thread = threading.Thread(target=prxoy, args=(clientSock,address))
		thread.start()

