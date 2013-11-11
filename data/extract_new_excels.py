import csv
import json
import glob

def get_from(row,index):
    try:
        val = row[index]
        val = val.replace(",","")
        if "." in val:
            return float(val)
        else:
            return int(val)
    except:
        return 0

def sumdict(a,b):
	for k,v in b.iteritems():
		if k in ['code','year','title']: 
			a[k] = v
			continue
		a.setdefault(k,0)
		a[k]+=v
	ret = {}
	ret.update(a)
	return ret

def to_code(t):
    t = t.split('-')
    t= [ "%02x" % int(x) for x in t ]
    t = "00" + ''.join(t)

sums = {}

out = file("new_csvs.json","w")
filelist = ['new_2005_2008/execution20052008.csv','new_2009_2011/execution20092011.csv','new_2012/execution2012.csv','2013_2014/budgets20132014.csv']
for filename in filelist:
    budgets=csv.reader(file(filename))
    for row in budgets:
        try:
            year = int(row[0])
        except:
            continue
        for col in [1,3,5,7]:
            code = to_code(row[col])
            title = row[col+1].decode('utf8')
            net_allocated = get_from(row,11)
            gross_allocated = net_allocated + get_from(row,12)
            net_revised = get_from(row,18)
            gross_revised = net_revised + get_from(row,19)
            net_used = get_from(row,25)
        
            key = "%s/%s" % (year,code)
            sums.setdefault(key,{'code':code,'year':year,'title':title,
                                 'net_allocated':0,'gross_allocated':0,
                                 'net_revised':0,'gross_revised':0,
                                 'net_used':0 })
            sums[key]['net_allocated'] += net_allocated
            sums[key]['gross_allocated'] += gross_allocated
            sums[key]['net_revised'] += net_revised
            sums[key]['gross_revised'] += gross_revised
            sums[key]['net_used'] += net_used

    keys = sums.keys()
    keys.sort()
    for key in keys:
        out.write("%s\n" % json.dumps(sums[key]))

