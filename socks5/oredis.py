#! usr/bin/python 
#coding=utf-8 

import redis

class ORedis:
	
	def __init__(self, ip, port):
		self.ip = ip
		self.port = port
		self.output_handle = None
		self.output_handle = redis.Redis(host = self.ip, port = self.port, db=0)
	
	def set(self, key, value):
		self.output_handle.set(key, value)
	
	def get(self, key):
		return self.output_handle.get(key)
	
	#遍历所有key，value，d=True 则遍历后删除
	def iteritems(self, d = False):
		result = []
		for key in self.output_handle.scan_iter():
			#redis 数据不纯净时会报错
			result.append(eval('{"%s":"%s"}'%(key, self.output_handle.get(key))))
			if d:
				self.output_handle.delete(key)
		return result
	
	#清空redis
	def empty(self):
		for key in self.output_handle.scan_iter():
			self.output_handle.delete(key)
	
	def close(self):
		self.output_handle.close()
#ors = ORedis('192.168.5.131', 6379)
#print ors.iteritems(True)
