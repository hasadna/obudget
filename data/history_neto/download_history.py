### encoding: utf8

import os
import re
import csv
import json

baseurl = 'http://www.ag.mof.gov.il'
xls = re.compile('[^"\']+xls')

for fn in []:#'history0.html','history1.html']:
    data = file(fn).read()
    xlss = xls.findall(data)
    for x in xlss:
        #print os.popen('wget %s%s' % (baseurl, x)).read()
        x = x.split('/')[-1]
        x1 = x.split('.')[0]+'.csv'
        #print os.popen('xls2csv %s | tail -n+14 > %s' % (x, x1)).read()

out = file('history.json','w')

for y in range(1992,2012):
    fn = 'history%d.csv' % y
    r = csv.reader(file(fn))
    for l in r:
        name = l[0].strip()
        if name == "": continue
        code,title = name.split('-',1)
        code=code.strip()
        title=title.strip()
        try:
            allocated = int(l[1])
        except:
            allocated = None
        try:
            revised = int(l[2])
        except:
            revised = None
        try:
            used = int(l[3])
        except:
            used = None
        if code=='0000':  
            income_allocated = allocated
            income_revised = revised 
            income_used = used
            title = 'הכנסות המדינה'            
        if code == '00':
            if income_allocated != None and allocated != None:
                allocated -= income_allocated 
            if income_revised != None and revised != None:
                revised -= income_revised 
            if income_used != None and used != None:
                used -= income_used
            title = 'המדינה'            
#        if code.startswith('0000'):
#            if allocated != None:
#                allocated = -allocated
#            if revised != None:
#                revised = -revised
#            if used != None:
#                used = -used
        if used == None and revised == None and allocated == None: continue
        j = { 'year':y, 'code' : code, 'title' : title, 'net_allocated' : allocated, 'net_revised' : revised, 'net_used' : used }
        out.write(json.dumps(j)+'\n')
        
