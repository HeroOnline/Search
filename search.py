#!/usr/bin/env python
import sys
sys.path.append('./')
sys.path.append('/usr/lib/python2.7/')
sys.path.append('/usr/local/lib/python2.7/dist-packages/')
sys.path.append('/usr/local/lib/python2.7/site-packages/')


import helper

hbase_quorum = "zk01,zk02,zk03"
hbase_port = 2181

hbase_table = "features"
hbase_family = "info"
hbase_columns = "feature"

hbase_helper = None


def init(quorum, port, table, columns):
	print "Function: init"
	print "quorum: ", quorum
	print "port: ", port
	print "table:", table
	print "columns: ", columns
	hbase_quorum = quorum
	hbase_port = port
	hbase_table = table
	hbase_family = columns[0]
	hbase_columns = columns[1:]
	#connect hbase
	hbase_helper = helper.HBaseHelper(host=quorum, port=port)
	print "connect hbase thrift at ", hbase_quorum, ":", hbase_port

def search(feature):
	print "Function: search"
	print "feature: ", feature
	print "search table ", hbase_table, hbase_family, ":(", hbase_columns, ")"
	rows = hbase_helper.scanner(hbase_table)
	return rows
