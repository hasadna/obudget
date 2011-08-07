import re
import urllib 
import httplib
import json
import threading
import time

def str_to_num(s):
    s=s.strip().replace(',','')
    if s=='':
        return 0
    else:
        return int(s)

def parse_data(data,code,year):
    td_re = re.compile('<TD[^>]*>(.+)</TD>')
    data = data.decode('windows-1255')
    l = td_re.findall(data.replace('&nbsp;',' '),re.I|re.M)
    records = []
    start = 0
    while start < (len(l) - 8):
        if l[start].strip().startswith(code):
            break
        else:
            start += 1
    while start < (len(l) - 8):
        if l[start].strip().startswith(code):
            records.append(l[start:start+9])
            start += 9
        else:
            break
    print "got %d lines" % len(records)
    lines = []
    for r in records:
        line = {}
        line['code'] = '00'+r[0].strip()
        line['year'] = year
        line['title'] = r[1].strip()
        line['net_allocated'] = str_to_num(r[2]) 
        line['gross_allocated'] = str_to_num(r[4]) 
        line['gross_used'] = str_to_num(r[7]) 
        lines.append(line)
    return lines
        
def download_one(year,code):
    
    fn = 'results/result-%s-%s.html' % (year,code)
    
    try:
        data = file(fn).read()
        ret = parse_data(data,code,year)
        
    except Exception,e:
        print e
        
        params = { 
            'APPNAME':'budget',
            'PRGNAME':'doc1',
            'ARGUMENTS':'BudgetCombo,PraNumber,PraName,DataView,ReportType',
            'BudgetCombo':str(year),
            'PraNumber':code,
            'DataView':'1',
            'ReportType':'1'
        }
    
        host = "religinfoserv.gov.il"
        uri = "/Magic94Scripts/mgrqispi94.dll"
        
        params = urllib.urlencode(params)
        headers =  {"Content-type": "application/x-www-form-urlencoded",
                    "Accept": "text/html"}
        
        conn = httplib.HTTPConnection(host)
        conn.request("POST", uri, params, headers)
        response = conn.getresponse()
        if response.status == 200:
            data = ''
            r = ' '
            while len(r) > 0:
                r = response.read()
                data += r
                print '.'
            file(fn,'w').write(data)
            
            print 'success downloading for %s,%s' % ( year, code )
            
            ret = parse_data(data,code,year)
        
        else:
            print 'ERROR downloading for %s,%s' % ( year, code )
            ret = []

        
        conn.close()

        time.sleep(60)
    
    return ret

def download_all():
    data = []
    
    class Downloader(threading.Thread):
        
        def __init__(self,year):
            super(Downloader,self).__init__(name='year-%d' % year)
            self.year = year
            self.data = []
            
        def run(self):
            for code in range(100):
                success = False
                while not success:
                    try:
                        self.data.extend( download_one(self.year,"%02d" % code) )
                        success = True
                    except Exception,e:
                        print 'retrying %s,%s: %s' % (self.year,code,e)
                        time.sleep(60)
                        pass

    ts = []
    for year in range(2010,2013):
        t = Downloader(year)
        t.start()
        ts.append(t)
        
    out = file('out.json','w')

    for t in ts:
        t.join()
        for d in t.data:
            out.write(json.dumps(d)+'\n')           
        
if __name__ == "__main__":
    download_all()
