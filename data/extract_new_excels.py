import csv
import json
import glob

def get_from(row,index,to_add=0):
    try:
        val = row[index]
        val = val.replace(",","")
        if "." in val:
            return float(val)+to_add
        else:
            return int(val)+to_add
    except:
        return None

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

def to_code(row,col):
    t = row[col]
    if '-' in t:
        t = t.split('-')
        assert(len(t)==(col+1)/2)
        t= [ "%02d" % int(x) for x in t ]
        t = "00" + ''.join(t)
    else:
        add = "0" * (col+3-len(t))
        t=add+t
    return t

def add_to_sums(sums,amount,field):
    if amount is not None: sums[key][field] = sums[key].setdefault(field,0)+amount

out = file("new_csvs.json","w")
filelist = ['new_2005_2008/execution20052008.csv','new_2009_2011/execution20092011.csv','new_2012/execution2012.csv','2013_2014/budgets20132014.csv']
for filename in filelist:
    sums = {}
    budgets=csv.reader(file(filename))
    for row in budgets:
        try:
            year = int(row[0])
        except:
            continue
        for col in [1,3,5,7]:
            code = to_code(row,col)
            if len(code) != col+3: 
                print code, row
                assert(False)
            title = row[col+1].decode('utf8')
            net_allocated = get_from(row,11)
            gross_allocated = get_from(row,12,net_allocated)
            net_revised = get_from(row,18)
            gross_revised = get_from(row,19,net_revised)
            net_used = get_from(row,25)
        
            key = "%s/%s" % (year,code)
            sums.setdefault(key,{'code':code,'year':year,'title':title})
            add_to_sums(sums,net_allocated,'net_allocated')
            add_to_sums(sums,net_revised,'net_revised')
            add_to_sums(sums,net_used,'net_used')
            add_to_sums(sums,gross_allocated,'gross_allocated')
            add_to_sums(sums,gross_revised,'gross_revised')

    keys = sums.keys()
    keys.sort()
    for key in keys:
        out.write("%s\n" % json.dumps(sums[key]))

